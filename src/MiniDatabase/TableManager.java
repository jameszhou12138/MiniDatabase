package MiniDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableManager {
	private static final int SIZE = 32;  //字节长度
	private static String dbHome = readDbHome();  //存放所有数据表的文件夹路径
	private static String tiPath = readTiPath();  //ti文件路径
	private static ArrayList<String> operatorList = readOperators();  //关系运算符号列表

	public TableManager() {
		//递归创建文件夹
		File dbPath = new File(dbHome);
		if (!dbPath.exists()) {
			dbPath.mkdirs();
		}
		//创建ti文件
		File tiFile = new File(tiPath);
		if (!tiFile.exists()) {
			writeFile(tiPath, "0", 0, SIZE);
		}
		System.out.println("欢迎使用迷你数据库管理系统! 请使用help命令查询所有操作介绍。");
		while (true) {
			System.out.print("miDB>");
			Scanner sc = new Scanner(System.in);
			String sql = sc.nextLine().trim();  //获取当前这一行的 sql 语句
			//直接键入回车，重新开始循环
			if (sql.equals("")) {
				continue;
			}
			//退出，跳出循环
			if (sql.equalsIgnoreCase("quit")) {
				break;
			}
			//提示，输出所有操作及其对应的含义，重新开始循环
			if (sql.equalsIgnoreCase("help")) {
				System.out.println("quit ---- 退出迷你数据库管理系统。");
				System.out.println("help ---- 显示所有的指令。");
				System.out.println("show tables ---- 显示目前所有的数据表。");
				System.out.println("desc table XXX ---- 显示数据表XXX中的表结构。");
				System.out.println("create table XXX(columnA varchar(10), columnB int, columnC decimal) ---- 创建一个3列的名称为XXX的表格，列名称分别为columnA、columnB、columnC，具体类型分别为10个以内的字符、整型数和小数。");
				System.out.println("drop table XXX ---- 删除数据表XXX。");
				System.out.println("select colX, colY, colX from XXX where colZ > 1.5 order by colZ desc ---- 从数据表XXX中选取3列，colX，colY，colX，每一个记录必须满足colZ的值大于1.5 且显示时按照colZ这一列降序排序。");
				System.out.println("select * from XXX where colA <> '北林信息' ---- 从数据表XXX中选取所有列，且记录要满足列colA不是北林信息。");
				System.out.println("insert into XXX values('北林信息', 15, 25.5) ---- 向数据表XXX中追加一条记录，各个列的值分别为北林信息、15、25.5。");
				System.out.println("delete from XXX where colB = 10 ---- 把数据表XXX中colB列的值为10的记录全部删除。");
				System.out.println("update XXX set colD = '计算机科学与技术' where colA = '北林信息' ---- 在数据表XXX中，把那些colA是北林信息的记录中的colD列全部改写为计算机科学与技术。\n");
				continue;
			}
			System.out.println(operate(sql));  //输出输入 sql 语句后返回的信息
		}
	}

	//解析 sql 语句
	public static SqlResult operate(String sql) {
		SqlResult sqlResult = new SqlResult();  //记录解析语句后的结果
		String op = sql.split("\\s")[0];  //获取第一个词
		if (op.equalsIgnoreCase("show")) {
			//显示目前所有的数据表
			String pattern = "(show +)(tables+)";  //正则表达式
			Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			Matcher m = r.matcher(sql);
			if (m.find()) {
				sqlResult.setSqlFeedBack(showTables());  //匹配，调用 showTables 方法
			} else {
				sqlResult.setSqlFeedBack("show 语法错误\n");  //不匹配，提示 show 语法错误
			}
		} else if (op.equalsIgnoreCase("desc")) {
			//显示数据表的表结构
			String pattern = "(desc +)(table +)(\\w+)";  //正则表达式
			Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			Matcher m = r.matcher(sql);
			if (m.find()) {
				String tableName = m.group(3).trim();
				sqlResult.setSqlFeedBack(descTable(tableName));  //匹配，调用 descTable 方法
			} else {
				sqlResult.setSqlFeedBack("desc 语法错误\n");  //不匹配，提示 desc 语法错误
			}
		} else if (op.equalsIgnoreCase("create")) {
			//创建数据表
			String pattern = "(create +)(table +)(\\w+)\\s*\\((.+)\\)";  //正则表达式
			Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			Matcher m = r.matcher(sql);
			if (m.find()) {
				String tableName = m.group(3).trim();  //数据表的表名
				String[] values = m.group(4).trim().split(",");
				ArrayList<String> nameList = new ArrayList<>();  //存放所有字段名称
				ArrayList<String> typeList = new ArrayList<>();  //存放所有字段类型
				for (String value : values) {
					String t = value.trim();
					nameList.add(t.substring(0, t.indexOf(" ")));
					typeList.add(t.substring(t.lastIndexOf(" ") + 1));
				}
				sqlResult.setSqlFeedBack(createTable(tableName, nameList, typeList));  //匹配，调用 createTable 方法
			} else {
				sqlResult.setSqlFeedBack("create 语句法错误\n");  //不匹配，提示 create 语法错误
			}
		} else if (op.equalsIgnoreCase("drop")) {
			//删除数据表
			String pattern = "(drop +)(table +)(\\w+)";  //正则表达式
			Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			Matcher m = r.matcher(sql);
			if (m.find()) {
				String tableName = m.group(3).trim();
				sqlResult.setSqlFeedBack(dropTable(tableName));  //匹配，调用 dropTable 方法
			} else {
				sqlResult.setSqlFeedBack("drop 语法错误\n");  //不匹配，提示 drop 语法错误
			}
		} else if (op.equalsIgnoreCase("select")) {
			//查询数据表
			String pattern1 = "(select +)(.+)(from +)(.+)";  //正则表达式
			Pattern r1 = Pattern.compile(pattern1, Pattern.CASE_INSENSITIVE);
			Matcher m1 = r1.matcher(sql);
			if (m1.find()) {
				//匹配
				boolean flag = true;  //记录 select 语法是否正确
				String[] columns = m1.group(2).trim().split(",");
				if (columns.length > 1 && columns[0].equals("*")) {
					flag = false;  //查询字段既包含了 *，又包含了其他，提示 select 语法错误
				} else {
					ArrayList<String> columnList = new ArrayList<>();  //记录所有的字段名称
					for (String column : columns) {
						columnList.add(column.trim());
					}
					String tableName = m1.group(4).split(" ")[0];  //数据表表名
					String pattern2 = "(.+)\\s*(where +)(\\w+)\\s*(" + operatorsToRegex(operatorList) + ")\\s*([^ ]+)";  //where 正则表达式
					Pattern r2 = Pattern.compile(pattern2, Pattern.CASE_INSENSITIVE);
					Matcher m2 = r2.matcher(m1.group(4));
					String caseColumn = "";  //where 的字段名称
					String operator = "";  //where 的关系运算符号
					String caseValue = "";  //where 比较的值
					if (m2.find()) {
						//where 匹配，更新值
						caseColumn = m2.group(3).trim();
						operator = m2.group(4).trim();
						caseValue = m2.group(5).trim();
					}
					String pattern3 = "(.+)\\s*(order +)(by +)(\\w+)\\s*(.*)\\s*";  //order by 正则表达式
					Pattern r3 = Pattern.compile(pattern3, Pattern.CASE_INSENSITIVE);
					Matcher m3 = r3.matcher(m1.group(4));
					String orderColumn = "";  //排序依据字段
					boolean isAscOrder = true;  //是否为升序
					if (m3.find()) {
						//order by 匹配
						if (m3.group(5).trim().equals("") || m3.group(5).trim().equals("asc")) {
							//升序，更新值
							orderColumn = m3.group(4).trim();
						} else if (m3.group(5).trim().equals("desc")) {
							//降序，更新值
							orderColumn = m3.group(4).trim();
							isAscOrder = false;
						} else {
							flag = false;  //非空字符串、asc和desc，提示 select 语法错误
						}
					}
					if (flag) {
						sqlResult.setSqlFeedBack(selectData(tableName, columnList, caseColumn, operator, caseValue, orderColumn, isAscOrder));  //语法正常，调用 selectData 方法
					}
				}
				if (!flag) {
					sqlResult.setSqlFeedBack("select 语法错误\n");  //select 语法错误
				}
			} else {
				sqlResult.setSqlFeedBack("select 语法错误\n");  //不匹配，提示 select 语法错误
			}
		} else if (op.equalsIgnoreCase("insert")) {
			//插入记录
			String pattern = "(insert +)(into +)(.+)(values *)\\((.+)\\)";  //正则表达式
			Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			Matcher m = r.matcher(sql);
			if (m.find()) {
				String tableName = m.group(3).trim();  //数据表表名
				String[] infos = m.group(5).trim().split(",");
				ArrayList<String> infoList = new ArrayList<>();   //插入记录的信息
				for (String info : infos) {
					infoList.add(info.trim());
				}
				sqlResult.setSqlFeedBack(insertTable(tableName, infoList));  //匹配，调用 insertTable 方法
			} else {
				sqlResult.setSqlFeedBack("insert 语法错误\n");  //不匹配，提示 insert 语法错误
			}
		} else if (op.equalsIgnoreCase("delete")) {
			//删除记录
			String pattern = "(delete +)(from +)(\\w+)\\s*(where +)(\\w+)\\s*(" + operatorsToRegex(operatorList) + ")\\s*([^ ]+)";  //正则表达式
			Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			Matcher m = r.matcher(sql);
			if (m.find()) {
				String tableName = m.group(3).trim();  //数据表表名
				String column = m.group(5).trim();  //where 的字段名
				String operator = m.group(6).trim(); //where 的关系运算符号
				String value = m.group(7).trim();  //where 比较的值
				sqlResult.setSqlFeedBack(deleteData(tableName, column, operator, value));
			} else {
				sqlResult.setSqlFeedBack("delete 语法错误\n");
			}
		} else if (op.equalsIgnoreCase("update")) {
			//更新记录
			String pattern = "(update +)(\\w+)\\s*(set +)(.+)\\s*(=)(.+)(where +)(\\w+)\\s*(" + operatorsToRegex(operatorList) + ")\\s*([^ ]+)";  //正则表达式
			Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			Matcher m = r.matcher(sql);
			if (m.find()) {
				String tableName = m.group(2).trim();  //数据表表名
				String column1 = m.group(4).trim();  //set 的字段名
				String value1 = m.group(6).trim();  //set 的值
				String column2 = m.group(8).trim();  //where 的字段名
				String operator = m.group(9).trim(); //where 的关系运算符号
				String value2 = m.group(10).trim();  //where 比较的值
				sqlResult.setSqlFeedBack(updateTable(tableName, column1, value1, column2, operator, value2));  //匹配，调用 updateTable 方法
			} else {
				sqlResult.setSqlFeedBack("update 语法错误\n");  //不匹配，提示 update 语法错误
			}
		}
		//当输入的语句的第一个词不为以上 8 种情况时，为非 sql 语句，返回 sql 语法错误
		if (sqlResult.getSqlFeedBack() == null) {
			sqlResult.setSqlFeedBack("sql 语法错误\n");
		}
		return sqlResult;  //返回解析后的语句
	}

	//显示所有数据表
	public static String showTables() {
		int cnt = Integer.parseInt(readFile(tiPath, 0, SIZE));  //数据表的数量
		String ans = "数据表\n";
		for (int i = 1; i <= cnt; i++) {
			ans += readFile(tiPath, i * SIZE, SIZE) + "\n";  //每一个数据表表名
		}
		ans += "共有 " + cnt + " 个数据表\n";
		return ans;
	}

	//显示数据表表结构
	public static String descTable(String tableName) {
		String dbPath = getDbPath(tableName);  //.midb文件路径
		File midbFile = new File(dbPath);
		if (!midbFile.exists()) {
			return "数据表 " + tableName + " 不存在！\n";  //不存在该数据表，返回“数据表不存在”
		}
		int len = Integer.parseInt(readFile(dbPath, SIZE, SIZE));  //数据表的字段个数
		String ans = "列表名, 列类型, 列宽度\n";
		for (int i = 0; i < len; i++) {
			String name = readFile(dbPath, (2 + i) * SIZE, SIZE);  //字段名称
			String type = readFile(dbPath, (2 + len + i) * SIZE, SIZE);  //字段类型
			if (type.contains("(")) {
				String length = type.substring(type.indexOf("(") + 1, type.indexOf(")"));  //字段长度
				type = type.substring(0, type.indexOf("("));
				ans += name + ", " + type + ", " + length;
			} else {
				ans += name + ", " + type + ", 1";
			}
			ans += "\n";
		}
		return ans;
	}

	//创建数据表
	public static String createTable(String tableName, ArrayList<String> nameList, ArrayList<String> typeList) {
		String dbPath = getDbPath(tableName);  //.midb文件路径
		File midbFile = new File(dbPath);
		if (midbFile.exists()) {
			return "数据表 " + tableName + " 已存在！\n";  //存在该数据表，返回“数据表已存在”
		}
		//写 .ti 文件
		String preTablesNum = readFile(tiPath, 0, SIZE);
		int currentTablesNum = Integer.parseInt(preTablesNum) + 1;
		writeFile(tiPath, String.valueOf(currentTablesNum), 0, SIZE);
		writeFile(tiPath, tableName, currentTablesNum * SIZE, SIZE);
		//写 .midb 文件
		writeFile(dbPath, "0", 0, SIZE);
		writeFile(dbPath, String.valueOf(nameList.size()), SIZE, SIZE);
		for (int i = 0; i < nameList.size(); i++) {
			writeFile(dbPath, nameList.get(i), (2 + i) * SIZE, SIZE);
		}
		for (int i = 0; i < typeList.size(); i++) {
			writeFile(dbPath, typeList.get(i), (2 + nameList.size() + i) * SIZE, SIZE);
		}
		return "创建数据表 " + tableName + " 成功！\n";
	}

	//删除数据表
	public static String dropTable(String tableName) {
		String dbPath = getDbPath(tableName);  //.midb文件路径
		File midbFile = new File(dbPath);
		if (!midbFile.exists()) {
			return "数据表 " + tableName + " 不存在！\n";  //不存在该数据表，返回“数据表不存在”
		}
		midbFile.delete();  //删除.midb文件
		int cnt = Integer.parseInt(readFile(tiPath, 0, SIZE));  //数据表的个数
		writeFile(tiPath, String.valueOf(cnt - 1), 0, SIZE);  //更新数据表的个数
		//找到对应的数据表名称删除
		int index = 1;
		while (index <= cnt) {
			if (Objects.requireNonNull(readFile(tiPath, index * SIZE, SIZE)).equalsIgnoreCase(tableName)) {
				break;
			}
			index++;
		}
		//更新.ti文件
		for (int i = index; i < cnt; i++) {
			String nextTableName = readFile(tiPath, (index + 1) * SIZE, SIZE);
			writeFile(tiPath, nextTableName, index * SIZE, SIZE);
		}
		writeFile(tiPath, "", cnt * SIZE, SIZE);
		return "删除数据表 " + tableName + " 成功！\n";
	}

	//筛选数据表的记录
	public static String selectData(String tableName, ArrayList<String> columnList, String caseColumn, String operator, String caseValue, String orderColumn, boolean isAscOrder) {
		String dbPath = getDbPath(tableName);  //.midb文件路径
		File midbFile = new File(dbPath);
		if (!midbFile.exists()) {
			return "数据表 " + tableName + " 不存在！\n";  //不存在该数据表，返回“数据表不存在”
		}
		int cnt = Integer.parseInt(readFile(dbPath, 0, SIZE));
		int length = Integer.parseInt(readFile(dbPath, SIZE, SIZE));
		ArrayList<String> allTitleList = new ArrayList<>();
		for (int i = 0; i < length; i++) {
			allTitleList.add(readFile(dbPath, (2 + i) * SIZE, SIZE));
		}
		ArrayList<String> titleList = new ArrayList<>();
		ArrayList<Integer> indexList = new ArrayList<>();
		//* 或者字段列表
		if (columnList.get(0).equals("*")) {
			for (int i = 0; i < length; i++) {
				indexList.add(i);
			}
			titleList = allTitleList;
		} else {
			for (String column : columnList) {
				if (allTitleList.contains(column)) {
					indexList.add(allTitleList.indexOf(column));
					titleList.add(column);
				} else {
					return "不存在名为 " + column + " 的字段！\n";
				}
			}
		}
		ArrayList<ArrayList<Object>> infoList = new ArrayList<>(new ArrayList<>());
		//where
		if (caseColumn.equals("")) {
			for (int i = 0; i < cnt; i++) {
				ArrayList<Object> info = new ArrayList<>();
				for (int index : indexList) {
					info.add(readFile(dbPath, (2 + (2 + i) * length + index) * SIZE, SIZE));
				}
				infoList.add(info);
			}
		} else {
			if (!allTitleList.contains(caseColumn)) {
				return "不存在名为 " + caseColumn + " 的字段！\n";
			}
			int caseIndex = allTitleList.indexOf(caseColumn);
			String caseType = readFile(dbPath, (2 + length + caseIndex) * SIZE, SIZE);
			Object obj2 = null;
			if (caseType.contains("int")) {
				try {
					obj2 = Integer.parseInt(caseValue);
				} catch (Exception e) {
					return caseColumn + " 字段需要的参数类型为 int 类型\n";
				}
			} else if (caseType.contains("decimal")) {
				try {
					obj2 = Double.parseDouble(caseValue);
				} catch (Exception e) {
					return caseColumn + " 字段需要的参数类型为 decimal 类型\n";
				}
			} else if (caseType.contains("varchar")) {
				if (caseValue.charAt(0) != '\'' || caseValue.charAt(caseValue.length() - 1) != '\'') {
					return caseColumn + " 字段需要的参数类型为 varchar 类型\n";
				}
				obj2 = caseValue.substring(1, caseValue.length() - 1);
			}
			for (int i = 0; i < cnt; i++) {
				Object obj1 = null;
				String s = readFile(dbPath, (2 + (2 + i) * length + caseIndex) * SIZE, SIZE);
				if (caseType.contains("int")) {
					obj1 = Integer.parseInt(s);
				} else if (caseType.contains("decimal")) {
					obj1 = Double.parseDouble(s);
				} else if (caseType.contains("varchar")) {
					obj1 = s;
				}
				if (compareObject(obj1, obj2, operator)) {
					ArrayList<Object> info = new ArrayList<>();
					for (int index : indexList) {
						info.add(readFile(dbPath, (2 + (2 + i) * length + index) * SIZE, SIZE));
					}
					infoList.add(info);
				}
			}
		}
		//order by
		if (!orderColumn.equals("")) {
			if (!allTitleList.contains(orderColumn)) {
				return "不存在名为 " + orderColumn + " 的字段！\n";
			}
			int orderIndex = allTitleList.indexOf(orderColumn);
			String orderType = readFile(dbPath, (2 + length + orderIndex) * SIZE, SIZE);
			ArrayList<Object> byObjects = new ArrayList<>();
			if (caseColumn.equals("")) {
				for (int i = 0; i < cnt; i++) {
					String s = readFile(dbPath, (2 + (2 + i) * length + orderIndex) * SIZE, SIZE);
					Object obj = null;
					if (orderType.contains("int")) {
						obj = Integer.parseInt(s);
					} else if (orderType.contains("decimal")) {
						obj = Double.parseDouble(s);
					} else if (orderType.contains("varchar")) {
						obj = s;
					}
					byObjects.add(obj);
				}
			} else {
				int caseIndex = allTitleList.indexOf(caseColumn);
				String caseType = readFile(dbPath, (2 + length + caseIndex) * SIZE, SIZE);
				Object obj2 = null;
				if (caseType.contains("int")) {
					try {
						obj2 = Integer.parseInt(caseValue);
					} catch (Exception e) {
						return caseColumn + " 字段需要的参数类型为 int 类型\n";
					}
				} else if (caseType.contains("decimal")) {
					try {
						obj2 = Double.parseDouble(caseValue);
					} catch (Exception e) {
						return caseColumn + " 字段需要的参数类型为 decimal 类型\n";
					}
				} else if (caseType.contains("varchar")) {
					if (caseValue.charAt(0) != '\'' || caseValue.charAt(caseValue.length() - 1) != '\'') {
						return caseColumn + " 字段需要的参数类型为 varchar 类型\n";
					}
					obj2 = caseValue.substring(1, caseValue.length() - 1);
				}
				for (int i = 0; i < cnt; i++) {
					Object obj1 = null;
					String s = readFile(dbPath, (2 + (2 + i) * length + caseIndex) * SIZE, SIZE);
					if (caseType.contains("int")) {
						obj1 = Integer.parseInt(s);
					} else if (caseType.contains("decimal")) {
						obj1 = Double.parseDouble(s);
					} else if (caseType.contains("varchar")) {
						obj1 = s;
					}
					if (compareObject(obj1, obj2, operator)) {
						String ss = readFile(dbPath, (2 + (2 + i) * length + orderIndex) * SIZE, SIZE);
						Object obj = null;
						if (orderType.contains("int")) {
							obj = Integer.parseInt(ss);
						} else if (orderType.contains("decimal")) {
							obj = Double.parseDouble(ss);
						} else if (orderType.contains("varchar")) {
							obj = ss;
						}
						byObjects.add(obj);
					}
				}
			}
			infoList = sortedDisplay(infoList, byObjects, isAscOrder);
		}
		String ans = "";
		for (int i = 0; i < titleList.size(); i++) {
			ans += titleList.get(i);
			if (i != titleList.size() - 1) {
				ans += ", ";
			} else {
				ans += "\n";
			}
		}
		for (ArrayList<Object> info : infoList) {
			for (int i = 0; i < info.size(); i++) {
				ans += info.get(i);
				if (i != info.size() - 1) {
					ans += ", ";
				} else {
					ans += "\n";
				}
			}
		}
		ans += "共有 " + infoList.size() + " 条记录\n";
		return ans;
	}

	//插入记录
	public static String insertTable(String tableName, ArrayList<String> infoList) {
		String dbPath = getDbPath(tableName);  //.midb文件路径
		File midbFile = new File(dbPath);
		if (!midbFile.exists()) {
			return "数据表 " + tableName + " 不存在！\n";  //不存在该数据表，返回“数据表不存在”
		}
		int length = Integer.parseInt(readFile(dbPath, SIZE, SIZE));
		if (length != infoList.size()) {
			return "插入数量不合法";
		}
		int cnt = Integer.parseInt(readFile(dbPath, 0, SIZE));
		for (int i = 0; i < length; i++) {
			String type = readFile(dbPath, (2 + length + i) * SIZE, SIZE);
			if (type.contains("int")) {
				try {
					Integer.parseInt(infoList.get(i));
				} catch (Exception e) {
					return "第 " + (i + 1) + " 个参数需要为 int 类型\n";
				}
			} else if (type.contains("decimal")) {
				try {
					Double.parseDouble(infoList.get(i));
				} catch (Exception e) {
					return "第 " + (i + 1) + " 个参数需要为 decimal 类型\n";
				}
			} else if (type.contains("varchar")) {
				String s = infoList.get(i);
				if (s.charAt(0) != '\'' || s.charAt(s.length() - 1) != '\'') {
					return "第 " + (i + 1) + " 个参数需要为 varchar 类型\n";
				}
				s = s.substring(1, s.length() - 1);
				infoList.set(i, s);
				int maxLength = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));
				if (s.length() > maxLength) {
					return "第 " + (i + 1) + " 个参数字符串长度过长！\n";
				}
			}
			writeFile(dbPath, infoList.get(i), (2 + 2 * length + cnt * length + i) * SIZE, SIZE);
		}
		writeFile(dbPath, String.valueOf(cnt + 1), 0, SIZE);
		return "向数据表 " + tableName + " 写入一条记录成功！\n";
	}

	//删除数据
	public static String deleteData(String tableName, String column, String operator, String value) {
		String dbPath = getDbPath(tableName);  //.midb文件路径
		File midbFile = new File(dbPath);
		if (!midbFile.exists()) {
			return "数据表 " + tableName + " 不存在！\n";  //不存在该数据表，返回“数据表不存在”
		}
		int length = Integer.parseInt(readFile(dbPath, SIZE, SIZE));
		int index = 0;
		while (index < length) {
			if (readFile(dbPath, (2 + index) * SIZE, SIZE).equalsIgnoreCase(column)) {
				break;
			}
			index++;
		}
		if (index == length) {
			return "数据表 " + tableName + " 中不存在名称为 " + column + " 的字段！\n";
		}
		String type = readFile(dbPath, (2 + length + index) * SIZE, SIZE);
		Object obj2 = null;
		if (type.contains("int")) {
			try {
				obj2 = Integer.parseInt(value);
			} catch (Exception e) {
				return column + " 字段需要的参数类型为 int 类型\n";
			}
		} else if (type.contains("decimal")) {
			try {
				obj2 = Double.parseDouble(value);
			} catch (Exception e) {
				return column + " 字段需要的参数类型为 decimal 类型\n";
			}
		} else if (type.contains("varchar")) {
			if (value.charAt(0) != '\'' || value.charAt(value.length() - 1) != '\'') {
				return column + " 字段需要的参数类型为 varchar 类型\n";
			}
			obj2 = value.substring(1, value.length() - 1);
		}
		int cnt = Integer.parseInt(readFile(dbPath, 0, SIZE));
		boolean isDeleted = false;
		for (int i = 0; i < cnt; i++) {
			Object obj1 = null;
			String s = readFile(dbPath, (2 + (2 + i) * length + index) * SIZE, SIZE);
			if (type.contains("int")) {
				obj1 = Integer.parseInt(s);
			} else if (type.contains("decimal")) {
				obj1 = Double.parseDouble(s);
			} else if (type.contains("varchar")) {
				obj1 = s;
			}
			if (compareObject(obj1, obj2, operator)) {
				for (int j = i; j < cnt - 1; j++) {
					String nextInfo = readFile(dbPath, (2 + (2 + j + 1) * length) * SIZE, length * SIZE);
					writeFile(dbPath, nextInfo, (2 + (2 + j) * length) * SIZE, length * SIZE);
				}
				writeFile(dbPath, "", (2 + (2 + cnt - 1) * length) * SIZE, length * SIZE);
				cnt--;
				i--;
				isDeleted = true;
			}
		}
		if (isDeleted) {
			writeFile(dbPath, String.valueOf(cnt), 0, SIZE);
			return "删除数据表 " + tableName + " 所有满足条件的记录成功！\n";
		} else {
			return "数据表 " + tableName + " 没有满足条件的记录！\n";
		}
	}

	//更新记录
	public static String updateTable(String tableName, String column1, String value1, String column2, String operator, String value2) {
		String dbPath = getDbPath(tableName);  //.midb文件路径
		File midbFile = new File(dbPath);
		if (!midbFile.exists()) {
			return "数据表 " + tableName + " 不存在！\n";  //不存在该数据表，返回“数据表不存在”
		}
		int length = Integer.parseInt(readFile(dbPath, SIZE, SIZE));
		int index1 = 0;
		while (index1 < length) {
			if (readFile(dbPath, (2 + index1) * SIZE, SIZE).equalsIgnoreCase(column1)) {
				break;
			}
			index1++;
		}
		if (index1 == length) {
			return "数据表 " + tableName + " 中不存在名称为 " + column1 + " 的字段！\n";
		}
		String type1 = readFile(dbPath, (2 + length + index1) * SIZE, SIZE);
		if (type1.contains("int")) {
			try {
				Integer.parseInt(value1);
			} catch (Exception e) {
				return column1 + " 字段需要的参数类型为 int 类型\n";
			}
		} else if (type1.contains("decimal")) {
			try {
				Double.parseDouble(value1);
			} catch (Exception e) {
				return column1 + " 字段需要的参数类型为 decimal 类型\n";
			}
		} else if (type1.contains("varchar")) {
			if (value1.charAt(0) != '\'' || value1.charAt(value1.length() - 1) != '\'') {
				return column1 + " 字段需要的参数类型为 varchar 类型\n";
			}
			value1 = value1.substring(1, value1.length() - 1);
		}
		int index2 = 0;
		while (index2 < length) {
			if (readFile(dbPath, (2 + index2) * SIZE, SIZE).equalsIgnoreCase(column2)) {
				break;
			}
			index2++;
		}
		if (index2 == length) {
			return "数据表 " + tableName + " 中不存在名称为 " + column2 + " 的字段！\n";
		}
		String type = readFile(dbPath, (2 + length + index2) * SIZE, SIZE);
		Object obj2 = null;
		if (type.contains("int")) {
			try {
				obj2 = Integer.parseInt(value2);
			} catch (Exception e) {
				return column2 + " 字段需要的参数类型为 int 类型\n";
			}
		} else if (type.contains("decimal")) {
			try {
				obj2 = Double.parseDouble(value2);
			} catch (Exception e) {
				return column2 + " 字段需要的参数类型为 decimal 类型\n";
			}
		} else if (type.contains("varchar")) {
			if (value2.charAt(0) != '\'' || value2.charAt(value2.length() - 1) != '\'') {
				return column2 + " 字段需要的参数类型为 varchar 类型\n";
			}
			obj2 = value2.substring(1, value2.length() - 1);
		}
		int cnt = Integer.parseInt(readFile(dbPath, 0, SIZE));
		boolean isUpdated = false;
		for (int i = 0; i < cnt; i++) {
			Object obj1 = null;
			String s = readFile(dbPath, (2 + (2 + i) * length + index2) * SIZE, SIZE);
			if (type.contains("int")) {
				obj1 = Integer.parseInt(s);
			} else if (type.contains("decimal")) {
				obj1 = Double.parseDouble(s);
			} else if (type.contains("varchar")) {
				obj1 = s;
			}
			if (compareObject(obj1, obj2, operator)) {
				writeFile(dbPath, value1, (2 + (2 + i) * length + index1) * SIZE, SIZE);
				isUpdated = true;
			}
		}
		if (isUpdated) {
			writeFile(dbPath, String.valueOf(cnt), 0, SIZE);
			return "修改数据表 " + tableName + " 所有满足条件的记录成功！\n";
		} else {
			return "数据表 " + tableName + " 无需修改！\n";
		}
	}

	//若 obj1 operator obj2 返回true，否则返回false
	public static boolean compareObject(Object obj1, Object obj2, String operator) {
		if (obj1.getClass() != obj2.getClass()) {
			return false;
		}
		boolean flagSatisfied = false;
		int compareResult = 0;
		if (obj1 instanceof Integer && obj2 instanceof Integer) {
			compareResult = ((Integer) obj1).compareTo((Integer) obj2);
		} else if (obj1 instanceof Double && obj2 instanceof Double) {
			compareResult = ((Double) obj1).compareTo((Double) obj2);
		} else if (obj1 instanceof String && obj2 instanceof String) {
			compareResult = ((String) obj1).compareTo((String) obj2);
		}
		if (compareResult < 0 && operator.contains("<")) {
			flagSatisfied = true;
		} else if (compareResult == 0 && operator.contains("=")) {
			flagSatisfied = true;
		} else if (compareResult > 0 && operator.contains(">")) {
			flagSatisfied = true;
		}
		return flagSatisfied;
	}

	//排序
	public static ArrayList<ArrayList<Object>> sortedDisplay(ArrayList<ArrayList<Object>> objects, ArrayList<Object> byObjects, boolean flagAscend) {
		// 如果没有排序要求或数据数组为空，则直接返回获取的内容：
		if (byObjects == null || objects.size() == 0) {
			return objects;
		}
		char type = 'i';
		// 反射
		if (byObjects.get(0) instanceof Double) {
			type = 'd';
		}
		if (byObjects.get(0) instanceof String) {
			type = 's';
		}
		char typeFinal = type;
		class LineAndBy implements Comparable<LineAndBy> {
			ArrayList<Object> line;
			Object byObject;

			LineAndBy(ArrayList<Object> line, Object byObject) {
				this.line = line;
				this.byObject = byObject;
			}

			public int compareTo(LineAndBy that) {
				if (typeFinal == 'i') {
					Integer thisInteger = (Integer) this.byObject;
					Integer thatInteger = (Integer) that.byObject;
					int compareResult = thisInteger.compareTo(thatInteger);
					if (flagAscend) {
						return compareResult;
					} else {
						return -compareResult;
					}
				} else if (typeFinal == 'd') {
					Double thisDouble = (Double) this.byObject;
					Double thatDouble = (Double) that.byObject;
					int compareResult = thisDouble.compareTo(thatDouble);
					if (flagAscend) {
						return compareResult;
					} else {
						return -compareResult;
					}
				} else {
					String thisString = (String) this.byObject;
					String thatString = (String) that.byObject;
					int compareResult = thisString.compareTo(thatString);
					if (flagAscend) {
						return compareResult;
					} else {
						return -compareResult;
					}
				}
			}
		}
		int amount = objects.size();
		LineAndBy[] lineAndBys = new LineAndBy[amount];
		for (int i = 0; i < amount; i++) {
			lineAndBys[i] = new LineAndBy(objects.get(i), byObjects.get(i));
		}
		java.util.Arrays.sort(lineAndBys);
		ArrayList<ArrayList<Object>> objectsToDisplay = new ArrayList<>();
		for (int i = 0; i < amount; i++) {
			objectsToDisplay.add(lineAndBys[i].line);
		}
		return objectsToDisplay;
	}

	//写文件
	public static void writeFile(String path, String content, int pos, int size) {
		try {
			RandomAccessFile raf = new RandomAccessFile(path, "rw");
			raf.skipBytes(pos);
			byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
			bytes = Arrays.copyOf(bytes, size);
			raf.write(bytes);
			raf.close();
		} catch (IOException e) {
			System.out.println("编写文件失败！");
		}
	}

	//读文件
	public static String readFile(String path, int pos, int size) {
		try {
			RandomAccessFile raf = new RandomAccessFile(path, "rw");
			raf.skipBytes(pos);
			byte[] bytes = new byte[size];
			raf.read(bytes);
			String result = new String(bytes, StandardCharsets.UTF_8).trim();
			raf.close();
			return result;
		} catch (IOException e) {
			System.out.println("读取文件失败！");
		}
		return null;
	}

	//读取存放所有数据表的文件夹路径
	public static String readDbHome() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("Config.properties"));
			return properties.getProperty("dbHome");
		} catch (IOException ioe) {
			return null;
		}
	}

	//计算数据表 table 的路径
	public static String getDbPath(String table) {
		return dbHome + "/" + table + ".midb";
	}

	//读取ti文件的路径
	public static String readTiPath() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("Config.properties"));
			return properties.getProperty("tiHome");
		} catch (IOException ioe) {
			return null;
		}
	}

	//读取比较符号列表
	public static ArrayList<String> readOperators() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("Config.properties"));
			String operators = properties.getProperty("operators");
			String[] operatorArray = operators.split(";");
			ArrayList<String> operatorList = new ArrayList<>();
			for (String operator : operatorArray) {
				operatorList.add(operator.trim());
			}
			return operatorList;
		} catch (IOException ioe) {
			return null;
		}
	}

	public static String operatorsToRegex(List<String> operatorList) {
		String ans = "";
		for (int i = 0; i < operatorList.size(); i++) {
			if (i != 0) {
				ans += "|";
			}
			ans += operatorList.get(i);
		}
		return ans;
	}

	public static void main(String[] args) {
		TableManager tableManager = new TableManager();
	}
}
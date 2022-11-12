package MiniDatabase;

public class SqlResult {
	private String sqlFeedBack = null;
	private Object[][] valueLines = null;
	public SqlResult() { }
	public SqlResult(String sqlFeedBack, Object[][] valueLines) {
		this.sqlFeedBack = sqlFeedBack;
		this.valueLines = valueLines;
	}

	public String getSqlFeedBack() {
		return sqlFeedBack;
	}

	public Object[][] getValueLines() {
		return valueLines;
	}

	public void setSqlFeedBack(String sqlFeedBack) {
		this.sqlFeedBack = sqlFeedBack;
	}

	public void setValueLines(Object[][] valueLines) {
		this.valueLines = valueLines;
	}

	@Override public String toString() {
		// Add your code here:
		return sqlFeedBack;
	}
}

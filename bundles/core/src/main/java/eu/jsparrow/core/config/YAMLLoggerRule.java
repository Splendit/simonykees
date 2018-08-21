package eu.jsparrow.core.config;

public class YAMLLoggerRule {

	private String systemOutReplaceOption;
	private String systemErrReplaceOption;
	private String printStacktraceReplaceOption;
	private String systemOutPrintExceptionReplaceOption;
	private String systemErrPrintExceptionReplaceOption;
	private boolean newLoggingStatementOption;

	public YAMLLoggerRule() {
		super();
		this.systemOutReplaceOption = "info";
		this.systemErrReplaceOption = "error";
		this.printStacktraceReplaceOption = "error";
		this.systemOutPrintExceptionReplaceOption = "info";
		this.systemErrPrintExceptionReplaceOption = "error";
		this.newLoggingStatementOption = true;
	}

	public YAMLLoggerRule(String systemOutReplaceOption, String systemErrReplaceOption,
			String printStacktraceReplaceOption, String systemOutPrintExceptionReplaceOption,
			String systemErrPrintExceptionReplaceOption, boolean newLoggingStatementOption) {
		super();
		this.systemOutReplaceOption = systemOutReplaceOption;
		this.systemErrReplaceOption = systemErrReplaceOption;
		this.printStacktraceReplaceOption = printStacktraceReplaceOption;
		this.systemOutPrintExceptionReplaceOption = systemOutPrintExceptionReplaceOption;
		this.systemErrPrintExceptionReplaceOption = systemErrPrintExceptionReplaceOption;
		this.newLoggingStatementOption = newLoggingStatementOption;
	}

	public String getSystemOutReplaceOption() {
		return systemOutReplaceOption;
	}

	public void setSystemOutReplaceOption(String systemOutReplaceOption) {
		this.systemOutReplaceOption = systemOutReplaceOption;
	}

	public String getSystemErrReplaceOption() {
		return systemErrReplaceOption;
	}

	public void setSystemErrReplaceOption(String systemErrReplaceOption) {
		this.systemErrReplaceOption = systemErrReplaceOption;
	}

	public String getPrintStacktraceReplaceOption() {
		return printStacktraceReplaceOption;
	}

	public void setPrintStacktraceReplaceOption(String printStacktraceReplaceOption) {
		this.printStacktraceReplaceOption = printStacktraceReplaceOption;
	}

	public String getSystemOutPrintExceptionReplaceOption() {
		return systemOutPrintExceptionReplaceOption;
	}

	public void setSystemOutPrintExceptionReplaceOption(String systemOutPrintExceptionReplaceOption) {
		this.systemOutPrintExceptionReplaceOption = systemOutPrintExceptionReplaceOption;
	}

	public String getSystemErrPrintExceptionReplaceOption() {
		return systemErrPrintExceptionReplaceOption;
	}

	public void setSystemErrPrintExceptionReplaceOption(String systemErrPrintExceptionReplaceOption) {
		this.systemErrPrintExceptionReplaceOption = systemErrPrintExceptionReplaceOption;
	}

	public boolean getNewLoggingStatementOption() {
		return newLoggingStatementOption;
	}

	public void setNewLoggingStatementOption(boolean newLoggingStatementOption) {
		this.newLoggingStatementOption = newLoggingStatementOption;
	}

}

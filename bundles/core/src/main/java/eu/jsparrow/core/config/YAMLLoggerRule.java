package eu.jsparrow.core.config;

import eu.jsparrow.core.rule.impl.logger.LogLevelEnum;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;

/**
 * Model class for {@link StandardLoggerRule} YAML data.
 * 
 * @since 2.6.0
 *
 */
public class YAMLLoggerRule {

	private LogLevelEnum systemOutReplaceOption;
	private LogLevelEnum systemErrReplaceOption;
	private LogLevelEnum printStacktraceReplaceOption;
	private LogLevelEnum systemOutPrintExceptionReplaceOption;
	private LogLevelEnum systemErrPrintExceptionReplaceOption;
	private LogLevelEnum addMissingLoggingStatement;
	private Boolean attachExceptionObject;

	/**
	 * Default constructor
	 */
	public YAMLLoggerRule() {
		this.systemOutReplaceOption = LogLevelEnum.INFO;
		this.systemErrReplaceOption = LogLevelEnum.ERROR;
		this.printStacktraceReplaceOption = LogLevelEnum.ERROR;
		this.systemOutPrintExceptionReplaceOption = LogLevelEnum.INFO;
		this.systemErrPrintExceptionReplaceOption = LogLevelEnum.ERROR;
		this.addMissingLoggingStatement = LogLevelEnum.ERROR;
		this.attachExceptionObject = true;
	}

	public YAMLLoggerRule(LogLevelEnum systemOutReplaceOption, LogLevelEnum systemErrReplaceOption,
			LogLevelEnum printStacktraceReplaceOption, LogLevelEnum systemOutPrintExceptionReplaceOption,
			LogLevelEnum systemErrPrintExceptionReplaceOption, LogLevelEnum addMissingLoggingStatement,
			boolean attachExceptionObject) {
		this.systemOutReplaceOption = systemOutReplaceOption;
		this.systemErrReplaceOption = systemErrReplaceOption;
		this.printStacktraceReplaceOption = printStacktraceReplaceOption;
		this.systemOutPrintExceptionReplaceOption = systemOutPrintExceptionReplaceOption;
		this.systemErrPrintExceptionReplaceOption = systemErrPrintExceptionReplaceOption;
		this.addMissingLoggingStatement = addMissingLoggingStatement;
		this.attachExceptionObject = attachExceptionObject;
	}

	public LogLevelEnum getSystemOutReplaceOption() {
		return systemOutReplaceOption;
	}

	public void setSystemOutReplaceOption(LogLevelEnum systemOutReplaceOption) {
		this.systemOutReplaceOption = systemOutReplaceOption;
	}

	public LogLevelEnum getSystemErrReplaceOption() {
		return systemErrReplaceOption;
	}

	public void setSystemErrReplaceOption(LogLevelEnum systemErrReplaceOption) {
		this.systemErrReplaceOption = systemErrReplaceOption;
	}

	public LogLevelEnum getPrintStacktraceReplaceOption() {
		return printStacktraceReplaceOption;
	}

	public void setPrintStacktraceReplaceOption(LogLevelEnum printStacktraceReplaceOption) {
		this.printStacktraceReplaceOption = printStacktraceReplaceOption;
	}

	public LogLevelEnum getSystemOutPrintExceptionReplaceOption() {
		return systemOutPrintExceptionReplaceOption;
	}

	public void setSystemOutPrintExceptionReplaceOption(LogLevelEnum systemOutPrintExceptionReplaceOption) {
		this.systemOutPrintExceptionReplaceOption = systemOutPrintExceptionReplaceOption;
	}

	public LogLevelEnum getSystemErrPrintExceptionReplaceOption() {
		return systemErrPrintExceptionReplaceOption;
	}

	public void setSystemErrPrintExceptionReplaceOption(LogLevelEnum systemErrPrintExceptionReplaceOption) {
		this.systemErrPrintExceptionReplaceOption = systemErrPrintExceptionReplaceOption;
	}

	public LogLevelEnum getAddMissingLoggingStatement() {
		return addMissingLoggingStatement;
	}

	public void setAddMissingLoggingStatement(LogLevelEnum addMissingLoggingStatement) {
		this.addMissingLoggingStatement = addMissingLoggingStatement;
	}

	public Boolean getAttachExceptionObject() {
		return attachExceptionObject;
	}

	public void setAttachExceptionObject(boolean attachExceptionObject) {
		this.attachExceptionObject = attachExceptionObject;
	}

	@Override
	@SuppressWarnings("nls")
	public String toString() {
		return "YAMLLoggerRule [systemOutReplaceOption=" + systemOutReplaceOption + ", systemErrReplaceOption="
				+ systemErrReplaceOption + ", printStacktraceReplaceOption=" + printStacktraceReplaceOption
				+ ", systemOutPrintExceptionReplaceOption=" + systemOutPrintExceptionReplaceOption
				+ ", systemErrPrintExceptionReplaceOption=" + systemErrPrintExceptionReplaceOption
				+ ", addMissingLoggingStatement=" + addMissingLoggingStatement + ", attachExceptionObject="
				+ attachExceptionObject + "]";
	}
}

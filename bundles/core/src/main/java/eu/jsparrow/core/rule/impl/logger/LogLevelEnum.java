package eu.jsparrow.core.rule.impl.logger;

/**
 * Enum values used in {@link StandardLoggerRule} for log level. Used also for
 * YAML configuration of {@link StandardLoggerRule}
 *
 */
public enum LogLevelEnum {

	TRACE("trace"),
	DEBUG("debug"),
	INFO("info"),
	WARN("warn"),
	ERROR("error"),
	LEAVE("");

	private String logLevel;

	private LogLevelEnum(String logLevel) {
		this.logLevel = logLevel;
	}

	public String getLogLevel() {
		return logLevel;
	}
}

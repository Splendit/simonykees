package eu.jsparrow.core.rule.impl.logger;

/**
 * Enum values used in {@link StandardLoggerRule} for log level. Used also for
 * YAML configuration of {@link StandardLoggerRule}
 *
 * @since 2.6.0
 */
public enum LogLevelEnum {

	TRACE("trace"), //$NON-NLS-1$
	DEBUG("debug"), //$NON-NLS-1$
	INFO("info"), //$NON-NLS-1$
	WARN("warn"), //$NON-NLS-1$
	ERROR("error"), //$NON-NLS-1$
	LEAVE(""); //$NON-NLS-1$

	private String logLevel;

	private LogLevelEnum(String logLevel) {
		this.logLevel = logLevel;
	}

	public String getLogLevel() {
		return logLevel;
	}
}

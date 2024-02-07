package eu.jsparrow.logging.service.api;

public interface IJSparrowLogger {

	String getName();

	boolean isWarnEnabled();

	boolean isTraceEnabled();

	boolean isInfoEnabled();

	boolean isErrorEnabled();

	boolean isDebugEnabled();

	void debug(String arg0, Object arg1, Object arg2);

	void debug(String arg0, Object... arg1);

	void debug(String arg0, Object arg1);

	void debug(String arg0, Throwable arg1);

	void debug(String arg0);

	void error(String arg0);

	void error(String arg0, Throwable arg1);

	void error(String arg0, Object arg1);

	void error(String arg0, Object... arg1);

	void error(String arg0, Object arg1, Object arg2);

	void info(String arg0, Object arg1, Object arg2);

	void info(String arg0, Object... arg1);

	void info(String arg0, Throwable arg1);

	void info(String arg0, Object arg1);

	void info(String arg0);

	void warn(String arg0);

	void warn(String arg0, Throwable arg1);

	void warn(String arg0, Object arg1);

	void warn(String arg0, Object... arg1);

	void warn(String arg0, Object arg1, Object arg2);

	void trace(String arg0, Object arg1, Object arg2);

	void trace(String arg0, Object... arg1);

	void trace(String arg0, Object arg1);

	void trace(String arg0, Throwable arg1);

	void trace(String arg0);
}

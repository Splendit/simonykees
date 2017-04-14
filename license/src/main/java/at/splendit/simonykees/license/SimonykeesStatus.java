package at.splendit.simonykees.license;

import org.eclipse.core.runtime.Status;

public class SimonykeesStatus extends Status {
	private StackTraceElement caller;

	public SimonykeesStatus(int severity, String pluginId, String message) {
		super(severity, pluginId, message);
		initCaller();
	}

	public SimonykeesStatus(int severity, String pluginId, String message, Throwable exception) {
		super(severity, pluginId, message, exception);
		initCaller();
	}

	public SimonykeesStatus(int severity, String pluginId, int code, String message, Throwable exception) {
		super(severity, pluginId, code, message, exception);
		initCaller();
	}

	@Override
	public String getMessage() {
		return String.format("%s%n%s.%s (%s:%d)%n%s", getSeverityLevel(), caller.getClassName(), caller.getMethodName(), //$NON-NLS-1$
				caller.getFileName(), caller.getLineNumber(), super.getMessage());
	}

	/**
	 * Black magic
	 */
	private void initCaller() {
		caller = Thread.currentThread().getStackTrace()[4];
	}

	private String getSeverityLevel() {
		switch (super.getSeverity()) {
		case 1:
			return "INFO"; //$NON-NLS-1$
		case 2:
			return "WARNING"; //$NON-NLS-1$
		case 4:
			return "ERROR"; //$NON-NLS-1$
		default:
			return "UNKNOWN"; //$NON-NLS-1$
		}
	}
}

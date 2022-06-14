package eu.jsparrow.jdt.ls.core.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


public final class StatusFactory {

	public static final IStatus UNSUPPORTED_PROJECT = newErrorStatus("Unsupported Java project");

	private StatusFactory() {}

	public static IStatus newErrorStatus(String message) {
		return newErrorStatus(message, null);
	}

	public static IStatus newErrorStatus(String message, Throwable exception) {
		return new Status(IStatus.ERROR, IConstants.PLUGIN_ID, message, exception);
	}
}

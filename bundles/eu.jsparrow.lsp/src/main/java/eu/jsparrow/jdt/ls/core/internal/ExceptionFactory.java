package eu.jsparrow.jdt.ls.core.internal;

import org.eclipse.core.runtime.CoreException;
import eu.jsparrow.jdt.ls.core.internal.StatusFactory;

public class ExceptionFactory {

	private ExceptionFactory() {
	}

	public static CoreException newException(String message) {
		return new CoreException(StatusFactory.newErrorStatus(message));
	}

	public static CoreException newException(Throwable e) {
		return new CoreException(StatusFactory.newErrorStatus(e.getMessage(), e));
	}

}
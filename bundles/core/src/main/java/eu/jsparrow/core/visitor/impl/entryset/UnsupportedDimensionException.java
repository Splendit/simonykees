package eu.jsparrow.core.visitor.impl.entryset;

import eu.jsparrow.rules.common.exception.SimonykeesException;

public class UnsupportedDimensionException extends SimonykeesException {

	private static final long serialVersionUID = -1839601468104761621L;

	public UnsupportedDimensionException(String message) {
		super(message);
	}
}

package eu.jsparrow.core.exception.visitor;

import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.exception.SimonykeesException;

/**
 * Indicates that an {@link ITypeBinding} cannot be resolved or found.
 * 
 * @since 4.8.0
 *
 */
public class UnexpectedKindOfBindingException extends SimonykeesException {

	private static final long serialVersionUID = 917395804661919739L;

	public UnexpectedKindOfBindingException(String message) {
		super(message);
	}
}

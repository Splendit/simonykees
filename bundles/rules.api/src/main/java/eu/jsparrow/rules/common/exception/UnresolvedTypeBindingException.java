package eu.jsparrow.rules.common.exception;

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Indicates that an {@link ITypeBinding} cannot be resolved or found.
 * 
 * @since 4.8.0
 *
 */
public class UnresolvedTypeBindingException extends UnresolvedBindingException {

	private static final long serialVersionUID = 917395804661919739L;

	public UnresolvedTypeBindingException(String message) {
		super(message);
	}
}

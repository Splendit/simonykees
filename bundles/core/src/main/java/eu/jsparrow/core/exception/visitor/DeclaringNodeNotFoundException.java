package eu.jsparrow.core.exception.visitor;

import org.eclipse.jdt.core.dom.IBinding;

import eu.jsparrow.rules.common.exception.SimonykeesException;

/**
 * Indicates that for a specified {@link IBinding} no declaring node could be
 * found in the given compilation unit.
 * 
 * @since 4.9.0
 *
 */
public class DeclaringNodeNotFoundException extends SimonykeesException {

	private static final long serialVersionUID = 4922718320518098866L;

	public DeclaringNodeNotFoundException(String message) {
		super(message);
	}
}

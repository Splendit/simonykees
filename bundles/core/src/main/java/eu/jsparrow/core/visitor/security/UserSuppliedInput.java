package eu.jsparrow.core.visitor.security;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * Intended to be used as input when creating instances of
 * {@link ReplaceableParameter} in combination with a setter name and a
 * parameter position.
 * 
 * @since 3.19.0
 *
 */
class UserSuppliedInput {
	private final StringLiteral previous;
	private final Expression input;
	private final StringLiteral next;

	public UserSuppliedInput(StringLiteral previous, Expression input, StringLiteral next) {
		super();
		this.previous = previous;
		this.input = input;
		this.next = next;
	}

	public StringLiteral getPrevious() {
		return previous;
	}

	public Expression getInput() {
		return input;
	}

	public StringLiteral getNext() {
		return next;
	}
}
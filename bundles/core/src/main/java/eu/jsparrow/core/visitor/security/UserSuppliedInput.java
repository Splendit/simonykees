package eu.jsparrow.core.visitor.security;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

class UserSuppliedInput {
	final StringLiteral previous;
	final Expression input;
	final StringLiteral next;

	public UserSuppliedInput(StringLiteral previous,  Expression input, StringLiteral next) {
		super();
		this.previous = previous;
		this.input = input;
		this.next = next;
	}

}
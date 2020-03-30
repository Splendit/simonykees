package eu.jsparrow.core.visitor.security;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

public class ReplaceableParameter {

	private StringLiteral previous;
	private StringLiteral next;
	private Expression parameter;
	private String setterName;
	private int position;

	public ReplaceableParameter(StringLiteral previous, StringLiteral next, Expression parameter, String setterName,
			int position) {
		this.previous = previous;
		this.next = next;
		this.parameter = parameter;
		this.setterName = setterName;
		this.position = position;
	}

	public StringLiteral getPrevious() {
		return previous;
	}

	public StringLiteral getNext() {
		return next;
	}

	public Expression getParameter() {
		return parameter;
	}

	public String getSetterName() {
		return setterName;
	}

	public int getPosition() {
		return position;
	}

}

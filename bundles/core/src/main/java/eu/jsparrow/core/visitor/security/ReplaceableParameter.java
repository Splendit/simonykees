package eu.jsparrow.core.visitor.security;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

public class ReplaceableParameter {
	
	private StringLiteral previous;
	private StringLiteral next;
	private Expression parameter;
	public ReplaceableParameter(StringLiteral previous, StringLiteral next, Expression parameter) {
		super();
		this.previous = previous;
		this.next = next;
		this.parameter = parameter;
	}
	public StringLiteral getPrevious() {
		return previous;
	}
	public void setPrevious(StringLiteral previous) {
		this.previous = previous;
	}
	public StringLiteral getNext() {
		return next;
	}
	public void setNext(StringLiteral next) {
		this.next = next;
	}
	public Expression getParameter() {
		return parameter;
	}
	public void setParameter(Expression parameter) {
		this.parameter = parameter;
	}
	
	

}

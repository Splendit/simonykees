package eu.jsparrow.core.visitor.security;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * A simple POJO for wrapping together the components of a dynamic query that
 * are affected by replacing one component with a parameter on a
 * {@link PreparedStatement}.
 * 
 * @since 3.16.0
 *
 */
public class ReplaceableParameter {

	private StringLiteral previous;
	private StringLiteral next;
	private Expression parameter;
	private String setterName;
	private int position;

	/**
	 * 
	 * @param previous
	 *            string literal preceding the component to be replaced
	 * @param next
	 *            string literal succeeding the component to be replaced
	 * @param parameter
	 *            the component to be replaced with a parameter
	 * @param setterName
	 *            the parameter's setter method
	 * @param position
	 *            the parameter's index
	 */
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

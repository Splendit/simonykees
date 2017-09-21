package eu.jsparrow.core.util;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.InfixExpression;

/**
 * A utility class for expressions containing arithmetic operations and assignments. 
 * 
 * @author Martin Huter
 * @since 0.9
 */
public class ArithmeticUtil {

	/**
	 *  Generates a corresponding arithmetic assignment operator to an arithmetic operator.
	 *  Works only for the four base arithmetic operations.
	 *  Throws UnsupportedOperationException if other type is inserted
	 *  + transforms to +=
	 *  - transforms to -=
	 *  * transforms to *=
	 *  / transforms to /=
	 * @param infixExpressionOperator is an InfixExpression.Operator that is converted
	 * @return returns an Assignment.Operator that is corresponding to the given {@link InfixExpression.Operator}
	 */
	public static Assignment.Operator generateOperator(InfixExpression.Operator infixExpressionOperator) {
		if (InfixExpression.Operator.PLUS.equals(infixExpressionOperator)) {
			return Assignment.Operator.PLUS_ASSIGN;
		} else if (InfixExpression.Operator.MINUS.equals(infixExpressionOperator)) {
			return Assignment.Operator.MINUS_ASSIGN;
		} else if (InfixExpression.Operator.TIMES.equals(infixExpressionOperator)) {
			return Assignment.Operator.TIMES_ASSIGN;
		} else if (InfixExpression.Operator.DIVIDE.equals(infixExpressionOperator)) {
			return Assignment.Operator.DIVIDE_ASSIGN;
		}
		
		throw new UnsupportedOperationException();
	}

}

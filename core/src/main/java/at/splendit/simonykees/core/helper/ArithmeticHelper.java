package at.splendit.simonykees.core.helper;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;

public class ArithmeticHelper {

	public static void extractSimpleName(SimpleName simpleName, InfixExpression infixExpression,
			MutablePair<InfixExpression, Expression> result) {

		String varName = simpleName.getIdentifier();
		Expression infixLeftOperand = infixExpression.getLeftOperand();
		Expression infixRightOperand = infixExpression.getRightOperand();

		/**
		 * * InfixOperator:<code> <b>*</b> TIMES <b>/</b> DIVIDE <b>%</b>
		 * REMAINDER <b>+</b> PLUS <b>-</b> MINUS
		 */
		InfixExpression.Operator currentOperator = infixExpression.getOperator();

		if (InfixExpression.Operator.PLUS.equals(currentOperator) || 
				InfixExpression.Operator.MINUS.equals(currentOperator)) {
			if (infixLeftOperand instanceof SimpleName) {
				SimpleName simpleLeftOperand = (SimpleName) infixLeftOperand;
				if (simpleLeftOperand.getIdentifier().equals(varName)) {
					result.setLeft(infixExpression);
					result.setRight(infixRightOperand);
					return;
				}
			} else if (infixLeftOperand instanceof InfixExpression) {
				// TODO go deeper
				throw new RuntimeException("NotYetImplemented");
			}
			//Other Types of nodes are not relevant for this use case 
			return;
		} else if (InfixExpression.Operator.DIVIDE.equals(currentOperator) ||
				InfixExpression.Operator.TIMES.equals(currentOperator)) {
			throw new RuntimeException("NotYetImplemented");
		}
		return;
	}
	
	

	public static Pair<InfixExpression, Expression> extractSimpleName(SimpleName simpleName,
			InfixExpression infixExpression) {
		MutablePair<InfixExpression, Expression> result = new MutablePair<>();
		extractSimpleName(simpleName, infixExpression, result);
		return result;
	}

}

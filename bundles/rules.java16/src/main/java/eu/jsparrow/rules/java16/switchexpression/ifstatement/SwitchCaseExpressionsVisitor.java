package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Helper visitor which tries to extract expressions from a node representing
 * the condition of an if statement as case expressions in connection with the
 * transformation of a multi-branch if statement to a switch expression or to a
 * switch statement.
 * <p>
 * For example, <br>
 * {@code if (value == 3 || value == 4 || value == 5)} <br>
 * is an if condition where it is possible to extract the values 3, 4, and 5
 * which can be transformed to the combined rule-labeled <br>
 * {@code case 3,4,5 ->} <br>
 * in the switch replacing the given if statement.
 * 
 * @since 4.13.0
 */
public class SwitchCaseExpressionsVisitor extends AbstractIfExpressionVisitor {
	private static final ASTMatcher AST_MATCHER = new ASTMatcher();
	final SimpleName expectedSwitchHeaderExpression;
	final ITypeBinding expectedOperandType;
	private final List<Expression> caseExpressions = new ArrayList<>();
	private final UniqueLiteralValueStore uniqueLiteralValueStore;

	private boolean isSupportedCharacter(Expression caseExpression) {
		if (caseExpression.getNodeType() != ASTNode.CHARACTER_LITERAL) {
			return false;
		}
		CharacterLiteral characterLiteral = (CharacterLiteral) caseExpression;
		Character characterValue = Character.valueOf(characterLiteral.charValue());
		return uniqueLiteralValueStore.isUnique(characterValue);
	}

	private boolean isSupportedInteger(Expression caseExpression) {
		try {
			Integer integerValue = ExpressionToConstantValue.extractIntegerConstant(caseExpression)
				.orElse(null);
			return integerValue != null && uniqueLiteralValueStore.isUnique(integerValue);
		} catch (NumberFormatException exc) {
			return false;
		}
	}

	private boolean isSupportedString(Expression caseExpression) {
		if (caseExpression.getNodeType() != ASTNode.STRING_LITERAL) {
			return false;
		}
		StringLiteral stringLiteral = (StringLiteral) caseExpression;
		return uniqueLiteralValueStore.isUnique(stringLiteral.getLiteralValue());
	}

	private boolean isValidEqualsMethodCaseExpression(Expression assumedCaseExpression) {
		if (ClassRelationUtil.isContentOfType(expectedOperandType, java.lang.String.class.getName())) {
			return isSupportedString(assumedCaseExpression);
		}
		return false;
	}

	private boolean isValidEqualsInfixCaseExpression(Expression assumedCaseExpression) {
		if (ClassRelationUtil.isContentOfType(expectedOperandType, char.class.getName())) {
			return isSupportedCharacter(assumedCaseExpression);
		}
		if (ClassRelationUtil.isContentOfType(expectedOperandType, int.class.getName())) {
			return isSupportedInteger(assumedCaseExpression);
		}
		return false;
	}

	public SwitchCaseExpressionsVisitor(SimpleName expectedSwitchHeaderExpression,
			ITypeBinding expectedOperandType, UniqueLiteralValueStore uniqueLiteralValueStore) {
		this.expectedSwitchHeaderExpression = expectedSwitchHeaderExpression;
		this.expectedOperandType = expectedOperandType;
		this.uniqueLiteralValueStore = uniqueLiteralValueStore;
	}

	public List<Expression> getCaseExpressions() {
		if (isUnexpectedNode()) {
			return Collections.emptyList();
		}
		return caseExpressions;
	}

	@Override
	protected boolean analyzeEqualsInfixOperands(Expression leftOperand, Expression rightOperand) {
		Expression assumedCaseExpression;
		if (AST_MATCHER.match(expectedSwitchHeaderExpression, leftOperand)) {
			assumedCaseExpression = rightOperand;
		} else if (AST_MATCHER.match(expectedSwitchHeaderExpression, rightOperand)) {
			assumedCaseExpression = leftOperand;
		} else {
			return false;
		}

		if (isValidEqualsInfixCaseExpression(assumedCaseExpression)) {
			caseExpressions.add(assumedCaseExpression);
			return true;
		}
		return false;
	}

	@Override
	protected boolean analyzeEqualsMethodOperands(Expression equalsInvocationExpression,
			Expression equalsInvocationArgument) {
		Expression assumedCaseExpression;
		if (AST_MATCHER.match(expectedSwitchHeaderExpression, equalsInvocationExpression)) {
			assumedCaseExpression = equalsInvocationArgument;
		} else if (AST_MATCHER.match(expectedSwitchHeaderExpression, equalsInvocationArgument)) {
			assumedCaseExpression = equalsInvocationExpression;
		} else {
			return false;
		}

		if (isValidEqualsMethodCaseExpression(assumedCaseExpression)) {
			caseExpressions.add(assumedCaseExpression);
			return true;
		}
		return false;
	}
}

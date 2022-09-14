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
 *
 */
class EqualsOperationForSwitchVisitor extends AbstractIfExpressionVisitor {
	private static final ASTMatcher AST_MATCHER = new ASTMatcher();
	final SimpleName expectedSwitchHeaderExpression;
	final ITypeBinding expectedOperandType;
	private final List<Expression> caseExpressions = new ArrayList<>();
	private final UniqueLiteralValues uniqueLiteralValues = new UniqueLiteralValues();

	boolean isSupportedCharacter(Expression caseExpression) {
		if (caseExpression.getNodeType() != ASTNode.CHARACTER_LITERAL) {
			return false;
		}
		CharacterLiteral characterLiteral = (CharacterLiteral) caseExpression;
		Character characterValue = Character.valueOf(characterLiteral.charValue());
		return uniqueLiteralValues.isUnique(characterValue);
	}

	boolean isSupportedInteger(Expression caseExpression) {
		Integer integerValue;
		try {
			integerValue = ExpressionToNumericToken.expressionToInteger(caseExpression)
				.orElse(null);
		} catch (NumberFormatException exc) {
			integerValue = null;
		}
		if (integerValue != null && uniqueLiteralValues.isUnique(integerValue)) {
			return true;
		}
		return false;
	}

	boolean isSupportedString(Expression caseExpression) {
		if (caseExpression.getNodeType() != ASTNode.STRING_LITERAL) {
			return false;
		}
		StringLiteral stringLiteral = (StringLiteral) caseExpression;
		return uniqueLiteralValues.isUnique(stringLiteral.getLiteralValue());
	}

	// Optional<Expression> findCaseExpression(Expression assumedCaseExpression)
	// {
	//
	// Optional<Expression> optionalReturnValue =
	// Optional.of(assumedCaseExpression);
	// int parentNodeType = assumedCaseExpression.getParent()
	// .getNodeType();
	// if (parentNodeType == ASTNode.INFIX_EXPRESSION) {
	//
	// if (ClassRelationUtil.isContentOfType(expectedOperandType,
	// char.class.getName())) {
	// return optionalReturnValue.filter(expression ->
	// isSupportedCharacter(expression));
	// }
	// if (ClassRelationUtil.isContentOfType(expectedOperandType,
	// int.class.getName())) {
	// return optionalReturnValue.filter(expression ->
	// isSupportedInteger(expression));
	// }
	// }
	// if (parentNodeType == ASTNode.METHOD_INVOCATION &&
	// ClassRelationUtil.isContentOfType(expectedOperandType,
	// java.lang.String.class.getName())) {
	// return optionalReturnValue.filter(expression ->
	// isSupportedString(expression));
	// }
	// return Optional.empty();
	// }

	boolean isValidEqualsMethodCaseExpression(Expression assumedCaseExpression) {
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

	public EqualsOperationForSwitchVisitor(SimpleName expectedSwitchHeaderExpression,
			ITypeBinding expectedOperandType) {
		this.expectedSwitchHeaderExpression = expectedSwitchHeaderExpression;
		this.expectedOperandType = expectedOperandType;
	}

	// @Override
	// protected boolean
	// analyzeEqualsOperationForSwitch(EqualsOperationForSwitch equalsOperation)
	// {
	// SimpleName simpleNameFound = equalsOperation.getSwitchHeaderExpression();
	// if (!AST_MATCHER.match(expectedSwitchHeaderExpression, simpleNameFound))
	// {
	// return false;
	// }
	// Expression assumedCaseExpression = equalsOperation.getCaseExpression();
	// Expression caseExpression = findCaseExpression(assumedCaseExpression)
	// .orElse(null);
	// if (caseExpression == null) {
	// return false;
	// }
	// caseExpressions.add(caseExpression);
	// return true;
	// }

	public List<Expression> getCaseExpressions() {
		if (isUnexpectedNode()) {
			return Collections.emptyList();
		}
		return caseExpressions;
	}

	@Override
	protected boolean analyzeEqualsInfixOperationForSwitch(Expression leftOperand, Expression rightOperand) {
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
	protected boolean analyzeEqualsMethodInvocation(Expression equalsInvocationExpression,
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

package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
	private final List<EqualsOperationForSwitch> equalsOperations = new ArrayList<>();
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

	Optional<Expression> findCaseExpression(ITypeBinding expectedOperandType,
			EqualsOperationForSwitch equalsOperation) {

		Optional<Expression> optionalReturnValue = Optional.of(equalsOperation.getCaseExpression());
		if (equalsOperation.getOperationNodeType() == ASTNode.INFIX_EXPRESSION) {

			if (ClassRelationUtil.isContentOfType(expectedOperandType, char.class.getName())) {
				return optionalReturnValue.filter(expression -> isSupportedCharacter(expression));
			}
			if (ClassRelationUtil.isContentOfType(expectedOperandType, int.class.getName())) {
				return optionalReturnValue.filter(expression -> isSupportedInteger(expression));
			}
		}
		if (equalsOperation.getOperationNodeType() == ASTNode.METHOD_INVOCATION &&
				ClassRelationUtil.isContentOfType(expectedOperandType, java.lang.String.class.getName())) {
			return optionalReturnValue.filter(expression -> isSupportedString(expression));
		}
		return Optional.empty();
	}

	public EqualsOperationForSwitchVisitor(SimpleName expectedSwitchHeaderExpression,
			ITypeBinding expectedOperandType) {
		this.expectedSwitchHeaderExpression = expectedSwitchHeaderExpression;
		this.expectedOperandType = expectedOperandType;
	}

	@Override
	protected boolean analyzeEqualsOperationForSwitch(EqualsOperationForSwitch equalsOperation) {
		equalsOperations.add(equalsOperation);
		SimpleName simpleNameFound = equalsOperation.getSwitchHeaderExpression();
		if (!AST_MATCHER.match(expectedSwitchHeaderExpression, simpleNameFound)) {
			return false;
		}
		Expression caseExpression = findCaseExpression(expectedOperandType, equalsOperation)
			.orElse(null);
		if (caseExpression == null) {
			return false;
		}
		caseExpressions.add(caseExpression);
		return true;
	}

	public List<EqualsOperationForSwitch> getEqualsOperations() {
		return equalsOperations;
	}

	public List<Expression> getCaseExpressions() {
		if(isUnexpectedNode()) {
			return Collections.emptyList();
		}
		return caseExpressions;
	}

}

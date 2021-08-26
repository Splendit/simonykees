package eu.jsparrow.rules.java16.textblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Helper class collecting the string representations of the components of a
 * string concatenation.
 * 
 * @since 4.3.0
 *
 */
@SuppressWarnings("nls")
public class ConcatenationComponentsCollector {

	private final Predicate<String> SUPPORTED_NUMERIC_LITERAL_PREDICATE = Pattern.compile("^(0|([1-9][0-9]*))$")
		.asPredicate();

	/**
	 * @return a list of strings representing all the component(s) of a String
	 *         concatenation expression specified by the the
	 *         {@link InfixExpression}. An empty list s returned as soon as not
	 *         all components can be represented in a valid way, indicating that
	 *         the given {@link InfixExpression} cannot be transformed to a
	 *         {@code TextBlock}.
	 */
	public List<String> collectConcatenationComponents(InfixExpression infixExpression) {
		Operator operator = infixExpression.getOperator();
		if (operator != InfixExpression.Operator.PLUS) {
			return Collections.emptyList();
		}

		Expression left = infixExpression.getLeftOperand();
		Expression right = infixExpression.getRightOperand();
		if (!isStringOperand(left) && !isStringOperand(right)) {
			return Collections.emptyList();
		}
		List<String> leftOperandComponents = collectComponents(left);
		if (leftOperandComponents.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> rightOperandComponents = collectComponents(right);
		if (rightOperandComponents.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> componentList = new ArrayList<>();
		componentList.addAll(leftOperandComponents);
		componentList.addAll(rightOperandComponents);
		if (infixExpression.hasExtendedOperands()) {
			List<Expression> extendedOperands = ASTNodeUtil
				.convertToTypedList(infixExpression.extendedOperands(), Expression.class);
			for (Expression operand : extendedOperands) {
				List<String> operandComponents = collectComponents(operand);
				if (operandComponents.isEmpty()) {
					return Collections.emptyList();
				}
				componentList.addAll(operandComponents);
			}
		}
		return componentList;
	}

	private List<String> collectComponents(Expression expression) {
		Expression unwrappedExpression = removeSurroundingParenthesis(expression);
		if (unwrappedExpression.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			return collectConcatenationComponents((InfixExpression) unwrappedExpression);
		}
		String componentAsString = literalComponentToString(unwrappedExpression).orElse(null);
		if (componentAsString == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(componentAsString);
	}

	private Optional<String> literalComponentToString(Expression component) {

		if (component.getNodeType() == ASTNode.STRING_LITERAL) {
			StringLiteral stringLiteral = (StringLiteral) component;
			return Optional.of(stringLiteral.getLiteralValue());
		}

		if (component.getNodeType() == ASTNode.NUMBER_LITERAL) {
			NumberLiteral numberLiteral = (NumberLiteral) component;
			String numericToken = numberLiteral.getToken();
			if (SUPPORTED_NUMERIC_LITERAL_PREDICATE.test(numericToken)) {
				return Optional.of(numericToken);
			}
			return Optional.empty();
		}

		if (component.getNodeType() == ASTNode.CHARACTER_LITERAL) {
			CharacterLiteral characterLiteral = (CharacterLiteral) component;
			return Optional.of(Character.toString(characterLiteral.charValue()));
		}

		if (component.getNodeType() == ASTNode.BOOLEAN_LITERAL) {
			BooleanLiteral booleanLiteral = (BooleanLiteral) component;
			return Optional.of(Boolean.toString(booleanLiteral.booleanValue()));

		}

		if (component.getNodeType() == ASTNode.NULL_LITERAL) {
			return Optional.of("null"); //$NON-NLS-1$
		}

		return Optional.empty();
	}

	private Expression removeSurroundingParenthesis(Expression expression) {
		if (expression.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression;
			return removeSurroundingParenthesis(parenthesizedExpression.getExpression());
		}
		return expression;
	}

	private boolean isStringOperand(Expression expression) {
		return ClassRelationUtil.isContentOfType(expression.resolveTypeBinding(), String.class.getName());
	}
}

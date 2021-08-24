package eu.jsparrow.rules.java16.textblock;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Prototype without any validation.
 * <p>
 * Copied from
 * {@code eu.jsparrow.core.visitor.security.DynamicQueryComponentsStore},
 * renamed and modified.
 * 
 * @since 3.18.0
 *
 */
public class StringConcatenationComponentsStore {

	private List<Expression> components = new ArrayList<>();

	/**
	 * Recursive Method dividing a {@link String} concatenation expression into
	 * its components and storing them in a list.
	 * 
	 * @param initializer
	 *            expected to be a {@link String} concatenation expression.
	 */
	public void storeComponents(Expression initializer) {
		if (initializer.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			InfixExpression infixExpression = (InfixExpression) initializer;
			Expression left = infixExpression.getLeftOperand();
			storeComponents(left);
			Expression right = infixExpression.getRightOperand();
			storeComponents(right);
			if (infixExpression.hasExtendedOperands()) {
				List<Expression> extendedOperands = ASTNodeUtil
					.convertToTypedList(infixExpression.extendedOperands(), Expression.class);
				extendedOperands.forEach(this::storeComponents);
			}
		} else {
			components.add(initializer);
		}
	}

	public List<Expression> getComponents() {
		return components;
	}

}

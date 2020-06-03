package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * @since 3.18.0
 */
public class AvoidEvaluationOfParametersInLoggingMessagesASTVisitor extends AbstractASTRewriteASTVisitor {

	@SuppressWarnings("nls")
	private static final List<String> LOGGER_TYPES = Collections.unmodifiableList(
			Arrays.asList("org.slf4j.Logger", "org.apache.logging.log4j.Logger", "ch.qos.logback.classic.Logger"));

	private static final String THROWABLE_TYPE = Throwable.class.getName();
	private static final List<String> THROWABLE_TYPES = Collections
		.unmodifiableList(Arrays.asList(THROWABLE_TYPE));

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		if (methodInvocation.getExpression() == null) {
			return true;
		}

		Expression expression = methodInvocation.getExpression();
		boolean isLoggerType = ClassRelationUtil.isContentOfTypes(expression.resolveTypeBinding(), LOGGER_TYPES);

		if (!isLoggerType) {
			return true;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (!argumentsAllowRefactoring(arguments)) {
			return true;
		}

		InfixExpression infix = (InfixExpression) arguments.get(0);

		boolean isLeftOperandStringLiteral = ASTNode.STRING_LITERAL == infix.getLeftOperand()
			.getNodeType();

		if (!isLeftOperandStringLiteral) {
			return true;
		}

		StringLiteral currentLiteral = (StringLiteral) infix.getLeftOperand();

		/*
		 * TODO this check needs to be more sophisticated. it needs to look at
		 * the complete infix expression. Example: logger.info("A " + 1 +
		 * " B {}", 2);
		 */
		boolean isStringLiteralWithoutArguments = !StringUtils.contains(currentLiteral.getLiteralValue(), "{}"); //$NON-NLS-1$

		/*
		 * TODO this has no real purpose yet. Something like that will be used though to make an example like this work:
		 * Pre:
		 * logger.info("A " + 1 + " B " + 2 + " C " + 3 + " D " + 4);
		 * Post (currently):
		 * logger.info("A {}{}{}{}{}{}{}",1," B ",2," C ",3," D ",4);
		 * Post (improved):
		 * logger.info("A {} B {} C {} D {}", 1, 2, 3, 4);
		 */
		boolean isRightOperandValid = isRightOperandAllowedType(infix.getRightOperand());

		if (!isStringLiteralWithoutArguments || !isRightOperandValid) {
			return true;
		}

		AST ast = astRewrite.getAST();

		List<Expression> newArguments = getNewArguments(infix);

		StringLiteral newLiteral = ast.newStringLiteral();
		// TODO check if the repeat has the right number when a Throwable arg is present
		newLiteral.setLiteralValue(currentLiteral.getLiteralValue() + StringUtils.repeat("{}", newArguments.size())); //$NON-NLS-1$

		astRewrite.replace(infix, newLiteral, null);

		ListRewrite listRewriter = astRewrite.getListRewrite(methodInvocation, MethodInvocation.ARGUMENTS_PROPERTY);

		for (Expression newArgument : newArguments) {
			listRewriter.insertLast(newArgument, null);
		}

		return true;
	}

	private List<Expression> getNewArguments(InfixExpression infix) {

		List<Expression> arguments = new ArrayList<>();

		arguments.add(infix.getRightOperand());
		arguments.addAll(infix.extendedOperands());

		return arguments;
	}

	/**
	 * We want an {@link InfixExpression} as first argument and optionally a
	 * {@link Throwable} as second argument (see
	 * http://slf4j.org/faq.html#paramException).
	 * <p/>
	 * Allowed examples:
	 * 
	 * <pre>
	 * <code>
	 * logger.info("Print " + message + " and " + otherMessage); // 1 argument (InfixExpression)
	 * logger.error("Failed to format " + s, e); // 2 arguments (InfixExpression + Throwable)
	 * </code>
	 * </pre>
	 * 
	 * @param arguments
	 *            {@link MethodInvocation} arguments
	 * @return true if the arguments allow refactoring, false otherwise
	 */
	private boolean argumentsAllowRefactoring(List<Expression> arguments) {

		// 2 arguments only allowed if the second one is a Throwable
		if (arguments.size() == 2) {
			boolean isSecondArgumentThrowableSubtype = ClassRelationUtil.isInheritingContentOfTypes(arguments.get(1)
				.resolveTypeBinding(),
					THROWABLE_TYPES);
			boolean isSecondArgumentThrowableType = ClassRelationUtil.isContentOfType(arguments.get(1)
				.resolveTypeBinding(),
					THROWABLE_TYPE);
			if (!isSecondArgumentThrowableSubtype && !isSecondArgumentThrowableType) {
				return false;
			}
		} else if (arguments.size() != 1) {
			return false;
		}

		boolean isFirstArgumentString = ClassRelationUtil.isContentOfType(arguments.get(0)
			.resolveTypeBinding(),
				String.class.getName());
		boolean isFirstArgumentInfix = (arguments.get(0)
			.getNodeType() == ASTNode.INFIX_EXPRESSION);

		if (!isFirstArgumentString || !isFirstArgumentInfix) {
			return false;
		}

		return true;
	}

	private boolean isRightOperandAllowedType(Expression expression) {
		int nodeType = expression.getNodeType();

		switch (nodeType) {

		case ASTNode.SIMPLE_NAME:
		case ASTNode.STRING_LITERAL:
		case ASTNode.NUMBER_LITERAL:
			return true;

		default:
			return false;
		}
	}

}

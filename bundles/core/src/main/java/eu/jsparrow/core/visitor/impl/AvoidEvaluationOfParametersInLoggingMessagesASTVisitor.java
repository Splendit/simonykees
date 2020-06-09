package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Concatenated arguments in a logging message lead to a concatenation even if
 * the logging level is too low to show a message. This leads to a needless
 * performance reduction and is classified as "Major" code smell by sonarcloud
 * (https://sonarcloud.io/organizations/default/rules?open=java%3AS2629&q=S2629).
 * <p/>
 * By using the built-in string formatting of loggers, an evaluation will only
 * happen when the logging level permits the message to be displayed.
 * <p/>
 * This visitor finds cases where an {@link InfixExpression} may be replaced
 * with logger arguments.
 * <p/>
 * Examples:
 * 
 * <pre>
 * <code>
 * // Pre:
 * logger.info("A " + 1 + " B " + 2);
 * // Post:
 * logger.info("A {} B {}", 1, 2);
 * 
 * // Pre:
 * logger.info("A " + 1 + " B " + 2 + " C " + 3 + " D " + 4 + " E", new Exception("5"));
 * // Post:
 * logger.info("A {} B {} C {} D {} E", 1, 2, 3, 4, new Exception("5"));
 * 
 * // Pre:
 * logger.info("bd: '" + BigDecimal.ONE + "'");
 * // Post:
 * logger.info("bd: '{}'", BigDecimal.ONE);
 * </code>
 * </pre>
 * 
 * @since 3.18.0
 * 
 *        Implemented in SIM-1744 (see ticket for additional comments).
 */
public class AvoidEvaluationOfParametersInLoggingMessagesASTVisitor extends AbstractASTRewriteASTVisitor {

	@SuppressWarnings("nls")
	private static final List<String> LOGGER_TYPES = Collections.unmodifiableList(
			Arrays.asList("org.slf4j.Logger", "org.apache.logging.log4j.Logger", "ch.qos.logback.classic.Logger"));

	private static final String THROWABLE_TYPE = Throwable.class.getName();
	private static final List<String> THROWABLE_TYPES = Collections
		.unmodifiableList(Arrays.asList(THROWABLE_TYPE));

	@SuppressWarnings("unchecked")
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

		// here we already know it is an InfixExpression
		InfixExpression infix = (InfixExpression) arguments.get(0);

		boolean isLeftOperandStringLiteral = isLeftOperandAllowedType(infix.getLeftOperand());

		if (!isLeftOperandStringLiteral) {
			return true;
		}

		StringLiteral currentLiteral = (StringLiteral) infix.getLeftOperand();

		ParameterCheckAstVisitor visitor = new ParameterCheckAstVisitor();
		infix.accept(visitor);

		boolean isStringLiteralWithoutArguments = visitor.isSafe();

		if (!isStringLiteralWithoutArguments) {
			return true;
		}

		List<Expression> newArguments = new ArrayList<>();
		List<StringLiteral> newStringLiterals = new ArrayList<>();

		AST ast = astRewrite.getAST();

		/*
		 * Logic to concatenate StringLiterals and introduce parameters
		 */
		boolean lastArgumentWasStringLiteral = true;
		StringLiteral lastLiteral = ast.newStringLiteral();
		lastLiteral.setLiteralValue(currentLiteral.getLiteralValue());
		for (Expression argument : getAllArguments(infix)) {
			if (ASTNode.STRING_LITERAL == argument.getNodeType()) {
				String argumentLiteralValue = ((StringLiteral) argument).getLiteralValue();
				if (lastArgumentWasStringLiteral) {
					newStringLiterals.add(lastLiteral);
					lastLiteral = ast.newStringLiteral();
					lastLiteral.setLiteralValue(argumentLiteralValue);
				} else {
					lastLiteral.setLiteralValue(lastLiteral.getLiteralValue() + argumentLiteralValue);
				}
				lastArgumentWasStringLiteral = true;
			} else {
				lastLiteral.setLiteralValue(lastLiteral.getLiteralValue() + "{}"); //$NON-NLS-1$
				newArguments.add(argument);
				lastArgumentWasStringLiteral = false;
			}
		}
		newStringLiterals.add(lastLiteral);

		/*
		 * Prepare the node to replace the original InfixExpression
		 */
		ASTNode newNode;
		if (newStringLiterals.size() == 1) {
			newNode = newStringLiterals.get(0);
		} else if (newStringLiterals.size() > 1) {
			InfixExpression newInfix = ast.newInfixExpression();
			newInfix.setLeftOperand(newStringLiterals.get(0));
			newInfix.setRightOperand(newStringLiterals.get(1));
			if (newStringLiterals.size() > 2) {
				for (int i = 2; i < newStringLiterals.size(); i++) {
					newInfix.extendedOperands()
						.add(newStringLiterals.get(i));
				}
			}
			newNode = newInfix;
		} else {
			// we have a problem
			return true;
		}

		astRewrite.replace(infix, newNode, null);

		ListRewrite listRewriter = astRewrite.getListRewrite(methodInvocation, MethodInvocation.ARGUMENTS_PROPERTY);

		/*
		 * Adding the collected arguments.
		 * 
		 * Reversing the arguments is done for the following reason: We cannot
		 * use insertLast (where we would not have to change the order) because
		 * there might be a Throwable as an argument after the InfixExpression
		 * and we want to add all items of this list before that. This version
		 * works regardless of whether or not there is a Throwable.
		 */
		Collections.reverse(newArguments);
		for (Expression newArgument : newArguments) {
			listRewriter.insertAfter(newArgument, newNode, null);
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	private List<Expression> getAllArguments(InfixExpression infix) {

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

	/**
	 * We only allow {@link StringLiteral}s as leftOperand.
	 * <p/>
	 * <b>Note:</b> Cases such as this:
	 * 
	 * <pre>
	 * <code>logger.info("s: " + "s" + " i: " + 1 + ".");</code>
	 * </pre>
	 * 
	 * ..will produce an {@link InfixExpression} as leftOperand with content:
	 * 
	 * <pre>
	 * <code>"s: " + "s" + " i: "</code>
	 * </pre>
	 * 
	 * ..and therefore no transformation will take place.
	 * <p/>
	 * We decided to not support those cases because it could get complicated
	 * real fast (e.g., further nesting of InfixExpressions).
	 */
	private boolean isLeftOperandAllowedType(Expression expression) {
		int nodeType = expression.getNodeType();

		switch (nodeType) {
		case ASTNode.STRING_LITERAL:
			return true;
		default:
			return false;
		}
	}

	/**
	 * This visitor is used to check whether all {@link StringLiteral} operands
	 * of an {@link InfixExpression} are free of parameters '{}'
	 */
	private class ParameterCheckAstVisitor extends ASTVisitor {
		private boolean safe = true;

		@Override
		public boolean visit(StringLiteral node) {
			if (StringUtils.contains(node.getLiteralValue(), "{}")) { //$NON-NLS-1$
				safe = false;
			}
			return safe;
		}

		public boolean isSafe() {
			return safe;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			return safe;
		}
	}

}

package eu.jsparrow.core.visitor.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
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

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		// we want an InfixExpression, which should always be 1 argument
		/*
		 * TODO: we might want to also include cases like that:
		 * http://slf4j.org/faq.html#paramException. Here the "pre" state might look
		 * like this: logger.error("Failed to format " + s, e);
		 */
		if (methodInvocation.arguments().size() != 1 || methodInvocation.getExpression() == null) {
			return true;
		}

		Expression expression = methodInvocation.getExpression();
		boolean isLoggerType = ClassRelationUtil.isContentOfTypes(expression.resolveTypeBinding(), LOGGER_TYPES);

		if (!isLoggerType) {
			return true;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		boolean isString = ClassRelationUtil.isContentOfType(arguments.get(0).resolveTypeBinding(),
				String.class.getName());
		boolean isInfix = (arguments.get(0).getNodeType() == ASTNode.INFIX_EXPRESSION);

		if (!isString || !isInfix) {
			return true;
		}

		InfixExpression infix = (InfixExpression) arguments.get(0);

		String method = methodInvocation.toString(); // TODO just for debugging -> remove

		boolean isLeftOperandStringLiteral = ASTNode.STRING_LITERAL == infix.getLeftOperand().getNodeType();

		if (!isLeftOperandStringLiteral) {
			return true;
		}

		StringLiteral currentLiteral = (StringLiteral) infix.getLeftOperand();

		/*
		 * TODO this check needs to be more sophisticated. it needs to look at the
		 * complete infix expression. Example: logger.info("A " + 1 + " B {}", 2);
		 */
		boolean isStringLiteralWithoutArguments = !StringUtils.contains(currentLiteral.getLiteralValue(), "{}"); //$NON-NLS-1$

		/*
		 * TODO this needs to be extended for string literals etc.
		 */
		boolean isRightOperandSimpleName = ASTNode.SIMPLE_NAME == infix.getRightOperand().getNodeType();

		if (!isStringLiteralWithoutArguments || !isRightOperandSimpleName) {
			return true;
		}

		AST ast = astRewrite.getAST();
		StringLiteral newLiteral = ast.newStringLiteral();
		newLiteral.setLiteralValue(currentLiteral.getLiteralValue() + "{}"); //$NON-NLS-1$
		
		SimpleName newArgument = (SimpleName) infix.getRightOperand();
		
		astRewrite.replace(infix, newLiteral, null);
		
		ListRewrite listRewriter = astRewrite.getListRewrite(methodInvocation, MethodInvocation.ARGUMENTS_PROPERTY);
		
		listRewriter.insertAfter(newArgument, newLiteral, null);

		return true;
	}

}

package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class OptionalIfPresentASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String OPTIONAL_FULLY_QUALIFIED_NAME = java.util.Optional.class.getName();
	private static final String IS_PRESENT = "isPresent"; //$NON-NLS-1$

	/**
	 * Looks for occurrences of optional.isPresent() where the following
	 * conditions are met:
	 * <ul>
	 * <li>The statements parent is an if-statement.</li>
	 * <li>There are no other statements in this if-statement, neither in the
	 * else branch.</li>
	 * <li>The THAN_STATEMENT contains VariableDeclarationStatement first</li>
	 * <li>The THAN_STATEMENT contains ExpressionStatement</li>
	 * <p>
	 * 
	 * If all conditions are met the entire if-statement is replaced with a call
	 * to optional.ifPresent(..) where the expression matches the previous
	 * expression statement in than branch and the consumer match the previous
	 * Type of VariableDeclarationStatement.
	 * 
	 */
	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		if (!isIsPresentMethod(methodInvocation)) {
			return true;
		}

		boolean hasIfStatementParent = methodInvocation.getParent()
			.getNodeType() == ASTNode.IF_STATEMENT;
		if (!hasIfStatementParent) {
			return true;
		}

		IfStatement ifStatement = (IfStatement) methodInvocation.getParent();
		if (ifStatement == null) {
			return true;
		}
		if (ifStatement.getElseStatement() != null) {
			return true;
		}

		Block thanStatement = (Block) ifStatement.getThenStatement();
		// remove VariableDeclarationStatement, take name from it and use it as
		// consumer in ifPresent and the rest statements in expression field.
		
		AST ast = astRewrite.getAST();
		LambdaExpression lambdaExpression = ast.newLambdaExpression();
//		lambdaExpression.setBody(body);
//		lambdaExpression.parameters().add(ast.newSimpleName(identifier));

		return false;
	}

	private boolean isIsPresentMethod(MethodInvocation methodInvocation) {
		if (!methodInvocation.arguments()
			.isEmpty() || methodInvocation.getExpression() == null) {
			return false;
		}
		return hasRightTypeAndName(methodInvocation, OPTIONAL_FULLY_QUALIFIED_NAME, IS_PRESENT);
	}

	private Boolean hasRightTypeAndName(MethodInvocation methodInvocation, String type, String name) {
		List<String> fullyQualifiedOptionalName = generateFullyQualifiedNameList(type);
		Boolean epxressionTypeMatches = ClassRelationUtil.isContentOfTypes(methodInvocation.getExpression()
			.resolveTypeBinding(), fullyQualifiedOptionalName);
		Boolean methodNameMatches = StringUtils.equals(name, methodInvocation.getName()
			.getFullyQualifiedName());
		return epxressionTypeMatches && methodNameMatches;
	}

}

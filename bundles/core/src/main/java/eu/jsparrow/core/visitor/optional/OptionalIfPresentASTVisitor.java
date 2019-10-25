package eu.jsparrow.core.visitor.optional;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Usages of {@link Optional#isPresent()} combined with {@link Optional#get()}
 * should be replaced with {@link Optional#ifPresent(Consumer)} method wherever
 * possible.
 * 
 * @since 2.6
 *
 */
public class OptionalIfPresentASTVisitor extends AbstractOptionalASTVisitor {

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
	 * expression statement in then branch and the consumer match the previous
	 * Type of VariableDeclarationStatement.
	 * 
	 */
	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		if (!isIsPresentMethod(methodInvocation)) {
			return true;
		}

		boolean hasIfStatementParent = IfStatement.EXPRESSION_PROPERTY == methodInvocation.getLocationInParent();
		if (!hasIfStatementParent) {
			return true;
		}

		IfStatement ifStatement = (IfStatement) methodInvocation.getParent();
		if (ifStatement.getElseStatement() != null) {
			return true;
		}

		Statement thenStatement = ifStatement.getThenStatement();

		// Check thenStatement for non-effectively final variables
		boolean hasNonEfectivellyFinalVariable = containsNonEffectivelyFinal(thenStatement);
		if (hasNonEfectivellyFinalVariable) {
			return true;
		}

		/*
		 * Check thenStatement for 'return', throw, 'break' or 'continue'
		 * statements.
		 */
		boolean hasReturnStatement = containsFlowControlStatement(thenStatement);
		if (hasReturnStatement) {
			return true;
		}

		/*
		 * Check for unhandled exceptions
		 */
		boolean hasUnhandledException = containsUnhandledException(thenStatement);
		if (hasUnhandledException) {
			return true;
		}

		// Find the optional expression
		Expression optional = methodInvocation.getExpression();
		OptionalGetVisitor optionalGetVisitor = new OptionalGetVisitor(optional);
		thenStatement.accept(optionalGetVisitor);

		List<MethodInvocation> getExpressions = optionalGetVisitor.getInvocations();
		List<MethodInvocation> nonDiscardedGetExpressions = getExpressions.stream()
			.filter(get -> !isDiscardedMethodInvocation(get))
			.collect(Collectors.toList());
		if (nonDiscardedGetExpressions.isEmpty()) {
			return true;
		}

		// Find parameter name
		String identifier = findParameterName(thenStatement, nonDiscardedGetExpressions);
		if (identifier.isEmpty()) {
			return true;
		}

		IfPresentBodyFactoryVisitor visitor = new IfPresentBodyFactoryVisitor(nonDiscardedGetExpressions, identifier,
				astRewrite);
		thenStatement.accept(visitor);
		ASTNode lambdaBody = unwrapBody(thenStatement);
		LambdaExpression lambda = NodeBuilder.newLambdaExpression(methodInvocation.getAST(),
				astRewrite.createCopyTarget(lambdaBody), identifier);

		/*
		 * Create a lambda expression with parameter and body optional and
		 * ifPresent method invocation
		 */
		Statement optionalIfPresent = createOptionalIfPresentStatement(optional, lambda);

		// Replace the if statement with the new optiona.ifPresent statement
		astRewrite.replace(ifStatement, optionalIfPresent, null);
		onRewrite();
		saveComments(methodInvocation, ifStatement, lambdaBody, removedNodes);
		removedNodes.clear();
		return true;
	}

	private void saveComments(MethodInvocation methodInvocation, IfStatement ifStatement, ASTNode lambdaBody,
			List<ASTNode> removedNodes) {
		OptionalIfPresentCommentWriter commentsWriter = new OptionalIfPresentCommentWriter(getCommentRewriter());
		commentsWriter.saveComments(methodInvocation, ifStatement, lambdaBody, removedNodes);
	}

	private boolean isDiscardedMethodInvocation(MethodInvocation methodInvocation) {
		return ExpressionStatement.EXPRESSION_PROPERTY == methodInvocation.getLocationInParent();
	}

	@SuppressWarnings("unchecked")
	private Statement createOptionalIfPresentStatement(Expression optional, LambdaExpression lambda) {
		AST ast = astRewrite.getAST();
		MethodInvocation ifPresent = ast.newMethodInvocation();
		ifPresent.setName(ast.newSimpleName(IF_PRESENT));
		ifPresent.setExpression((Expression) astRewrite.createCopyTarget(optional));
		ifPresent.arguments()
			.add(lambda);

		return ast.newExpressionStatement(ifPresent);
	}

	/**
	 * Converts the body into an {@link Expression} if it consists a single
	 * {@link ExpressionStatement}. . * <b>ATTENTION:</b> deletes all nodes in
	 * {@code removedNodes}!
	 * 
	 * @param body
	 *            the node to be transformed
	 * @return the unwrapped {@link Expression} if the body consist of one
	 *         {@link ExpressionStatement} or the unchanged body otherwise.
	 */
	private ASTNode unwrapBody(Statement body) {

		ASTNode lambdaBody = body;
		if (ASTNode.BLOCK == body.getNodeType()) {
			Block block = (Block) body;
			List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
			if (statements.size() == 1) {
				Statement singleBodyStatement = statements.get(0);
				if (ASTNode.EXPRESSION_STATEMENT == singleBodyStatement.getNodeType()) {
					ExpressionStatement expressionStatement = (ExpressionStatement) singleBodyStatement;
					lambdaBody = expressionStatement.getExpression();
				}
			}
		} else if (ASTNode.EXPRESSION_STATEMENT == body.getNodeType()) {
			lambdaBody = ((ExpressionStatement) body).getExpression();
		}
		return lambdaBody;
	}

	protected boolean isIsPresentMethod(MethodInvocation methodInvocation) {
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

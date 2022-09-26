package eu.jsparrow.core.visitor.optional;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.core.markers.common.OptionalIfPresentEvent;
import eu.jsparrow.core.visitor.sub.FlowBreakersVisitor;
import eu.jsparrow.core.visitor.sub.UnhandledExceptionVisitor;
import eu.jsparrow.rules.common.builder.NodeBuilder;

/**
 * Usages of {@link Optional#isPresent()} combined with {@link Optional#get()}
 * should be replaced with {@link Optional#ifPresent(Consumer)} method wherever
 * possible.
 * 
 * @since 2.6
 *
 */
public class OptionalIfPresentASTVisitor extends AbstractOptionalASTVisitor implements OptionalIfPresentEvent {

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
		boolean hasReturnStatement = FlowBreakersVisitor.containsFlowControlStatement(thenStatement);
		if (hasReturnStatement) {
			return true;
		}

		/*
		 * Check for unhandled exceptions
		 */
		boolean hasUnhandledException = !UnhandledExceptionVisitor.analyzeExceptionHandling(thenStatement, ifStatement);
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
		ASTNode lambdaBody = unwrapLambdaBody(thenStatement);
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
		addMarkerEvent(methodInvocation);
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

	protected boolean isIsPresentMethod(MethodInvocation methodInvocation) {
		if (!methodInvocation.arguments()
			.isEmpty() || methodInvocation.getExpression() == null) {
			return false;
		}
		return hasRightTypeAndName(methodInvocation, OPTIONAL_FULLY_QUALIFIED_NAME, IS_PRESENT);
	}
}

package eu.jsparrow.core.visitor.optional;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import eu.jsparrow.core.visitor.sub.EffectivelyFinalVisitor;
import eu.jsparrow.core.visitor.sub.LiveVariableScope;
import eu.jsparrow.core.visitor.sub.UnhandledExceptionVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class OptionalIfPresentASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String OPTIONAL_FULLY_QUALIFIED_NAME = java.util.Optional.class.getName();
	private static final String IS_PRESENT = "isPresent"; //$NON-NLS-1$
	private static final String IF_PRESENT = "ifPresent"; //$NON-NLS-1$
	private static final String DEFAULT_LAMBDA_PARAMETER_NAME = "value"; //$NON-NLS-1$

	private LiveVariableScope scope = new LiveVariableScope();

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
		boolean hasReturnStatement = containsReturnStatement(thenStatement);
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
		if (getExpressions.isEmpty()) {
			return true;
		}

		// Find parameter name
		String identifier = findParameterName(thenStatement, getExpressions);
		if (identifier.isEmpty()) {
			return true;
		}

		IfPresentBodyFactoryVisitor visitor = new IfPresentBodyFactoryVisitor(getExpressions,
				optionalGetVisitor.getReferencesToBeRenamed(), identifier, astRewrite);
		thenStatement.accept(visitor);
		Statement body = thenStatement;
		ASTNode lambdaBody = unwrapBody(body, visitor.getRemovedNodes());

		LambdaExpression lambda = createLambdaExpression(lambdaBody, identifier);

		/*
		 * Create a lambda expression with parameter and body optional and
		 * ifPresent method invocation
		 */
		Statement optionalIfPresent = createOptionalIfPresentStatement(optional, lambda);

		// Replace the if statement with the new optiona.ifPresent statement
		astRewrite.replace(ifStatement, optionalIfPresent, null);

		// Remember to save comments at each step

		return true;
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		this.scope.clearLocalVariablesScope(typeDeclaration);
		this.scope.clearFieldScope(typeDeclaration);
	}

	@Override
	public void endVisit(MethodDeclaration methodDeclaration) {
		this.scope.clearLocalVariablesScope(methodDeclaration);
	}

	@Override
	public void endVisit(FieldDeclaration fieldDeclaration) {
		this.scope.clearLocalVariablesScope(fieldDeclaration);
	}

	@Override
	public void endVisit(Initializer initializer) {
		this.scope.clearLocalVariablesScope(initializer);
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

	@SuppressWarnings("unchecked")
	private LambdaExpression createLambdaExpression(ASTNode lambdaBody, String identifier) {

		AST ast = astRewrite.getAST();
		LambdaExpression lambdaExpression = ast.newLambdaExpression();
		SimpleName parameter = ast.newSimpleName(identifier);
		VariableDeclarationFragment parameterDeclaration = ast.newVariableDeclarationFragment();
		parameterDeclaration.setName(parameter);

		lambdaExpression.setParentheses(false);
		lambdaExpression.setBody(astRewrite.createCopyTarget(lambdaBody));
		lambdaExpression.parameters()
			.add(parameterDeclaration);

		return lambdaExpression;
	}

	/**
	 * Converts the body into an {@link Expression} if it consists a single
	 * {@link ExpressionStatement}. . * <b>ATTENTION:</b> deletes all nodes in
	 * {@code removedNodes}!
	 * 
	 * @param body
	 *            the node to be transformed
	 * @param removedNodes
	 *            list of nodes registered to be removed by the
	 *            {@link ASTRewrite}.
	 * @return the unwrapped {@link Expression} if the body consist of one
	 *         {@link ExpressionStatement} or the unchanged body otherwise.
	 */
	private ASTNode unwrapBody(Statement body, List<ASTNode> removedNodes) {

		/*
		 * A workaround to help with finding out how many statements remain in
		 * the body.
		 */
		for (ASTNode node : removedNodes) {
			node.delete();
		}

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

	private String findParameterName(Statement thenStatement, List<MethodInvocation> getExpressions) {
		return getExpressions.stream()
			.filter(e -> e.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY)
			.map(ASTNode::getParent)
			.map(fragment -> ((VariableDeclarationFragment) fragment).getName())
			.map(SimpleName::getIdentifier)
			.findFirst()
			.orElse(computeUniqueIdentifier(thenStatement));
	}

	private String computeUniqueIdentifier(Statement thenStatement) {
		ASTNode enclosingScope = scope.findEnclosingScope(thenStatement)
			.orElse(null);
		if (enclosingScope == null) {
			return ""; //$NON-NLS-1$
		}
		scope.lazyLoadScopeNames(enclosingScope);

		String newName = DEFAULT_LAMBDA_PARAMETER_NAME;
		int suffix = 1;
		while (scope.isInScope(newName)) {
			newName = DEFAULT_LAMBDA_PARAMETER_NAME + suffix;
			suffix++;
		}
		scope.storeIntroducedName(enclosingScope, newName);
		return newName;
	}

	private boolean containsReturnStatement(Statement thenStatement) {
		FlowBreakersVisitor visitor = new FlowBreakersVisitor();
		thenStatement.accept(visitor);
		return visitor.hasFlowBreakerStatement();
	}

	private boolean containsUnhandledException(Statement thenStatement) {
		UnhandledExceptionVisitor visitor = new UnhandledExceptionVisitor();
		thenStatement.accept(visitor);
		return visitor.throwsException();
	}

	private boolean containsNonEffectivelyFinal(Statement thenStatement) {
		EffectivelyFinalVisitor visitor = new EffectivelyFinalVisitor();
		thenStatement.accept(visitor);
		return visitor.containsNonEffectivelyFinalVariable();
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

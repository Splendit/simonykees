package eu.jsparrow.core.visitor.optional;

import java.util.ArrayList;
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
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.visitor.sub.LiveVariableScope;
import eu.jsparrow.core.visitor.sub.ReferencedFieldsVisitor;
import eu.jsparrow.core.visitor.sub.UnhandledExceptionVisitor;
import eu.jsparrow.core.visitor.sub.VariableDeclarationsVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Usages of {@link Optional#isPresent()} combined with {@link Optional#get()}
 * should be replaced with {@link Optional#ifPresent(Consumer)} method wherever
 * possible.
 * 
 * @since 2.6
 *
 */
public class OptionalIfPresentASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String OPTIONAL_FULLY_QUALIFIED_NAME = java.util.Optional.class.getName();
	private static final String IS_PRESENT = "isPresent"; //$NON-NLS-1$
	private static final String IF_PRESENT = "ifPresent"; //$NON-NLS-1$
	private static final String DEFAULT_LAMBDA_PARAMETER_NAME = "value"; //$NON-NLS-1$

	private LiveVariableScope scope = new LiveVariableScope();
	private List<ASTNode> removedNodes = new ArrayList<>();

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
		LambdaExpression lambda = createLambdaExpression(lambdaBody, identifier);

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
		lambdaExpression.parameters()
			.add(parameterDeclaration);
		int bodyNodeType = lambdaBody.getNodeType();
		if (ASTNode.BLOCK == bodyNodeType || lambdaBody instanceof Expression) {
			lambdaExpression.setBody(astRewrite.createCopyTarget(lambdaBody));
		} else {
			Block newBlock = ast.newBlock();
			newBlock.statements()
				.add(astRewrite.createCopyTarget(lambdaBody));
			lambdaExpression.setBody(newBlock);
		}

		return lambdaExpression;
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

	private String findParameterName(Statement thenStatement, List<MethodInvocation> getExpressions) {
		List<String> referencedFields = findAllReferencedFields(thenStatement).stream()
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toList());

		Optional<SimpleName> identifier = getExpressions.stream()
			.filter(e -> e.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY)
			.map(ASTNode::getParent)
			.map(fragment -> ((VariableDeclarationFragment) fragment).getName())
			.findFirst()
			.filter(name -> !referencedFields.contains(name.getIdentifier()));

		String name = identifier.map(SimpleName::getIdentifier)
			.orElse(""); //$NON-NLS-1$
		if (countDeclaredVariables(thenStatement, name) > 1) {
			return computeUniqueIdentifier(thenStatement);
		}

		identifier.ifPresent(this::safeDeleteInitializer);
		return identifier.map(SimpleName::getIdentifier)
			.orElse(computeUniqueIdentifier(thenStatement));
	}

	private long countDeclaredVariables(Statement thenStatement, String name) {
		VariableDeclarationsVisitor visitor = new VariableDeclarationsVisitor();
		thenStatement.accept(visitor);
		return visitor.getVariableDeclarationNames()
			.stream()
			.map(SimpleName::getIdentifier)
			.filter(name::equals)
			.count();
	}

	private void safeDeleteInitializer(SimpleName name) {

		if (VariableDeclarationFragment.NAME_PROPERTY != name.getLocationInParent()) {
			return;
		}
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) name.getParent();

		Expression initializer = fragment.getInitializer();
		if (initializer == null) {
			return;
		}

		if (VariableDeclarationStatement.FRAGMENTS_PROPERTY != fragment.getLocationInParent()) {
			return;
		}

		VariableDeclarationStatement declarationStatement = (VariableDeclarationStatement) fragment.getParent();
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(declarationStatement.fragments(),
				VariableDeclarationFragment.class);
		if (fragments.size() == 1) {
			removedNodes.add(declarationStatement);
			astRewrite.remove(declarationStatement, null);
			declarationStatement.delete();
			return;
		}

		astRewrite.remove(fragment, null);
		removedNodes.add(fragment);
		fragment.delete();
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

		return newName;
	}

	private List<SimpleName> findAllReferencedFields(Statement thenStatement) {
		ReferencedFieldsVisitor visitor = new ReferencedFieldsVisitor();
		thenStatement.accept(visitor);
		return visitor.getReferencedVariables();
	}

	private boolean containsFlowControlStatement(Statement thenStatement) {
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
		ExternalNonEffectivelyFinalReferencesVisitor visitor = new ExternalNonEffectivelyFinalReferencesVisitor();
		thenStatement.accept(visitor);
		return visitor.containsReferencesToExternalNonFinalVariables();
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

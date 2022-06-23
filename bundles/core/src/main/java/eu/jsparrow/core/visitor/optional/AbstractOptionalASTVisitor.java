package eu.jsparrow.core.visitor.optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.visitor.sub.ExternalNonEffectivelyFinalReferencesVisitor;
import eu.jsparrow.core.visitor.sub.FlowBreakersVisitor;
import eu.jsparrow.core.visitor.sub.ReferencedFieldsVisitor;
import eu.jsparrow.core.visitor.sub.UnhandledExceptionVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LiveVariableScope;
import eu.jsparrow.rules.common.visitor.helper.VariableDeclarationsVisitor;

/**
 * A parent class for visitors of {@link java.util.Optional} rules.
 * 
 * @since 3.10.0
 */
public class AbstractOptionalASTVisitor extends AbstractASTRewriteASTVisitor {

	protected static final String OPTIONAL_FULLY_QUALIFIED_NAME = java.util.Optional.class.getName();
	protected static final String IS_PRESENT = "isPresent"; //$NON-NLS-1$
	protected static final String IF_PRESENT = "ifPresent"; //$NON-NLS-1$
	protected static final String DEFAULT_LAMBDA_PARAMETER_NAME = "value"; //$NON-NLS-1$
	protected LiveVariableScope scope = new LiveVariableScope();
	protected List<ASTNode> removedNodes = new ArrayList<>();

	protected boolean containsNonEffectivelyFinal(Statement thenStatement) {
		ExternalNonEffectivelyFinalReferencesVisitor visitor = new ExternalNonEffectivelyFinalReferencesVisitor();
		thenStatement.accept(visitor);
		return visitor.containsReferencesToExternalNonFinalVariables();
	}

	protected boolean containsFlowControlStatement(Statement thenStatement) {
		FlowBreakersVisitor visitor = new FlowBreakersVisitor();
		thenStatement.accept(visitor);
		return visitor.hasFlowBreakerStatement();
	}

	protected String findParameterName(Statement thenStatement, List<MethodInvocation> getExpressions) {
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

	protected long countDeclaredVariables(Statement thenStatement, String name) {
		VariableDeclarationsVisitor visitor = new VariableDeclarationsVisitor();
		thenStatement.accept(visitor);
		return visitor.getVariableDeclarationNames()
			.stream()
			.map(SimpleName::getIdentifier)
			.filter(name::equals)
			.count();
	}

	protected String computeUniqueIdentifier(Statement thenStatement) {
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

	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		this.scope.clearCompilationUnitScope(compilationUnit);
		super.endVisit(compilationUnit);
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
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

	protected List<SimpleName> findAllReferencedFields(Statement thenStatement) {
		ReferencedFieldsVisitor visitor = new ReferencedFieldsVisitor();
		thenStatement.accept(visitor);
		return visitor.getReferencedVariables();
	}

	protected void safeDeleteInitializer(SimpleName name) {

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
	protected ASTNode unwrapLambdaBody(Statement body) {
		if (body.getNodeType() == ASTNode.BLOCK) {
			Block block = (Block) body;
			List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
			if (statements.size() == 1) {
				Statement statement = statements.get(0);
				if (statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
					ExpressionStatement expressionStatement = (ExpressionStatement) statement;
					return expressionStatement.getExpression();
				}
			}
		} else if (body.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
			ExpressionStatement expressionStatement = (ExpressionStatement) body;
			return expressionStatement.getExpression();
		}
		return body;
	}

	protected Boolean hasRightTypeAndName(MethodInvocation methodInvocation, String type, String name) {
		List<String> fullyQualifiedOptionalName = generateFullyQualifiedNameList(type);
		Boolean epxressionTypeMatches = ClassRelationUtil.isContentOfTypes(methodInvocation.getExpression()
			.resolveTypeBinding(), fullyQualifiedOptionalName);
		Boolean methodNameMatches = StringUtils.equals(name, methodInvocation.getName()
			.getFullyQualifiedName());
		return epxressionTypeMatches && methodNameMatches;
	}
}

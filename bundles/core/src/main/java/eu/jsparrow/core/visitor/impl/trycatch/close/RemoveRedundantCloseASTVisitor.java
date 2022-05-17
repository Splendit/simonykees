package eu.jsparrow.core.visitor.impl.trycatch.close;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.markers.common.RemoveRedundantCloseEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor looks for redundant close statements in Try-With-Resource
 * statements and removes them if possible.
 * 
 * @since 4.11.0
 *
 */
public class RemoveRedundantCloseASTVisitor extends AbstractASTRewriteASTVisitor implements RemoveRedundantCloseEvent {

	private static final String CLOSE = "close"; //$NON-NLS-1$

	@Override
	public boolean visit(TryStatement node) {

		List<VariableDeclarationFragment> resourceDeclarations = collectResourceDeclarations(node);
		for (VariableDeclarationFragment resourceDeclaration : resourceDeclarations) {
			findRedundantCloseStatementToRemove(node, resourceDeclaration)
				.ifPresent(this::transform);
		}
		return true;
	}

	protected void transform(ExpressionStatement closeStatement) {
		getCommentRewriter().saveRelatedComments(closeStatement);
		astRewrite.remove(closeStatement, null);
		addMarkerEvent(closeStatement);
		onRewrite();
	}

	protected List<VariableDeclarationFragment> collectResourceDeclarations(TryStatement tryStatement) {

		List<ASTNode> resourcesASTNodes = ASTNodeUtil.convertToTypedList(tryStatement.resources(),
				ASTNode.class);

		if (resourcesASTNodes.isEmpty()) {
			return Collections.emptyList();
		}

		List<VariableDeclarationFragment> resources = new ArrayList<>();
		for (ASTNode resourceASTNode : resourcesASTNodes) {
			if (resourceASTNode.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION) {
				VariableDeclarationExpression variableDeclarationexpresson = (VariableDeclarationExpression) resourceASTNode;
				ASTNodeUtil
					.convertToTypedList(variableDeclarationexpresson.fragments(), VariableDeclarationFragment.class)
					.forEach(resources::add);

			} else if (resourceASTNode.getNodeType() == ASTNode.SIMPLE_NAME) {
				SimpleName simpleName = (SimpleName) resourceASTNode;
				findLocalVariableDeclarationFragment(simpleName)
					.ifPresent(resources::add);
			}
		}
		return resources;
	}

	private Optional<VariableDeclarationFragment> findLocalVariableDeclarationFragment(SimpleName simpleName) {
		IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			return Optional.empty();
		}

		if (binding.getKind() != IBinding.VARIABLE) {
			return Optional.empty();
		}

		IVariableBinding variableBinding = (IVariableBinding) binding;
		if (variableBinding.isField()) {
			return Optional.empty();
		}

		ASTNode declaringNode = getCompilationUnit().findDeclaringNode(variableBinding);
		if (declaringNode == null) {
			return Optional.empty();
		}

		if (declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return Optional.empty();
		}

		VariableDeclarationFragment localVariableDeclarationFragment = (VariableDeclarationFragment) declaringNode;
		return Optional.of(localVariableDeclarationFragment);

	}

	protected Optional<ExpressionStatement> findRedundantCloseStatementToRemove(TryStatement tryStatement,
			VariableDeclarationFragment resourceDeclaration) {

		LastReferenceOnResourceVisitor lastReferenceVisitor = new LastReferenceOnResourceVisitor(resourceDeclaration,
				getCompilationUnit());
		Block tryStatementBody = tryStatement.getBody();
		tryStatementBody.accept(lastReferenceVisitor);

		SimpleName lastReference = lastReferenceVisitor.getLastReference()
			.orElse(null);
		if (lastReference == null) {
			return Optional.empty();
		}
		if (lastReference.getLocationInParent() != MethodInvocation.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}
		MethodInvocation methodInvocation = (MethodInvocation) lastReference.getParent();
		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}
		ExpressionStatement expressionStatement = (ExpressionStatement) methodInvocation.getParent();
		if (expressionStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return Optional.empty();
		}

		if (!methodInvocation.getName()
			.getIdentifier()
			.equals(CLOSE)) {
			return Optional.empty();
		}
		if (!methodInvocation.arguments()
			.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(expressionStatement);
	}
}

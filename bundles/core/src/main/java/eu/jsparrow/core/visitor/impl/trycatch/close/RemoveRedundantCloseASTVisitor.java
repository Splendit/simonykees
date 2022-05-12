package eu.jsparrow.core.visitor.impl.trycatch.close;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor looks for redundant close statements in Try-With-Resource
 * statements and removes them if possible.
 * 
 * @since 4.11.0
 *
 */
public class RemoveRedundantCloseASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String CLOSE = "close"; //$NON-NLS-1$

	@Override
	public boolean visit(TryStatement node) {

		List<VariableDeclarationFragment> resourceDeclarations = collectResourceDeclarations(node);
		for (VariableDeclarationFragment resourceDeclaration : resourceDeclarations) {
			findRedundantCloseStatementToRemove(node, resourceDeclaration)
				.ifPresent(closeStatement -> {
					astRewrite.remove(closeStatement, null);
					onRewrite();
				});
		}

		return true;
	}

	private List<VariableDeclarationFragment> collectResourceDeclarations(TryStatement tryStatement) {

		List<VariableDeclarationExpression> resources = ASTNodeUtil.convertToTypedList(tryStatement.resources(),
				VariableDeclarationExpression.class);

		if (resources.isEmpty()) {
			return Collections.emptyList();
		}

		return resources
			.stream()
			.flatMap(resource -> ASTNodeUtil.convertToTypedList(resource.fragments(), VariableDeclarationFragment.class)
				.stream())
			.collect(Collectors.toList());
	}

	private Optional<ExpressionStatement> findRedundantCloseStatementToRemove(TryStatement tryStatement,
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
		if (expressionStatement.getParent() != tryStatementBody) {
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

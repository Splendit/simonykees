package eu.jsparrow.core.visitor.optional;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class IfPresentBodyFactoryVisitor extends ASTVisitor {

	private List<MethodInvocation> getInvocations;
	private String parameterName;
	private ASTRewrite astRewrite;
	private List<ASTNode> removedNodes = new ArrayList<>();
	private List<SimpleName> toBeRenamed;

	public IfPresentBodyFactoryVisitor(List<MethodInvocation> getInvocations, List<SimpleName> toBeRenamed, String parameterName,
			ASTRewrite astRewrite) {
		this.getInvocations = getInvocations;
		this.parameterName = parameterName;
		this.astRewrite = astRewrite;
		this.toBeRenamed = toBeRenamed;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (!getInvocations.contains(methodInvocation)) {
			return true;
		}

		if (VariableDeclarationFragment.INITIALIZER_PROPERTY == methodInvocation.getLocationInParent()) {
			/*
			 * Remove the initializer together with the declaration if
			 * necessary.
			 */
			VariableDeclarationFragment initializer = (VariableDeclarationFragment) methodInvocation.getParent();
			safeDeleteInitializer(initializer);

		} else {
			/*
			 * replace the method invocation with the parameter of the lambda
			 * expression of Optional.ifPresent
			 */
			AST ast = astRewrite.getAST();
			SimpleName simpleName = ast.newSimpleName(parameterName);
			astRewrite.replace(methodInvocation, simpleName, null);
		}

		return true;
	}
	
	@Override
	public boolean visit(SimpleName simpleName) {
		if(toBeRenamed.contains(simpleName)) {
			AST ast = simpleName.getAST();
			SimpleName newName = ast.newSimpleName(parameterName);
			astRewrite.replace(simpleName, newName, null);
		}
		return true;
	}

	private void safeDeleteInitializer(VariableDeclarationFragment initializer) {
		if (VariableDeclarationStatement.FRAGMENTS_PROPERTY == initializer.getLocationInParent()) {
			VariableDeclarationStatement declarationStatement = (VariableDeclarationStatement) initializer.getParent();
			List<VariableDeclarationFragment> fragments = ASTNodeUtil
				.convertToTypedList(declarationStatement.fragments(), VariableDeclarationFragment.class);
			if (fragments.size() == 1) {
				astRewrite.remove(declarationStatement, null);
				removedNodes.add(declarationStatement);
				return;
			}
		}
		astRewrite.remove(initializer, null);
		removedNodes.add(initializer);
	}
	
	public List<ASTNode> getRemovedNodes() {
		return removedNodes;
	}
}

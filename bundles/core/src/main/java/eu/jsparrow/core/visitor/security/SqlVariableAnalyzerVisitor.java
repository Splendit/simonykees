package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Collects all references on a given variable within the {@link ASTNode} which
 * is visited.
 * 
 * @since 3.16.0
 *
 */
public class SqlVariableAnalyzerVisitor extends ASTVisitor {

	private final CompilationUnit compilationUnit;
	private final VariableDeclarationFragment variableDeclarationFragment;
	private final List<SimpleName> variableReferences = new ArrayList<>();
	private boolean beforeDeclaration = true;

	public SqlVariableAnalyzerVisitor(VariableDeclarationFragment variableDeclarationFragment) {
		this.compilationUnit = ASTNodeUtil.getSpecificAncestor(variableDeclarationFragment, CompilationUnit.class);
		this.variableDeclarationFragment = variableDeclarationFragment;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		if (simpleName == variableDeclarationFragment.getName()) {
			beforeDeclaration = false;
			return false;
		}
		if (beforeDeclaration) {
			return false;
		}
		if (!simpleName.getIdentifier()
			.equals(variableDeclarationFragment.getName()
				.getIdentifier())) {
			return false;
		}
		IBinding binding = simpleName.resolveBinding();
		if (binding.getKind() != IBinding.VARIABLE) {
			return false;
		}
		if (compilationUnit.findDeclaringNode(binding) == variableDeclarationFragment) {
			variableReferences.add(simpleName);
		}
		return true;
	}

	public List<SimpleName> getVariableReferences() {
		return variableReferences;
	}

}

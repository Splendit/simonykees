package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * A visitor for collecting the names of the referenced variables.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class ReferencedVariablesASTVisitor extends ASTVisitor {
	private List<SimpleName> referencedVariables;

	public ReferencedVariablesASTVisitor() {
		referencedVariables = new ArrayList<>();
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		if (simpleName.resolveBinding() != null && simpleName.resolveBinding()
			.getKind() == IBinding.VARIABLE) {
			referencedVariables.add(simpleName);
		}
		return true;
	}

	public List<SimpleName> getReferencedVariables() {
		return referencedVariables;
	}
}

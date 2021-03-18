package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * A visitor for finding references to non-effectively final variables whose
 * declaration occurs outside the {@link ASTNode} being visited.
 * 
 * @since 2.6.0
 */
public class ExternalNonEffectivelyFinalReferencesVisitor extends ASTVisitor {

	private List<String> declarations = new ArrayList<>();

	private boolean containsExternalNonFinalVariable = false;

	public ExternalNonEffectivelyFinalReferencesVisitor() {

	}

	public ExternalNonEffectivelyFinalReferencesVisitor(List<String> excludes) {
		declarations.addAll(excludes);
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !containsExternalNonFinalVariable;
	}

	public boolean containsReferencesToExternalNonFinalVariables() {
		return containsExternalNonFinalVariable;
	}

	@Override
	public boolean visit(SimpleName name) {

		if (name.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY) {
			declarations.add(name.getIdentifier());
			return false;
		}

		if (declarations.contains(name.getIdentifier())) {
			return false;
		}

		IBinding binding = name.resolveBinding();
		if (binding != null && IBinding.VARIABLE == binding.getKind() && binding instanceof IVariableBinding) {
			IVariableBinding variableBinding = (IVariableBinding) binding;

			if (!variableBinding.isField() && !Modifier.isFinal(variableBinding.getModifiers())
					&& !variableBinding.isEffectivelyFinal()) {
				this.containsExternalNonFinalVariable = true;
			}
		}

		return true;
	}
}

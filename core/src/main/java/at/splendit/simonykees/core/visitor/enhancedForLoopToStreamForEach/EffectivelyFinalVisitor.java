package at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * A visitor that checks for occurrences of variables that are neither final nor
 * effectively final.
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
class EffectivelyFinalVisitor extends ASTVisitor {

	private boolean containsNonfinalVar = false;

	@Override
	public boolean preVisit2(ASTNode node) {
		return !containsNonfinalVar;
	}

	/**
	 * 
	 * @return if the the visitor has found an occurrence of a variable
	 *         which is neither final NOR effectively final.
	 */
	public boolean containsNonEffectivelyFinalVariable() {
		return this.containsNonfinalVar;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding binding = simpleName.resolveBinding();
		if (binding != null && IBinding.VARIABLE == binding.getKind() && binding instanceof IVariableBinding) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			if (!Modifier.isFinal(variableBinding.getModifiers()) && !variableBinding.isEffectivelyFinal()) {
				this.containsNonfinalVar = true;
			}
		}

		return true;
	}
}

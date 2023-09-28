package eu.jsparrow.rules.common.visitor.helper;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.exception.UnresolvedBindingException;

/**
 * Finds out whether a local variable is or is not referenced.
 * 
 * @since 4.19.0
 *
 */
public abstract class AbstractLocalVariableReferencesVisitor extends ASTVisitor {
	private final ReferenceToLocalVariableAnalyzer referenceToLocalVariableAnalyzer;
	private final VariableDeclarationFragment targetDeclarationFragment;
	private boolean canFindReference = false;
	private boolean invalidBinding = false;
	private boolean continueVisiting = true;

	protected AbstractLocalVariableReferencesVisitor(CompilationUnit compilationUnit,
			VariableDeclarationFragment targetDeclarationFragment) {
		this.referenceToLocalVariableAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				targetDeclarationFragment);
		this.targetDeclarationFragment = targetDeclarationFragment;
	}

	@Override
	public final boolean preVisit2(ASTNode node) {
		return continueVisiting;
	}

	@Override
	public final boolean visit(VariableDeclarationFragment node) {
		if (targetDeclarationFragment == node) {
			canFindReference = true;
		}
		return true;
	}

	@Override
	public final boolean visit(SimpleName node) {
		if (canFindReference) {
			try {
				if (referenceToLocalVariableAnalyzer.isReference(node)) {
					referenceFound(node);
				}
			} catch (UnresolvedBindingException e) {
				invalidBinding = true;
				stopVisiting();
			}
		}
		return false;
	}
	
	/**
	 * Optimization ?
	 */
	@Override
	public final boolean visit(QualifiedName node) {
		return !ExcludeVariableBinding.isVariableBindingExcludedFor(node);
	}

	protected final void stopVisiting() {
		continueVisiting = false;
	}

	public boolean isInvalidBinding() {
		return invalidBinding;
	}

	protected abstract void referenceFound(SimpleName simpleName);
}

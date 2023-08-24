package eu.jsparrow.rules.common.visitor.helper;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
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

	protected AbstractLocalVariableReferencesVisitor(CompilationUnit compilationUnit,
			VariableDeclarationFragment targetDeclarationFragment) {
		this.referenceToLocalVariableAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				targetDeclarationFragment);
		this.targetDeclarationFragment = targetDeclarationFragment;
	}

	@Override
	public final boolean preVisit2(ASTNode node) {
		if (invalidBinding) {
			return false;
		}
		return continueVisiting(node);
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
			}
		}
		return false;
	}

	public boolean isInvalidBinding() {
		return invalidBinding;
	}

	protected abstract void referenceFound(SimpleName simpleName);

	protected abstract boolean continueVisiting(ASTNode node);
}

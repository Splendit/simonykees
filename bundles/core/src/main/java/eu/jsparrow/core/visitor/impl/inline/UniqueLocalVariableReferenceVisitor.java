package eu.jsparrow.core.visitor.impl.inline;

import java.util.Optional;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.visitor.helper.LocalVariableReferencesCollectorVisitor;

/**
 * Finds out whether a local variable is or is not referenced exactly once. If a
 * local variable is referenced exactly once, then it may be possible to in-line
 * it.
 * 
 * @since 4.19.0
 *
 */
class UniqueLocalVariableReferenceVisitor extends LocalVariableReferencesCollectorVisitor {

	private final SimpleName expectedReference;
	private boolean unsupportedReferenceFound = false;

	public UniqueLocalVariableReferenceVisitor(CompilationUnit compilationUnit,
			VariableDeclarationFragment declarationFragment, SimpleName expectedReference) {
		super(compilationUnit, declarationFragment);
		this.expectedReference = expectedReference;
	}

	@Override
	public void endVisit(SimpleName node) {
		unsupportedReferenceFound = hasUnsupportedReference();
		if (unsupportedReferenceFound) {
			stopVisiting();
		}
	}

	boolean hasUnsupportedReference() {
		if (references.isEmpty()) {
			return false;
		}
		if (references.size() > 1) {
			return true;
		}
		return references.get(0) != expectedReference;
	}

	/**
	 * 
	 * @return an Optional storing a SimpleName if the simple name represents
	 *         the only one reference to the specified local variable. In all
	 *         other cases, an empty Optional is returned.
	 */
	Optional<SimpleName> getUniqueLocalVariableReference() {
		if (unsupportedReferenceFound || isInvalidBinding()) {
			return Optional.empty();
		}
		if (references.size() == 1) {
			return Optional.of(references.get(0));
		}
		return Optional.empty();
	}
}

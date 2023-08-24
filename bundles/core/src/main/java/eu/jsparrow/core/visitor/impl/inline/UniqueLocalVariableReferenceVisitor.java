package eu.jsparrow.core.visitor.impl.inline;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.visitor.helper.AbstractLocalVariableReferencesVisitor;

/**
 * Finds out whether a local variable is or is not referenced exactly once. If a
 * local variable is referenced exactly once, then it may be possible to in-line
 * it.
 * 
 * @since 4.19.0
 *
 */
class UniqueLocalVariableReferenceVisitor extends AbstractLocalVariableReferencesVisitor {
	private final List<SimpleName> references = new ArrayList<>();

	public UniqueLocalVariableReferenceVisitor(CompilationUnit compilationUnit,
			VariableDeclarationFragment declarationFragment) {
		super(compilationUnit, declarationFragment);
	}

	@Override
	protected void referenceFound(SimpleName simpleName) {
		references.add(simpleName);
		if (references.size() > 1) {
			stopVisiting();
		}
	}

	/**
	 * 
	 * @return an Optional storing a SimpleName if the simple name represents
	 *         the only one reference to the specified local variable. In all
	 *         other cases, an empty Optional is returned.
	 */
	Optional<SimpleName> getUniqueLocalVariableReference() {
		if (references.size() == 1) {
			return Optional.of(references.get(0));
		}
		return Optional.empty();
	}
}

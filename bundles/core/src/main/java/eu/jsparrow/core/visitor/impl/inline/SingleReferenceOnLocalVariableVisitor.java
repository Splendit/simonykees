package eu.jsparrow.core.visitor.impl.inline;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.visitor.helper.ReferenceToLocalVariableAnalyzer;

/**
 * Finds out whether a local variable is or is not referenced exactly once. If a
 * local variable is referenced exactly once, then it may be possible to in-line
 * it.
 * 
 * @since 4.19.0
 *
 */
class SingleReferenceOnLocalVariableVisitor extends ASTVisitor {
	private final List<SimpleName> references;
	private final ReferenceToLocalVariableAnalyzer referenceAnalyzer;
	private final VariableDeclarationFragment declarationFragment;
	private boolean declarationFragmentFound = false;

	public SingleReferenceOnLocalVariableVisitor(CompilationUnit compilationUnit,
			VariableDeclarationFragment declarationFragment) {
		this.references = new ArrayList<>();
		this.declarationFragment = declarationFragment;
		this.referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit, declarationFragment);
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return references.size() < 2;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		if (declarationFragment == node) {
			declarationFragmentFound = true;
		}
		return true;
	}

	@Override
	public boolean visit(SimpleName node) {
		if (isReference(node)) {
			references.add(node);
		}
		return false;
	}

	private boolean isReference(SimpleName node) {
		return declarationFragmentFound && referenceAnalyzer.isReference(node);
	}

	/**
	 * 
	 * @return an Optional storing a SimpleName if the simple name represents
	 *         the only one reference to the specified local variable. In all
	 *         other cases, an empty Optional is returned.
	 */
	Optional<SimpleName> getSingleLocalVariableReference() {
		if (references.size() == 1) {
			return Optional.of(references.get(0));
		}
		return Optional.empty();
	}
}

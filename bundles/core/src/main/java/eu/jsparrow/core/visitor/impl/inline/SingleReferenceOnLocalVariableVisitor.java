package eu.jsparrow.core.visitor.impl.inline;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

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
	private final CompilationUnit compilationUnit;
	private final VariableDeclarationFragment declarationFragment;
	private final String targetIdentifier;

	public SingleReferenceOnLocalVariableVisitor(CompilationUnit compilationUnit,
			VariableDeclarationFragment declarationFragment) {
		this.references = new ArrayList<>();
		this.compilationUnit = compilationUnit;
		this.declarationFragment = declarationFragment;
		this.targetIdentifier = declarationFragment.getName()
			.getIdentifier();
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return references.size() < 2;
	}

	@Override
	public boolean visit(SimpleName node) {
		if (isReference(node)) {
			references.add(node);
		}
		return false;
	}

	private boolean isReference(SimpleName node) {

		if (node.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY ||
				node.getLocationInParent() == SingleVariableDeclaration.NAME_PROPERTY ||
				node.getLocationInParent() == EnumConstantDeclaration.NAME_PROPERTY ||
				node.getLocationInParent() == FieldAccess.NAME_PROPERTY ||
				node.getLocationInParent() == SuperFieldAccess.NAME_PROPERTY ||
				node.getLocationInParent() == QualifiedName.NAME_PROPERTY

		) {
			return false;
		}

		if (!node.getIdentifier()
			.equals(targetIdentifier)) {
			return false;
		}

		IBinding binding = node.resolveBinding();
		if (binding == null) {
			return false;
		}
		if (binding.getKind() != IBinding.VARIABLE) {
			return false;
		}
		IVariableBinding variableBinding = (IVariableBinding) binding;
		if (variableBinding.isField() || variableBinding.isParameter()) {
			return false;
		}

		ASTNode declaringNode = compilationUnit.findDeclaringNode(variableBinding);
		return declaringNode == declarationFragment;
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

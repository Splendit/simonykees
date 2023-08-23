package eu.jsparrow.core.visitor.impl.inline;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.exception.UnresolvedBindingException;
import eu.jsparrow.rules.common.visitor.helper.FindVariableBinding;

/**
 * Finds out whether a local variable is or is not referenced exactly once. If a
 * local variable is referenced exactly once, then it may be possible to in-line
 * it.
 * 
 * @since 4.19.0
 *
 */
public abstract class AbstractLocalVariableReferencesVisitor extends ASTVisitor {
	private final CompilationUnit compilationUnit;
	private final VariableDeclarationFragment declarationFragment;
	private final String targetIdentifier;
	private boolean declarationFragmentFound = false;

	protected AbstractLocalVariableReferencesVisitor(CompilationUnit compilationUnit,
			VariableDeclarationFragment declarationFragment) {
		this.compilationUnit = compilationUnit;
		this.declarationFragment = declarationFragment;
		this.targetIdentifier = declarationFragment.getName()
			.getIdentifier();
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
		try {
			if (isReference(node)) {
				referenceFound(node);
			}
		} catch (UnresolvedBindingException e) {
			handleUnresolvedBinding(node, e);
		}
		return false;
	}

	/**
	 * 
	 * @param node
	 * @return true if the SimpleName specified by the parameter is a reference
	 *         to the variable declared by the VariableDeclarationFragment
	 *         {@link #declarationFragment}, otherwise false.
	 *         <P>
	 *         Note that if the parent node of the SimpleName specified by the
	 *         parameter is the same object as {@link #declarationFragment},
	 *         then also false is returned.
	 * @throws UnresolvedBindingException
	 */
	public boolean isReference(SimpleName node) throws UnresolvedBindingException {

		if (!declarationFragmentFound) {
			return false;
		}

		if (node.getParent() == declarationFragment) {
			return false;
		}

		if (!node.getIdentifier()
			.equals(targetIdentifier)) {
			return false;
		}

		final StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
		if (locationInParent == VariableDeclarationFragment.NAME_PROPERTY ||
				locationInParent == SingleVariableDeclaration.NAME_PROPERTY ||
				locationInParent == EnumConstantDeclaration.NAME_PROPERTY ||
				locationInParent == FieldAccess.NAME_PROPERTY ||
				locationInParent == SuperFieldAccess.NAME_PROPERTY ||
				locationInParent == QualifiedName.NAME_PROPERTY

		) {
			return false;
		}

		IVariableBinding variableBinding = FindVariableBinding.findVariableBinding(node)
			.orElse(null);

		if (variableBinding == null || variableBinding.isField() || variableBinding.isParameter()) {
			return false;
		}

		ASTNode declaringNode = compilationUnit.findDeclaringNode(variableBinding);
		return declaringNode == declarationFragment;
	}

	protected abstract void referenceFound(SimpleName simpleName);

	protected abstract void handleUnresolvedBinding(SimpleName simpleName, UnresolvedBindingException exception);
}

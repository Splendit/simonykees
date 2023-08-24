package eu.jsparrow.rules.common.visitor.helper;

import org.eclipse.jdt.core.dom.ASTNode;
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

public class ReferenceToLocalVariableAnalyzer {

	private final CompilationUnit compilationUnit;
	private final VariableDeclarationFragment targetDeclarationFragment;
	private final String targetIdentifier;

	public ReferenceToLocalVariableAnalyzer(CompilationUnit compilationUnit,
			VariableDeclarationFragment declarationFragment) {
		this.compilationUnit = compilationUnit;
		this.targetDeclarationFragment = declarationFragment;
		this.targetIdentifier = declarationFragment.getName()
			.getIdentifier();
	}

	/**
	 * 
	 * @param node
	 * @return true if the SimpleName specified by the parameter is a reference
	 *         to the variable declared by the VariableDeclarationFragment
	 *         {@link #targetDeclarationFragment}, otherwise false.
	 *         <P>
	 *         Note that if the parent node of the SimpleName specified by the
	 *         parameter is the same object as {@link #targetDeclarationFragment},
	 *         then also false is returned.
	 * @throws UnresolvedBindingException
	 */
	public boolean isReference(SimpleName node) throws UnresolvedBindingException {

		if (node.getParent() == targetDeclarationFragment) {
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
		return declaringNode == targetDeclarationFragment;
	}
}

package eu.jsparrow.rules.common.visitor.helper;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * This class is planned to be removed. A part of the logics of this class are
 * contained in NodeDeclaringVariableHelper.
 * 
 */
@Deprecated
public class ReferenceToLocalVariableAnalyzer {

	private final CompilationUnit compilationUnit;
	private final VariableDeclarationFragment declarationFragment;
	private final String targetIdentifier;

	public ReferenceToLocalVariableAnalyzer(CompilationUnit compilationUnit,
			VariableDeclarationFragment declarationFragment) {
		this.compilationUnit = compilationUnit;
		this.declarationFragment = declarationFragment;
		this.targetIdentifier = declarationFragment.getName()
			.getIdentifier();
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
	 */
	public boolean isReference(SimpleName node) {

		if (node.getParent() == declarationFragment) {
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
}

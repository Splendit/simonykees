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
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

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

	public boolean isReference(SimpleName node) {

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
}

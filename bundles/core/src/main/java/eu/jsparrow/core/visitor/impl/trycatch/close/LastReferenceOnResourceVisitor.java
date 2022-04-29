package eu.jsparrow.core.visitor.impl.trycatch.close;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.IVariableBinding;

class LastReferenceOnResourceVisitor extends ASTVisitor {
	private final CompilationUnit compilationUnit;
	private final VariableDeclarationFragment resourceDeclaration;
	private final SimpleName declarationFragmentName;
	private final String resourceIdentifier;
	private SimpleName lastReference = null;

	LastReferenceOnResourceVisitor(VariableDeclarationFragment resourceDeclaration, CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		this.resourceDeclaration = resourceDeclaration;
		this.declarationFragmentName = resourceDeclaration.getName();
		this.resourceIdentifier = declarationFragmentName.getIdentifier();
	}

	@Override
	public boolean visit(SimpleName node) {
		if (isReference(node)) {
			lastReference = node;
		}
		return false;
	}

	private boolean isReference(SimpleName simpleName) {
		if (simpleName == declarationFragmentName) {
			return false;
		}
		if (!simpleName.getIdentifier()
			.equals(resourceIdentifier)) {
			return false;
		}
		IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			return false;
		}
		if (binding.getKind() != IBinding.VARIABLE) {
			return false;
		}
		IVariableBinding variableBinding = (IVariableBinding) binding;
		return compilationUnit.findDeclaringNode(variableBinding) == resourceDeclaration;
	}

	public Optional<SimpleName> getLastReference() {
		return Optional.ofNullable(lastReference);
	}

}

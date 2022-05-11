package eu.jsparrow.core.visitor.impl.trycatch.close;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Intended to be used to visit the body of a Try With Resources statement in
 * order to find out the last reference on a given resource declaration.
 *
 */
class LastReferenceOnResourceVisitor extends ASTVisitor {
	private final CompilationUnit compilationUnit;
	private final VariableDeclarationFragment resourceDeclaration;
	private final String resourceIdentifier;
	private SimpleName lastReference = null;

	LastReferenceOnResourceVisitor(VariableDeclarationFragment resourceDeclaration, CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		this.resourceDeclaration = resourceDeclaration;
		this.resourceIdentifier = resourceDeclaration.getName()
			.getIdentifier();
	}

	@Override
	public boolean visit(SimpleName node) {
		if (isReference(node)) {
			lastReference = node;
		}
		return false;
	}

	private boolean isReference(SimpleName simpleName) {

		if (!simpleName.getIdentifier()
			.equals(resourceIdentifier)) {
			return false;
		}

		if (ASTNodeUtil.isLabel(simpleName)) {
			return false;
		}

		IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			return false;
		}
		
		return compilationUnit.findDeclaringNode(binding) == resourceDeclaration;
	}

	public Optional<SimpleName> getLastReference() {
		return Optional.ofNullable(lastReference);
	}

}

package eu.jsparrow.core.visitor.unused;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;

public class NonPrivateUnusedFieldCandidate {

	private VariableDeclarationFragment fragment;
	private CompilationUnit compilationUnit;
	private AbstractTypeDeclaration typeDeclaration;
	private JavaAccessModifier accessModifier;
	private List<ExpressionStatement> internalReassignments;

	public NonPrivateUnusedFieldCandidate(VariableDeclarationFragment fragment, CompilationUnit compilationUnit,
			AbstractTypeDeclaration typeDeclaration, JavaAccessModifier accessModifier,
			List<ExpressionStatement> internalReassignments) {
		this.fragment = fragment;
		this.compilationUnit = compilationUnit;
		this.typeDeclaration = typeDeclaration;
		this.accessModifier = accessModifier;
		this.internalReassignments = internalReassignments;
	}

	public VariableDeclarationFragment getFragment() {
		return fragment;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public AbstractTypeDeclaration getTypeDeclaration() {
		return typeDeclaration;
	}

	public JavaAccessModifier getAccessModifier() {
		return accessModifier;
	}

	public List<ExpressionStatement> getInternalReassignments() {
		return internalReassignments;
	}

	@Override
	public int hashCode() {
		return Objects.hash(accessModifier, compilationUnit, fragment, internalReassignments, typeDeclaration);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NonPrivateUnusedFieldCandidate)) {
			return false;
		}
		NonPrivateUnusedFieldCandidate other = (NonPrivateUnusedFieldCandidate) obj;
		return accessModifier == other.accessModifier && Objects.equals(compilationUnit, other.compilationUnit)
				&& Objects.equals(fragment, other.fragment)
				&& Objects.equals(internalReassignments, other.internalReassignments)
				&& Objects.equals(typeDeclaration, other.typeDeclaration);
	}

	
}

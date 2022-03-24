package eu.jsparrow.core.visitor.unused.type;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;

import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;

/**
 * Wraps information (the declaration fragment, the compilation unit, the access
 * modifier, and the reassignments within the compilation unit) about
 * non-private fields that are not used in the compilation unit where they are
 * declared.
 * 
 * @since 4.8.0
 *
 */
public class NonPrivateUnusedClassCandidate {

	private AbstractTypeDeclaration typeDeclaration;
	private AbstractTypeDeclaration enclosingTypeDeclaration;
	private JavaAccessModifier accessModifier;

	public NonPrivateUnusedClassCandidate(AbstractTypeDeclaration typeDeclaration, JavaAccessModifier accessModifier) {
		this.typeDeclaration = typeDeclaration;
		this.accessModifier = accessModifier;
	}

	public NonPrivateUnusedClassCandidate(AbstractTypeDeclaration typeDeclaration, JavaAccessModifier accessModifier,
			AbstractTypeDeclaration enclosingTypeDeclaration) {
		this(typeDeclaration, accessModifier);
		this.enclosingTypeDeclaration = enclosingTypeDeclaration;
	}

	public AbstractTypeDeclaration getTypeDeclaration() {
		return typeDeclaration;
	}

	public JavaAccessModifier getAccessModifier() {
		return accessModifier;
	}

	public Optional<AbstractTypeDeclaration> getEnclosingTypeDeclaration() {
		return Optional.ofNullable(enclosingTypeDeclaration);
	}

	public void setEnclosingTypeDeclaration(AbstractTypeDeclaration enclosingTypeDeclaration) {
		this.enclosingTypeDeclaration = enclosingTypeDeclaration;
	}

	@Override
	public int hashCode() {
		return Objects.hash(accessModifier, typeDeclaration, enclosingTypeDeclaration);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NonPrivateUnusedClassCandidate)) {
			return false;
		}
		NonPrivateUnusedClassCandidate other = (NonPrivateUnusedClassCandidate) obj;
		return accessModifier == other.accessModifier
				&& Objects.equals(typeDeclaration, other.typeDeclaration)
				&& Objects.equals(enclosingTypeDeclaration, other.enclosingTypeDeclaration);
	}
}

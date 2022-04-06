package eu.jsparrow.core.visitor.unused.type;

import java.util.Objects;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;

import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;

/**
 * Wraps information (the type declaration, the access modifier) about
 * non-private types that are not used in the compilation unit where they are
 * declared.
 * 
 * @since 4.10.0
 *
 */
public class NonPrivateUnusedTypeCandidate {

	private AbstractTypeDeclaration typeDeclaration;
	private JavaAccessModifier accessModifier;
	private boolean mainType;

	public NonPrivateUnusedTypeCandidate(AbstractTypeDeclaration typeDeclaration, JavaAccessModifier accessModifier,
			boolean mainType) {
		this.typeDeclaration = typeDeclaration;
		this.accessModifier = accessModifier;
		this.mainType = mainType;
	}

	public AbstractTypeDeclaration getTypeDeclaration() {
		return typeDeclaration;
	}

	public JavaAccessModifier getAccessModifier() {
		return accessModifier;
	}

	public boolean isMainType() {
		return mainType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(accessModifier, typeDeclaration);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NonPrivateUnusedTypeCandidate)) {
			return false;
		}
		NonPrivateUnusedTypeCandidate other = (NonPrivateUnusedTypeCandidate) obj;
		return accessModifier == other.accessModifier
				&& Objects.equals(typeDeclaration, other.typeDeclaration);
	}
}

package eu.jsparrow.core.visitor.unused;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

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
public class NonPrivateUnusedFieldCandidate {

	private VariableDeclarationFragment fragment;
	private JavaAccessModifier accessModifier;
	private List<ExpressionStatement> internalReassignments;

	public NonPrivateUnusedFieldCandidate(VariableDeclarationFragment fragment, JavaAccessModifier accessModifier,
			List<ExpressionStatement> internalReassignments) {
		this.fragment = fragment;
		this.accessModifier = accessModifier;
		this.internalReassignments = internalReassignments;
	}

	public VariableDeclarationFragment getFragment() {
		return fragment;
	}

	public JavaAccessModifier getAccessModifier() {
		return accessModifier;
	}

	public List<ExpressionStatement> getInternalReassignments() {
		return internalReassignments;
	}

	@Override
	public int hashCode() {
		return Objects.hash(accessModifier, fragment, internalReassignments);
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
		return accessModifier == other.accessModifier
				&& Objects.equals(fragment, other.fragment)
				&& Objects.equals(internalReassignments, other.internalReassignments);
	}
}

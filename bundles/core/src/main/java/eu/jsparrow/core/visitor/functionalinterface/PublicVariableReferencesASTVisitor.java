package eu.jsparrow.core.visitor.functionalinterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * A visitor for finding references of variables that are not local variables
 * and are <em>NOT</em> assigned <em>inside</em> the node which is being
 * visited.
 * 
 * @author Ardit Ymeri
 * @since 2.0
 *
 */
public class PublicVariableReferencesASTVisitor extends ASTVisitor {

	private List<SimpleName> publicVariableReferences = new ArrayList<>();
	private Set<String> localVariableNames = new HashSet<>();
	private List<SimpleName> assignedPublicVariables = new ArrayList<>();

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {
		localVariableNames.add(fragment.getName()
			.getIdentifier());
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration singleVarDeclaration) {
		localVariableNames.add(singleVarDeclaration.getName()
			.getIdentifier());
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {

		IBinding binding = simpleName.resolveBinding();
		if (binding != null && binding.getKind() == IBinding.VARIABLE) {
			if (simpleName.getLocationInParent() == QualifiedName.NAME_PROPERTY) {
				return true;
			}

			if (Assignment.LEFT_HAND_SIDE_PROPERTY == simpleName.getLocationInParent() && !isReferenced(simpleName)) {
				assignedPublicVariables.add(simpleName);
			} else if (!isLocalVariable(simpleName)) {
				publicVariableReferences.add(simpleName);
			}
		}

		return true;
	}

	/**
	 * Checks whether a simple name matches with any of the names of the public
	 * variables referenced so far.
	 * 
	 * @param simpleName
	 *            a {@link SimpleName} to check for
	 * 
	 * @return {@code true} if the given name is referenced before, or
	 *         {@code false} otherwise
	 */
	private boolean isReferenced(SimpleName simpleName) {
		return getPublicVariableReferences().stream()
			.map(SimpleName::getIdentifier)
			.anyMatch(id -> id.equals(simpleName.getIdentifier()));
	}

	/**
	 * Checks if the given simple name matches with any of the names of the
	 * local variables explored so far.
	 * 
	 * @param simpleName
	 *            a {@link SimpleName} to look for
	 * 
	 * @return {@code true} if a match is found, or {@code false} otherwise.
	 */
	private boolean isLocalVariable(SimpleName simpleName) {
		return simpleName.getLocationInParent() != FieldAccess.NAME_PROPERTY && localVariableNames.stream()
			.anyMatch(s -> s.equals(simpleName.getIdentifier()));
	}

	public List<SimpleName> getPublicVariableReferences() {
		return this.publicVariableReferences;
	}

	/**
	 * Returns the list of public variable references that are not assigned
	 * inside the node that is being visited.
	 * 
	 * @return a list of {@link SimpleName}s.
	 */
	public List<SimpleName> getUnassignedVariableReferences() {
		return publicVariableReferences.stream()
			.filter(ref -> assignedPublicVariables.stream()
				.noneMatch(assigned -> assigned.getIdentifier()
					.equals(ref.getIdentifier())))
			.collect(Collectors.toList());
	}

	public List<SimpleName> getAssignedPublicVariables() {
		return this.assignedPublicVariables;
	}
}

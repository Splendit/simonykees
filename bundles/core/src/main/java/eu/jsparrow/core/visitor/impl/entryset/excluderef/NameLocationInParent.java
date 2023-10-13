package eu.jsparrow.core.visitor.impl.entryset.excluderef;

import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import eu.jsparrow.core.visitor.impl.entryset.SimpleNamesCollectorVisitor;
import eu.jsparrow.rules.common.visitor.helper.ExcludeVariableBinding;

/**
 * Offers useful methods analyzing the location of a name in its parent node.
 * 
 * TODO:
 * <ul>
 * <li>Move this class to a more public place.</li>
 * <li>Make sure that everywhere where it is necessary, this class is used
 * instead of {@link ExcludeVariableBinding}</li>
 * <li>Move {@link eu.jsparrow.rules.common.visitor.helper.FindVariableBinding},
 * {@link eu.jsparrow.rules.common.visitor.helper.ReferenceToLocalVariableAnalyzer},
 * {@link SimpleNamesCollectorVisitor} and other classes handling variable
 * references to the same place as {@link NameLocationInParent}</li>
 * <li>Re-factor everything in a way that
 * {@link eu.jsparrow.rules.common.visitor.helper.FindVariableBinding} does not
 * any more reference {@link ExcludeVariableBinding}.</li>
 * <li>remove also {@code ExcludeVariableBindingTest} because
 * {@code NameLocationInParentTest} covers everything.</li>
 * <li>At last, remove {@link ExcludeVariableBinding} and all corresponding
 * tests which only apply the the removed class.</li>
 * </ul>
 */
public class NameLocationInParent {

	public static boolean canBeReferenceToLocalVariable(SimpleName simpleName) {
		return !isReferenceToLocalVariableExcludedFor(simpleName);
	}

	public static boolean isReferenceToLocalVariableExcludedFor(SimpleName simpleName) {
		return ExcludeReferenceByProperty.isReferenceToLocalVariableExcludedFor(simpleName.getLocationInParent());
	}

	public static boolean canHaveVariableBinding(SimpleName simpleName) {
		return !isVariableBindingExcludedFor(simpleName);
	}

	public static boolean isVariableBindingExcludedFor(SimpleName simpleName) {
		StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
		if (locationInParent == QualifiedName.NAME_PROPERTY || locationInParent == QualifiedName.QUALIFIER_PROPERTY) {
			return isVariableBindingExcludedFor((QualifiedName) simpleName.getParent());
		}
		return ExcludeReferenceByProperty.isReferenceToVariableExcluded4SimpleName(locationInParent);
	}

	public static boolean isVariableBindingExcludedFor(QualifiedName qualifiedName) {
		StructuralPropertyDescriptor locationInParent = qualifiedName.getLocationInParent();
		if (locationInParent == QualifiedName.QUALIFIER_PROPERTY) {
			return isVariableBindingExcludedFor((QualifiedName) qualifiedName.getParent());
		}
		return ExcludeReferenceByProperty.isReferenceToVariableExcluded4QualifiedName(locationInParent);
	}

	/**
	 * Private default constructor hiding implicit public one.
	 */
	private NameLocationInParent() {

	}

}

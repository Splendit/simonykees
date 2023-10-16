package eu.jsparrow.core.visitor.impl.entryset.excluderef;

import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import eu.jsparrow.core.visitor.impl.entryset.SimpleNamesCollectorVisitor;
import eu.jsparrow.rules.common.visitor.helper.ExcludeVariableBinding;

/**
 * This class offers methods to determine whether or not a reference to a local
 * variable or a field can be excluded for a given name without the expensive
 * resolving of its binding. This is done by analyzing the location of the given
 * qualified name or simple name in its parent.
 * 
 * TODO:
 * <ul>
 * <li>Move this class to a more public place.</li>
 * <li>Make sure that everywhere where it is necessary, this class is used
 * instead of {@link ExcludeVariableBinding}</li>
 * <li>Move {@link eu.jsparrow.rules.common.visitor.helper.FindVariableBinding},
 * {@link eu.jsparrow.rules.common.visitor.helper.ReferenceToLocalVariableAnalyzer},
 * {@link SimpleNamesCollectorVisitor} and other classes handling variable
 * references to the same place as {@link ExcludeVariableReference}</li>
 * <li>Re-factor everything in a way that
 * {@link eu.jsparrow.rules.common.visitor.helper.FindVariableBinding} does not
 * any more reference {@link ExcludeVariableBinding}.</li>
 * <li>remove also {@code ExcludeVariableBindingTest} because
 * {@code NameLocationInParentTest} covers everything.</li>
 * <li>At last, remove {@link ExcludeVariableBinding} and all corresponding
 * tests which only apply the the removed class.</li>
 * </ul>
 * 
 * @since 4.20.0
 */
public class ExcludeVariableReference {

	/**
	 * @return true if a reference to a local variable can be excluded for the
	 *         specified simple name.
	 */
	public static boolean isReferenceToLocalVariableExcludedFor(SimpleName simpleName) {
		return ExcludeReferenceByProperty.isReferenceToLocalVariableExcludedFor(simpleName.getLocationInParent());
	}

	public static boolean isReferenceToVariableExcludedFor(SimpleName simpleName) {
		StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
		if (locationInParent == QualifiedName.NAME_PROPERTY || locationInParent == QualifiedName.QUALIFIER_PROPERTY) {
			return isReferenceToVariableExcludedFor((QualifiedName) simpleName.getParent());
		}
		return ExcludeReferenceByProperty.isReferenceToVariableExcluded4SimpleName(locationInParent);
	}

	public static boolean isReferenceToVariableExcludedFor(QualifiedName qualifiedName) {
		StructuralPropertyDescriptor locationInParent = qualifiedName.getLocationInParent();
		if (locationInParent == QualifiedName.QUALIFIER_PROPERTY) {
			return isReferenceToVariableExcludedFor((QualifiedName) qualifiedName.getParent());
		}
		return ExcludeReferenceByProperty.isReferenceToVariableExcluded4QualifiedName(locationInParent);
	}

	/**
	 * Private default constructor hiding implicit public one.
	 */
	private ExcludeVariableReference() {

	}

}

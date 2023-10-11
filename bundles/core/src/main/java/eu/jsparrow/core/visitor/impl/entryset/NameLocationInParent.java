package eu.jsparrow.core.visitor.impl.entryset;

import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.visitor.helper.ExcludeVariableBinding;

/**
 * Offers useful methods analyzing the location of a name in its parent node.
 * 
 * TODO:
 * <ul>
 * <li>Move {@link ExcludeVariableBinding} to a more public place</li>
 * <li>Move this class to the same place as {@link ExcludeVariableBinding}</li>
 * <l>Move all functionalities of {@link ExcludeVariableBinding} into this
 * class.</li>
 * <li>Move {@link eu.jsparrow.rules.common.visitor.helper.FindVariableBinding},
 * {@link eu.jsparrow.rules.common.visitor.helper.ReferenceToLocalVariableAnalyzer},
 * {@link SimpleNamesCollectorVisitor} and other classes handling variable
 * references to the same place as {@link NameLocationInParent}</li>
 * <li>Re-factor everything in a way that
 * {@link eu.jsparrow.rules.common.visitor.helper.FindVariableBinding} does not
 * any more reference {@link ExcludeVariableBinding}.</li>
 * <li>remove also {@code ExcludeVariableBindingTest} because
 * {@code NameLocationInParentTest} covers everything.</li>
 * </ul>
 */
public class NameLocationInParent {

	public static boolean canBeReferenceToLocalVariable(SimpleName simpleName) {
		return !isReferenceToLocalVariableExcludedFor(simpleName);
	}

	public static boolean isReferenceToLocalVariableExcludedFor(SimpleName simpleName) {
		final StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
		return locationInParent == VariableDeclarationFragment.NAME_PROPERTY ||
				locationInParent == SingleVariableDeclaration.NAME_PROPERTY ||
				locationInParent == EnumConstantDeclaration.NAME_PROPERTY ||
				locationInParent == FieldAccess.NAME_PROPERTY ||
				locationInParent == SuperFieldAccess.NAME_PROPERTY ||
				locationInParent == QualifiedName.NAME_PROPERTY ||
				isVariableBindingExcludedFor(simpleName);
	}

	public static boolean canHaveVariableBinding(SimpleName simpleName) {
		return !isVariableBindingExcludedFor(simpleName);
	}
	
	/**
	 * TODO: move
	 * {@link ExcludeVariableBinding#isVariableBindingExcludedFor(SimpleName)}
	 * into this class.
	 */
	public static boolean isVariableBindingExcludedFor(SimpleName simpleName) {
		return ExcludeVariableBinding.isVariableBindingExcludedFor(simpleName);
	}
	
	/**
	 * TODO: move
	 * {@link ExcludeVariableBinding#isVariableBindingExcludedFor(QualifiedName)}
	 * into this class.
	 */
	public static boolean isVariableBindingExcludedFor(QualifiedName qualifiedName) {
		return ExcludeVariableBinding.isVariableBindingExcludedFor(qualifiedName);
	}


	/**
	 * Private default constructor hiding implicit public one.
	 */
	private NameLocationInParent() {

	}

}

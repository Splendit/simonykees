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
 * TODO: Move this class to a more public place, also re-factor and move
 * {@link eu.jsparrow.rules.common.visitor.helper.ReferenceToLocalVariableAnalyzer}.
 */
public class NameLocationInParent {

	public static boolean canBeReferenceToLocalVariable(SimpleName simpleName) {
		final StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
		if (locationInParent == VariableDeclarationFragment.NAME_PROPERTY ||
				locationInParent == SingleVariableDeclaration.NAME_PROPERTY ||
				locationInParent == EnumConstantDeclaration.NAME_PROPERTY ||
				locationInParent == FieldAccess.NAME_PROPERTY ||
				locationInParent == SuperFieldAccess.NAME_PROPERTY ||
				locationInParent == QualifiedName.NAME_PROPERTY

		) {
			return false;
		}

		return canHaveVariableBinding(simpleName);
	}

	/**
	 * TODO: move
	 * {@link ExcludeVariableBinding#isVariableBindingExcludedFor(SimpleName)}
	 * into this class.
	 */
	public static boolean canHaveVariableBinding(SimpleName simpleName) {
		return !ExcludeVariableBinding.isVariableBindingExcludedFor(simpleName);
	}

	/**
	 * Private default constructor hiding implicit public one.
	 */
	private NameLocationInParent() {

	}

}

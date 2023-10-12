package eu.jsparrow.core.visitor.impl.entryset;

import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
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

	public static boolean isVariableBindingExcludedFor(SimpleName simpleName) {
		if (ASTNodeUtil.isLabel(simpleName)) {
			return true;
		}
		if (simpleName.getLocationInParent() == QualifiedName.NAME_PROPERTY) {
			return isVariableBindingExcludedFor((QualifiedName) simpleName.getParent());
		}
		return simpleName.getLocationInParent() == MethodInvocation.NAME_PROPERTY ||
				simpleName.getLocationInParent() == SuperMethodInvocation.NAME_PROPERTY ||
				simpleName.getLocationInParent() == ExpressionMethodReference.NAME_PROPERTY ||
				simpleName.getLocationInParent() == SuperMethodReference.NAME_PROPERTY ||
				simpleName.getLocationInParent() == TypeMethodReference.NAME_PROPERTY ||
				simpleName.getLocationInParent() == MethodDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == TypeDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == EnumDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == AnnotationTypeDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == AnnotationTypeMemberDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == RecordDeclaration.NAME_PROPERTY ||
				// simpleName.getLocationInParent() == SimpleType.NAME_PROPERTY ||
				simpleName.getLocationInParent() == QualifiedType.NAME_PROPERTY ||
				simpleName.getLocationInParent() == NameQualifiedType.NAME_PROPERTY ||
				// simpleName.getLocationInParent() == NameQualifiedType.QUALIFIER_PROPERTY ||
				simpleName.getLocationInParent() == MemberValuePair.NAME_PROPERTY ||
				isVariableReferenceExcludedForName(simpleName);
	}

	public static boolean isVariableBindingExcludedFor(QualifiedName qualifiedName) {
		return isVariableReferenceExcludedForName(qualifiedName);
	}

	private static boolean isVariableReferenceExcludedForName(Name name) {
		StructuralPropertyDescriptor locationInParent = name.getLocationInParent();
		return locationInParent == SimpleType.NAME_PROPERTY ||
				locationInParent == NameQualifiedType.QUALIFIER_PROPERTY ||
				locationInParent == MarkerAnnotation.TYPE_NAME_PROPERTY ||
				locationInParent == SingleMemberAnnotation.TYPE_NAME_PROPERTY ||
				locationInParent == NormalAnnotation.TYPE_NAME_PROPERTY;
	}

	/**
	 * Private default constructor hiding implicit public one.
	 */
	private NameLocationInParent() {

	}

}

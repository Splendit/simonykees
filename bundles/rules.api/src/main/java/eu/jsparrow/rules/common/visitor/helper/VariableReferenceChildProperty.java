package eu.jsparrow.rules.common.visitor.helper;

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

/**
 * Utility class to find out whether a SimpleName can represent a reference to a
 * variable. This is done by analyzing the location of the specified SimpleName
 * in its parent node.
 * 
 *
 */
public class VariableReferenceChildProperty {

	public static boolean canBeLocalVariableReference(SimpleName simpleName) {
		return canBeVariableReference(simpleName, true);
	}

	public static boolean canBeVariableReference(SimpleName simpleName) {
		return canBeVariableReference(simpleName, false);
	}

	private static boolean canBeVariableReference(SimpleName simpleName, boolean expectingLocalVariable) {
		if (expectingLocalVariable) {
			if (simpleName.getLocationInParent() == FieldAccess.NAME_PROPERTY ||
					simpleName.getLocationInParent() == SuperFieldAccess.NAME_PROPERTY ||
					simpleName.getLocationInParent() == QualifiedName.NAME_PROPERTY) {
				return false;
			}
		} else if (simpleName.getLocationInParent() == QualifiedName.NAME_PROPERTY) {
			return !isVariableReferenceExcludedFor((QualifiedName) simpleName.getParent());
		}

		if (ASTNodeUtil.isLabel(simpleName)) {
			return false;
		}
		if (isDeclarationNameProperty(simpleName)) {
			return false;
		}

		if (isAnnotationTypeNameProperty(simpleName)) {
			return false;
		}

		if (isNamePropertyWithMethodBinding(simpleName)) {
			return false;
		}

		return simpleName.getLocationInParent() != SimpleType.NAME_PROPERTY &&
				simpleName.getLocationInParent() != QualifiedType.NAME_PROPERTY &&
				simpleName.getLocationInParent() != NameQualifiedType.NAME_PROPERTY &&
				simpleName.getLocationInParent() != NameQualifiedType.QUALIFIER_PROPERTY;
	}

	private static boolean isDeclarationNameProperty(SimpleName simpleName) {
		return simpleName.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY ||
				simpleName.getLocationInParent() == SingleVariableDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == EnumConstantDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == MethodDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == TypeDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == EnumDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == AnnotationTypeDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == AnnotationTypeMemberDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == RecordDeclaration.NAME_PROPERTY;
	}

	private static boolean isNamePropertyWithMethodBinding(SimpleName simpleName) {
		return simpleName.getLocationInParent() == MethodInvocation.NAME_PROPERTY &&
				simpleName.getLocationInParent() == SuperMethodInvocation.NAME_PROPERTY ||
				simpleName.getLocationInParent() == ExpressionMethodReference.NAME_PROPERTY ||
				simpleName.getLocationInParent() == SuperMethodReference.NAME_PROPERTY ||
				simpleName.getLocationInParent() == TypeMethodReference.NAME_PROPERTY ||
				simpleName.getLocationInParent() == MemberValuePair.NAME_PROPERTY;
	}

	private static boolean isAnnotationTypeNameProperty(Name name) {
		StructuralPropertyDescriptor locationInParent = name.getLocationInParent();
		return locationInParent == MarkerAnnotation.TYPE_NAME_PROPERTY ||
				locationInParent == SingleMemberAnnotation.TYPE_NAME_PROPERTY ||
				locationInParent == NormalAnnotation.TYPE_NAME_PROPERTY;
	}

	public static boolean isVariableReferenceExcludedFor(QualifiedName qualifiedName) {
		if (isAnnotationTypeNameProperty(qualifiedName)) {
			return true;
		}
		return qualifiedName.getLocationInParent() == SimpleType.NAME_PROPERTY ||
				qualifiedName.getLocationInParent() == NameQualifiedType.QUALIFIER_PROPERTY;
	}

	private VariableReferenceChildProperty() {
		// private default constructor to hide implicit public one
	}

}

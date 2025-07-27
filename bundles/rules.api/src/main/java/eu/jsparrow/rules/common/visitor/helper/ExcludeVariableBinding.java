package eu.jsparrow.rules.common.visitor.helper;

import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
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
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeMethodReference;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class ExcludeVariableBinding {

	public static boolean isVariableBindingExcludedFor(SimpleName simpleName) {
		if (ASTNodeUtil.isLabel(simpleName)) {
			return true;
		}
		if(simpleName.getLocationInParent() == QualifiedName.NAME_PROPERTY) {
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
				simpleName.getLocationInParent() == SimpleType.NAME_PROPERTY ||
				simpleName.getLocationInParent() == QualifiedType.NAME_PROPERTY ||
				simpleName.getLocationInParent() == NameQualifiedType.NAME_PROPERTY ||
				simpleName.getLocationInParent() == NameQualifiedType.QUALIFIER_PROPERTY ||
				simpleName.getLocationInParent() == MemberValuePair.NAME_PROPERTY ||
				isAnnotationTypeNameProperty(simpleName);
	}

	public static boolean isVariableBindingExcludedFor(QualifiedName qualifiedName) {
		return qualifiedName.getLocationInParent() == SimpleType.NAME_PROPERTY ||
				qualifiedName.getLocationInParent() == NameQualifiedType.QUALIFIER_PROPERTY ||
				isAnnotationTypeNameProperty(qualifiedName);
	}

	private static boolean isAnnotationTypeNameProperty(Name name) {
		StructuralPropertyDescriptor locationInParent = name.getLocationInParent();
		return locationInParent == MarkerAnnotation.TYPE_NAME_PROPERTY ||
				locationInParent == SingleMemberAnnotation.TYPE_NAME_PROPERTY ||
				locationInParent == NormalAnnotation.TYPE_NAME_PROPERTY;
	}

	private ExcludeVariableBinding() {
		// private default constructor hiding implicit public one
	}

}

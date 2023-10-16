package eu.jsparrow.core.visitor.impl.entryset.excluderef;

import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * @since 4.20.0
 *
 */
class ExcludeReferenceByProperty {

	static boolean isReferenceToLocalVariableExcludedFor(StructuralPropertyDescriptor locationInParent) {
		return locationInParent == FieldAccess.NAME_PROPERTY ||
				locationInParent == SuperFieldAccess.NAME_PROPERTY ||
				locationInParent == QualifiedName.NAME_PROPERTY ||
				isReferenceToVariableExcluded4SimpleName(locationInParent);
	}

	static boolean isReferenceToVariableExcluded4SimpleName(StructuralPropertyDescriptor locationInParent) {
		return locationInParent == LabeledStatement.LABEL_PROPERTY ||
				locationInParent == ContinueStatement.LABEL_PROPERTY ||
				locationInParent == BreakStatement.LABEL_PROPERTY ||
				locationInParent == MethodInvocation.NAME_PROPERTY ||
				locationInParent == SuperMethodInvocation.NAME_PROPERTY ||
				locationInParent == ExpressionMethodReference.NAME_PROPERTY ||
				locationInParent == SuperMethodReference.NAME_PROPERTY ||
				locationInParent == TypeMethodReference.NAME_PROPERTY ||
				locationInParent == VariableDeclarationFragment.NAME_PROPERTY ||
				locationInParent == SingleVariableDeclaration.NAME_PROPERTY ||
				locationInParent == MethodDeclaration.NAME_PROPERTY ||
				locationInParent == TypeDeclaration.NAME_PROPERTY ||
				locationInParent == EnumDeclaration.NAME_PROPERTY ||
				locationInParent == EnumConstantDeclaration.NAME_PROPERTY ||
				locationInParent == AnnotationTypeDeclaration.NAME_PROPERTY ||
				locationInParent == AnnotationTypeMemberDeclaration.NAME_PROPERTY ||
				locationInParent == RecordDeclaration.NAME_PROPERTY ||
				locationInParent == QualifiedType.NAME_PROPERTY ||
				locationInParent == NameQualifiedType.NAME_PROPERTY ||
				locationInParent == MemberValuePair.NAME_PROPERTY ||
				isReferenceToVariableExcluded4Name(locationInParent);
	}

	static boolean isReferenceToVariableExcluded4QualifiedName(StructuralPropertyDescriptor locationInParent) {
		return isReferenceToVariableExcluded4Name(locationInParent);
	}

	private static boolean isReferenceToVariableExcluded4Name(StructuralPropertyDescriptor locationInParent) {
		return locationInParent == SimpleType.NAME_PROPERTY ||
				locationInParent == NameQualifiedType.QUALIFIER_PROPERTY ||
				locationInParent == MarkerAnnotation.TYPE_NAME_PROPERTY ||
				locationInParent == SingleMemberAnnotation.TYPE_NAME_PROPERTY ||
				locationInParent == NormalAnnotation.TYPE_NAME_PROPERTY ||
				locationInParent == ThisExpression.QUALIFIER_PROPERTY ||
				locationInParent == SuperFieldAccess.QUALIFIER_PROPERTY ||
				locationInParent == SuperMethodInvocation.QUALIFIER_PROPERTY;
	}

	/**
	 * Private default constructor hiding implicit public one.
	 */
	private ExcludeReferenceByProperty() {

	}
}

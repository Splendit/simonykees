package eu.jsparrow.core.visitor.sub;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ExcludeReferenceByPropertyTest {

	private static Stream<Arguments> arguments_referenceToLocalVariableExcluded()
			throws Exception {
		return Stream.of(
				Arguments.of(FieldAccess.NAME_PROPERTY),
				Arguments.of(SuperFieldAccess.NAME_PROPERTY),
				Arguments.of(QualifiedName.NAME_PROPERTY),
				// structural property descriptors where the property type is
				// {@link org.eclipse.jdt.core.dom.SimpleName}.
				Arguments.of(LabeledStatement.LABEL_PROPERTY),
				Arguments.of(ContinueStatement.LABEL_PROPERTY),
				Arguments.of(BreakStatement.LABEL_PROPERTY),
				Arguments.of(MethodInvocation.NAME_PROPERTY),
				Arguments.of(SuperMethodInvocation.NAME_PROPERTY),
				Arguments.of(ExpressionMethodReference.NAME_PROPERTY),
				Arguments.of(SuperMethodReference.NAME_PROPERTY),
				Arguments.of(TypeMethodReference.NAME_PROPERTY),
				Arguments.of(MethodDeclaration.NAME_PROPERTY),
				Arguments.of(VariableDeclarationFragment.NAME_PROPERTY),
				Arguments.of(SingleVariableDeclaration.NAME_PROPERTY),
				Arguments.of(TypeDeclaration.NAME_PROPERTY),
				Arguments.of(EnumDeclaration.NAME_PROPERTY),
				Arguments.of(EnumConstantDeclaration.NAME_PROPERTY),
				Arguments.of(AnnotationTypeDeclaration.NAME_PROPERTY),
				Arguments.of(AnnotationTypeMemberDeclaration.NAME_PROPERTY),
				Arguments.of(RecordDeclaration.NAME_PROPERTY),
				Arguments.of(QualifiedType.NAME_PROPERTY),
				Arguments.of(NameQualifiedType.NAME_PROPERTY),
				Arguments.of(MemberValuePair.NAME_PROPERTY),
				// structural property descriptors where the property type is
				// {@link org.eclipse.jdt.core.dom.Name}.
				Arguments.of(SimpleType.NAME_PROPERTY),
				Arguments.of(NameQualifiedType.QUALIFIER_PROPERTY),
				Arguments.of(ThisExpression.QUALIFIER_PROPERTY),
				Arguments.of(SuperFieldAccess.QUALIFIER_PROPERTY),
				Arguments.of(SuperMethodInvocation.QUALIFIER_PROPERTY),
				Arguments.of(MarkerAnnotation.TYPE_NAME_PROPERTY),
				Arguments.of(SingleMemberAnnotation.TYPE_NAME_PROPERTY),
				Arguments.of(NormalAnnotation.TYPE_NAME_PROPERTY));

	}

	@ParameterizedTest
	@MethodSource("arguments_referenceToLocalVariableExcluded")
	void testIsReferenceToLocalVariableExcluded_shouldReturnTrue(
			StructuralPropertyDescriptor propertyDescriptor) {
		boolean referenceExcluded = ExcludeReferenceByProperty
			.isReferenceToLocalVariableExcludedFor(propertyDescriptor);
		assertTrue(referenceExcluded);
	}
	
	@Test
	void testIsReferenceToLocalVariableExcluded_shouldReturnFalse() {
		boolean referenceExcluded = ExcludeReferenceByProperty
			.isReferenceToLocalVariableExcludedFor(MethodInvocation.ARGUMENTS_PROPERTY);
		assertFalse(referenceExcluded);
	}

	private static Stream<Arguments> arguments_referenceToVariableExcludedForSimpleName()
			throws Exception {
		return Stream.of(
				// structural property descriptors where the property type is
				// {@link org.eclipse.jdt.core.dom.SimpleName}.
				Arguments.of(LabeledStatement.LABEL_PROPERTY),
				Arguments.of(ContinueStatement.LABEL_PROPERTY),
				Arguments.of(BreakStatement.LABEL_PROPERTY),
				Arguments.of(MethodInvocation.NAME_PROPERTY),
				Arguments.of(SuperMethodInvocation.NAME_PROPERTY),
				Arguments.of(ExpressionMethodReference.NAME_PROPERTY),
				Arguments.of(SuperMethodReference.NAME_PROPERTY),
				Arguments.of(TypeMethodReference.NAME_PROPERTY),
				Arguments.of(MethodDeclaration.NAME_PROPERTY),
				Arguments.of(VariableDeclarationFragment.NAME_PROPERTY),
				Arguments.of(SingleVariableDeclaration.NAME_PROPERTY),
				Arguments.of(TypeDeclaration.NAME_PROPERTY),
				Arguments.of(EnumDeclaration.NAME_PROPERTY),
				Arguments.of(EnumConstantDeclaration.NAME_PROPERTY),
				Arguments.of(AnnotationTypeDeclaration.NAME_PROPERTY),
				Arguments.of(AnnotationTypeMemberDeclaration.NAME_PROPERTY),
				Arguments.of(RecordDeclaration.NAME_PROPERTY),
				Arguments.of(QualifiedType.NAME_PROPERTY),
				Arguments.of(NameQualifiedType.NAME_PROPERTY),
				Arguments.of(MemberValuePair.NAME_PROPERTY),
				// structural property descriptors where the property type is
				// {@link org.eclipse.jdt.core.dom.Name}.
				Arguments.of(SimpleType.NAME_PROPERTY),
				Arguments.of(NameQualifiedType.QUALIFIER_PROPERTY),
				Arguments.of(ThisExpression.QUALIFIER_PROPERTY),
				Arguments.of(SuperFieldAccess.QUALIFIER_PROPERTY),
				Arguments.of(SuperMethodInvocation.QUALIFIER_PROPERTY),
				Arguments.of(MarkerAnnotation.TYPE_NAME_PROPERTY),
				Arguments.of(SingleMemberAnnotation.TYPE_NAME_PROPERTY),
				Arguments.of(NormalAnnotation.TYPE_NAME_PROPERTY));
	}

	@ParameterizedTest
	@MethodSource("arguments_referenceToVariableExcludedForSimpleName")
	void testIsReferenceToVariableExcludedForSimpleName_shouldReturnTrue(
			StructuralPropertyDescriptor propertyDescriptor) {
		boolean referenceExcluded = ExcludeReferenceByProperty
			.isReferenceToVariableExcluded4SimpleName(propertyDescriptor);
		assertTrue(referenceExcluded);
	}

	@Test
	void testIsReferenceToVariableExcludedForSimpleName_shouldReturnFalse() {
		boolean referenceExcluded = ExcludeReferenceByProperty
			.isReferenceToVariableExcluded4SimpleName(MethodInvocation.ARGUMENTS_PROPERTY);
		assertFalse(referenceExcluded);
	}

	private static Stream<Arguments> arguments_referenceToVariableExcludedForQualifiedName()
			throws Exception {
		return Stream.of(
				// structural property descriptors where the property type is
				// {@link org.eclipse.jdt.core.dom.Name}.
				Arguments.of(SimpleType.NAME_PROPERTY),
				Arguments.of(NameQualifiedType.QUALIFIER_PROPERTY),
				Arguments.of(ThisExpression.QUALIFIER_PROPERTY),
				Arguments.of(SuperFieldAccess.QUALIFIER_PROPERTY),
				Arguments.of(SuperMethodInvocation.QUALIFIER_PROPERTY),
				Arguments.of(MarkerAnnotation.TYPE_NAME_PROPERTY),
				Arguments.of(SingleMemberAnnotation.TYPE_NAME_PROPERTY),
				Arguments.of(NormalAnnotation.TYPE_NAME_PROPERTY));
	}

	@ParameterizedTest
	@MethodSource("arguments_referenceToVariableExcludedForQualifiedName")
	void testIsReferenceToVariableExcludedForQualifiedName_shouldReturnTrue(
			StructuralPropertyDescriptor propertyDescriptor) {
		boolean referenceExcluded = ExcludeReferenceByProperty
			.isReferenceToVariableExcluded4QualifiedName(propertyDescriptor);
		assertTrue(referenceExcluded);
	}

	@Test
	void testIsReferenceToVariableExcludedForQualifiedName_shouldReturnFalse() {
		boolean referenceExcluded = ExcludeReferenceByProperty
			.isReferenceToVariableExcluded4QualifiedName(MethodInvocation.ARGUMENTS_PROPERTY);
		assertFalse(referenceExcluded);
	}
}

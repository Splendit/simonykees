package eu.jsparrow.core.visitor.impl.entryset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
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
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

class ExcludeVariableReferenceTest {

	@Test
	void visit_MethodInvocation_shouldNotHaveVariableBinding() throws Exception {
		MethodInvocation methodInvocation = TestHelper.createExpressionFromString("exampleMethod()",
				MethodInvocation.class);
		boolean variableBindingExcludedFor = NameLocationInParent
			.isVariableBindingExcludedFor(methodInvocation.getName());
		assertTrue(variableBindingExcludedFor);
	}

	@Test
	void visit_SuperMethodInvocation_shouldNotHaveVariableBinding() throws Exception {
		SuperMethodInvocation superMethodInvocation = TestHelper.createExpressionFromString("super.superMethod()",
				SuperMethodInvocation.class);
		boolean variableBindingExcludedFor = NameLocationInParent
			.isVariableBindingExcludedFor(superMethodInvocation.getName());
		assertTrue(variableBindingExcludedFor);
	}

	@Test
	void visit_ExpressionMethodReference_shouldNotHaveVariableBinding() throws Exception {
		ExpressionMethodReference expressionMethodReference = TestHelper.createExpressionFromString("this::xMethod",
				ExpressionMethodReference.class);
		boolean variableBindingExcludedFor = NameLocationInParent
			.isVariableBindingExcludedFor(expressionMethodReference.getName());
		assertTrue(variableBindingExcludedFor);
	}

	@Test
	void visit_SuperMethodReference_shouldNotHaveVariableBinding() throws Exception {
		SuperMethodReference superMethodReference = TestHelper.createExpressionFromString("super::superMethod",
				SuperMethodReference.class);
		boolean variableBindingExcludedFor = NameLocationInParent
			.isVariableBindingExcludedFor(superMethodReference.getName());
		assertTrue(variableBindingExcludedFor);
	}

	@Test
	void visit_TypeMethodReference_shouldNotHaveVariableBinding() throws Exception {
		TypeMethodReference typeMethodReference = TestHelper.createExpressionFromString(
				"InnerClass.@MarkerAnnotation InnerInnerClass::xMethod", TypeMethodReference.class);
		boolean variableBindingExcludedFor = NameLocationInParent
			.isVariableBindingExcludedFor(typeMethodReference.getName());
		assertTrue(variableBindingExcludedFor);
	}

	@Test
	void visit_NamePropertyOfQualifiedName_shouldExcludeVariableBinding() throws Exception {
		SimpleType simpleType = (SimpleType) TestHelper
			.createStatementFromString("a.b.X x;", VariableDeclarationStatement.class)
			.getType();
		QualifiedName qualifiedName = (QualifiedName) simpleType.getName();
		assertTrue(NameLocationInParent.isVariableBindingExcludedFor(qualifiedName.getName()));
	}

	/**
	 * Does not exclude reference to a variable although it would be reasonable
	 * to do so because the name belongs to a Type. to exclude reference to a
	 * variable. After corresponding corrections, this test may fail.
	 */
	@Test
	void test_QualifiedNameAsQualifiedNameQualifier_cannotExcludeVariableBinding() throws Exception {
		SimpleType simpleType = (SimpleType) TestHelper
			.createStatementFromString("a.b.X x;", VariableDeclarationStatement.class)
			.getType();
		QualifiedName qualifiedName = (QualifiedName) simpleType.getName();
		QualifiedName qualifiedNameAsQualifier = (QualifiedName) qualifiedName.getQualifier();
		boolean variableBindingExcludedFor = NameLocationInParent
			.isVariableBindingExcludedFor(qualifiedNameAsQualifier);
		assertFalse(variableBindingExcludedFor);
	}

	/**
	 * Does not exclude reference to a variable although it would be reasonable
	 * to do so because the name belongs to a Type. to exclude reference to a
	 * variable. After corresponding corrections, this test may fail.
	 */
	@Test
	void test_SimpleNameAsQualifiedNameQualifier_cannotExcludeVariableBinding() throws Exception {
		SimpleType simpleType = (SimpleType) TestHelper
			.createStatementFromString("a.b.X x;", VariableDeclarationStatement.class)
			.getType();
		QualifiedName qualifiedName = (QualifiedName) simpleType.getName();
		QualifiedName qualifiedNameAsQualifier = (QualifiedName) qualifiedName.getQualifier();
		SimpleName simpleNameAsQualifier = (SimpleName) qualifiedNameAsQualifier.getQualifier();
		boolean variableBindingExcludedFor = NameLocationInParent.isVariableBindingExcludedFor(simpleNameAsQualifier);
		assertFalse(variableBindingExcludedFor);
	}

	private static Stream<Arguments> bodyDeclarations_excludingVariableBinding() throws Exception {
		return Stream.of(
				Arguments.of(
						"" +
								"	void exampleMethod() {\n" +
								"	}",
						MethodDeclaration.NAME_PROPERTY),
				Arguments.of(
						"" +
								"	class ExampleClass {\n" +
								"	}",
						TypeDeclaration.NAME_PROPERTY),
				Arguments.of(
						"" +
								"	interface ExampleInterface {\n" +
								"	}",
						TypeDeclaration.NAME_PROPERTY),
				Arguments.of(
						"" +
								"	@interface ExampleAnnotation {\n" +
								"	}",
						AnnotationTypeDeclaration.NAME_PROPERTY),
				Arguments.of(
						"" +
								"	enum ExampleEnum {\n" +
								"		X;\n" +
								"	}",
						EnumDeclaration.NAME_PROPERTY),
				Arguments.of(
						"" +
								"	record ExampleRecord(int x) {\n" +
								"	}",
						RecordDeclaration.NAME_PROPERTY));
	}

	@ParameterizedTest
	@MethodSource("bodyDeclarations_excludingVariableBinding")
	void test_BodyDeclaration_shouldExcludeVariableBinding(String code, ChildPropertyDescriptor childPropertyDescriptor)
			throws Exception {

		TypeDeclaration typeDeclaration = ASTNodeBuilder.createTypeDeclarationFromString("TestClass", code);
		BodyDeclaration bodyDeclaration = (BodyDeclaration) typeDeclaration.bodyDeclarations()
			.get(0);
		SimpleName simpleName = (SimpleName) bodyDeclaration.getStructuralProperty(childPropertyDescriptor);

		boolean variableBindingExcludedFor = NameLocationInParent.isVariableBindingExcludedFor(simpleName);

		assertTrue(variableBindingExcludedFor);
	}

	@Test
	void test_AnnotationTypeMemberDeclaration_shouldExcludeVariableBinding() throws Exception {
		String code = "" +
				"	@interface ExampleAnnotation {\n" +
				"			int x();\n" +
				"	}";
		TypeDeclaration typeDeclaration = ASTNodeBuilder.createTypeDeclarationFromString("TestClass", code);
		AnnotationTypeDeclaration annotationTypeDeclaration = (AnnotationTypeDeclaration) typeDeclaration
			.bodyDeclarations()
			.get(0);

		AnnotationTypeMemberDeclaration annotationMemberDeclaration = (AnnotationTypeMemberDeclaration) annotationTypeDeclaration
			.bodyDeclarations()
			.get(0);

		boolean variableBindingExcludedFor = NameLocationInParent
			.isVariableBindingExcludedFor(annotationMemberDeclaration.getName());
		assertTrue(variableBindingExcludedFor);
	}

	private static Stream<Arguments> arguments_SimpleNameAsTypeProperty_excludingVariableBinding() throws Exception {
		return Stream.of(
				Arguments.of(
						"ExampleType",
						SimpleType.NAME_PROPERTY),
				Arguments.of(
						"InnerClass.@MarkerAnnotation InnerInnerClass",
						NameQualifiedType.NAME_PROPERTY),
				Arguments.of(
						"InnerClass.@MarkerAnnotation InnerInnerClass",
						NameQualifiedType.QUALIFIER_PROPERTY),
				Arguments.of(
						"InnerClass.@MarkerAnnotation InnerInnerClass.InnermostClass",
						QualifiedType.NAME_PROPERTY));
	}

	@ParameterizedTest
	@MethodSource("arguments_SimpleNameAsTypeProperty_excludingVariableBinding")
	void test_SimpleNameAsTypeProperty_shouldExcludeVariableBinding(String stringCodingVariableType,
			ChildPropertyDescriptor childPropertyDescriptor)
			throws Exception {
		String code = stringCodingVariableType + " x;";
		Type type = TestHelper.createStatementFromString(code, VariableDeclarationStatement.class)
			.getType();
		SimpleName simpleName = (SimpleName) type.getStructuralProperty(childPropertyDescriptor);
		boolean variableBindingExcludedFor = NameLocationInParent.isVariableBindingExcludedFor(simpleName);
		assertTrue(variableBindingExcludedFor);
	}

	private static Stream<Arguments> arguments_QualifiedNameAsTypeProperty_excludingVariableBinding() throws Exception {
		return Stream.of(
				Arguments.of(
						"org.example.ExampleType",
						SimpleType.NAME_PROPERTY),
				Arguments.of(
						"InnerClass.InnerInnerClass.@MarkerAnnotation InnermostClass",
						NameQualifiedType.QUALIFIER_PROPERTY));
	}

	@ParameterizedTest
	@MethodSource("arguments_QualifiedNameAsTypeProperty_excludingVariableBinding")
	void test_QualifiedNameAsTypeProperty_shouldExcludeVariableBinding(String stringCodingVariableType,
			ChildPropertyDescriptor childPropertyDescriptor)
			throws Exception {
		String code = stringCodingVariableType + " x;";
		Type type = TestHelper.createStatementFromString(code, VariableDeclarationStatement.class)
			.getType();
		QualifiedName qualifiedName = (QualifiedName) type.getStructuralProperty(childPropertyDescriptor);
		boolean variableBindingExcludedFor = NameLocationInParent.isVariableBindingExcludedFor(qualifiedName);
		assertTrue(variableBindingExcludedFor);
	}

	@Test
	void test_NamePropertyOfMemberValuePair_shouldNotHaveVariableBinding() throws Exception {
		NormalAnnotation normalAnnotation = (NormalAnnotation) TestHelper
			.createStatementFromString("@XAnnotation(x=1) int x;", VariableDeclarationStatement.class)
			.modifiers()
			.get(0);

		MemberValuePair memberValuePair = (MemberValuePair) normalAnnotation.values()
			.get(0);
		boolean variableBindingExcludedFor = NameLocationInParent
			.isVariableBindingExcludedFor(memberValuePair.getName());
		assertTrue(variableBindingExcludedFor);
	}

	private static Stream<Arguments> arguments_SimpleNameAsAnnotationTypeNameProperty_excludingVariableBinding()
			throws Exception {
		return Stream.of(
				Arguments.of(
						"@MarkerAnnotation",
						MarkerAnnotation.TYPE_NAME_PROPERTY),
				Arguments.of(
						"@SingleMemberAnnotation(1)",
						SingleMemberAnnotation.TYPE_NAME_PROPERTY),
				Arguments.of(
						"@NormalAnnotation(x = 1)",
						NormalAnnotation.TYPE_NAME_PROPERTY));
	}

	@ParameterizedTest
	@MethodSource("arguments_SimpleNameAsAnnotationTypeNameProperty_excludingVariableBinding")
	void test_SimpleNameAsAnnotationTypeNameProperty_shouldExcludeVariableBinding(String stringCodingAnnotation,
			ChildPropertyDescriptor childPropertyDescriptor) throws Exception {

		Annotation annotation = (Annotation) TestHelper
			.createStatementFromString(stringCodingAnnotation + " int x;",
					VariableDeclarationStatement.class)
			.modifiers()
			.get(0);
		SimpleName simpleName = (SimpleName) annotation.getStructuralProperty(childPropertyDescriptor);
		boolean variableBindingExcludedFor = NameLocationInParent.isVariableBindingExcludedFor(simpleName);
		assertTrue(variableBindingExcludedFor);
	}

	private static Stream<Arguments> arguments_QualifiedNameAsAnnotationTypeNameProperty_excludingVariableBinding()
			throws Exception {
		return Stream.of(
				Arguments.of(
						"@examples.annotations.MarkerAnnotation",
						MarkerAnnotation.TYPE_NAME_PROPERTY),
				Arguments.of(
						"@examples.annotations.SingleMemberAnnotation(1)",
						SingleMemberAnnotation.TYPE_NAME_PROPERTY),
				Arguments.of(
						"@examples.annotations.NormalAnnotation(x = 1)",
						NormalAnnotation.TYPE_NAME_PROPERTY));
	}

	@ParameterizedTest
	@MethodSource("arguments_QualifiedNameAsAnnotationTypeNameProperty_excludingVariableBinding")
	void test_QualifiedNameAsAnnotationTypeNameProperty_shouldExcludeVariableBinding(String stringCodingAnnotation,
			ChildPropertyDescriptor childPropertyDescriptor) throws Exception {

		Annotation annotation = (Annotation) TestHelper
			.createStatementFromString(stringCodingAnnotation + " int x;",
					VariableDeclarationStatement.class)
			.modifiers()
			.get(0);
		QualifiedName qualifiedName = (QualifiedName) annotation.getStructuralProperty(childPropertyDescriptor);
		boolean variableBindingExcludedFor = NameLocationInParent.isVariableBindingExcludedFor(qualifiedName);
		assertTrue(variableBindingExcludedFor);
	}

	public static Stream<Arguments> arguments_Label_ExcludingVariableBinding() throws Exception {

		return Stream.of(
				Arguments.of(
						"" +
								"		x: while (true) {\n" +
								"			break x;\n" +
								"		}",
						LabeledStatement.LABEL_PROPERTY),
				Arguments.of(
						"break x;",
						BreakStatement.LABEL_PROPERTY),
				Arguments.of(
						"continue x;",
						ContinueStatement.LABEL_PROPERTY));
	}

	@ParameterizedTest
	@MethodSource("arguments_Label_ExcludingVariableBinding")
	void test_Label_shouldExcludeVariableBinding(String code, ChildPropertyDescriptor childPropertyDescriptor)
			throws Exception {

		Statement statement = (Statement) ASTNodeBuilder.createBlockFromString(code)
			.statements()
			.get(0);
		SimpleName simpleName = (SimpleName) statement.getStructuralProperty(childPropertyDescriptor);
		boolean variableBindingExcludedFor = NameLocationInParent.isVariableBindingExcludedFor(simpleName);
		assertTrue(variableBindingExcludedFor);
	}

	@Test
	void visit_SimpleName_shouldNotExcludeVariableBinding() throws Exception {
		SimpleName simpleName = TestHelper.createExpressionFromString("x", SimpleName.class);
		boolean variableBindingExcludedFor = NameLocationInParent.isVariableBindingExcludedFor(simpleName);
		assertFalse(variableBindingExcludedFor);
	}

	@Test
	void visit_QualifiedName_shouldNotExcludeVariableBinding() throws Exception {
		QualifiedName qualifiedName = TestHelper.createExpressionFromString("a.b.X", QualifiedName.class);
		boolean variableBindingExcludedFor = NameLocationInParent.isVariableBindingExcludedFor(qualifiedName);
		assertFalse(variableBindingExcludedFor);
	}

	@Test
	void test_QualifiedNameAsQualifiedNameQualifier_shouldNotExcludeVariableBinding() throws Exception {
		QualifiedName qualifiedName = TestHelper.createExpressionFromString("a.b.X", QualifiedName.class);
		QualifiedName qualifiedNameAsQualifier = (QualifiedName) qualifiedName.getQualifier();
		boolean variableBindingExcludedFor = NameLocationInParent
			.isVariableBindingExcludedFor(qualifiedNameAsQualifier);
		assertFalse(variableBindingExcludedFor);
	}

	@Test
	void test_SimpleNameAsQualifiedNameQualifier_shouldNotExcludeVariableBinding() throws Exception {
		QualifiedName qualifiedName = TestHelper.createExpressionFromString("a.b.X", QualifiedName.class);
		QualifiedName qualifiedNameAsQualifier = (QualifiedName) qualifiedName.getQualifier();
		SimpleName simpleNameAsQualifier = (SimpleName) qualifiedNameAsQualifier.getQualifier();
		boolean variableBindingExcludedFor = NameLocationInParent.isVariableBindingExcludedFor(simpleNameAsQualifier);
		assertFalse(variableBindingExcludedFor);
	}

	@Test
	void test_VariableDeclarationFragmentName_shouldExcludeLocalVariableReference() throws Exception {
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) TestHelper
			.createStatementFromString("a.b.X x;", VariableDeclarationStatement.class)
			.fragments()
			.get(0);
		assertTrue(NameLocationInParent.isReferenceToLocalVariableExcludedFor(variableDeclarationFragment.getName()));
	}

	@Test
	void test_SingleVariableDeclarationName_shouldExcludeLocalVariableReference() throws Exception {
		String code = ""
				+ "	void useObject(Object o) {\n"
				+ "	}";

		MethodDeclaration methodDeclaration = (MethodDeclaration) ASTNodeBuilder
			.createTypeDeclarationFromString("DummyClass", code)
			.bodyDeclarations()
			.get(0);

		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) methodDeclaration
			.parameters()
			.get(0);

		boolean variableBindingExcludedFor = NameLocationInParent
			.isReferenceToLocalVariableExcludedFor(singleVariableDeclaration.getName());
		assertTrue(variableBindingExcludedFor);
	}

	@Test
	void test_EnumConstantDeclarationName_shouldExcludeLocalVariableReference() throws Exception {
		String code = "" +
				"	enum ExampleEnum {\n" +
				"		X;\n" +
				"	}";
		TypeDeclaration typeDeclaration = ASTNodeBuilder.createTypeDeclarationFromString("TestClass", code);
		EnumDeclaration enumDeclaration = (EnumDeclaration) typeDeclaration
			.bodyDeclarations()
			.get(0);

		EnumConstantDeclaration enumConstantDeclaration = (EnumConstantDeclaration) enumDeclaration.enumConstants()
			.get(0);

		boolean variableBindingExcludedFor = NameLocationInParent
			.isReferenceToLocalVariableExcludedFor(enumConstantDeclaration.getName());
		assertTrue(variableBindingExcludedFor);
	}

	@Test
	void test_FieldAccessName_shouldExcludeLocalVariableBinding()
			throws Exception {
		FieldAccess fieldAccess = (FieldAccess) TestHelper.createExpressionFromString("this.x",
				FieldAccess.class);
		assertTrue(NameLocationInParent.isReferenceToLocalVariableExcludedFor(fieldAccess.getName()));
	}

	@Test
	void test_SuperFieldAccess_shouldExcludeLocalVariableBinding()
			throws Exception {
		SuperFieldAccess superFieldAccess = (SuperFieldAccess) TestHelper.createExpressionFromString("super.x",
				SuperFieldAccess.class);
		assertTrue(NameLocationInParent.isReferenceToLocalVariableExcludedFor(superFieldAccess.getName()));
	}

	@Test
	void test_QualifiedName_shouldExcludeLocalVariableBinding()
			throws Exception {
		QualifiedName qualifiedName = (QualifiedName) TestHelper.createExpressionFromString("a.b.X",
				QualifiedName.class);
		assertTrue(NameLocationInParent.isReferenceToLocalVariableExcludedFor(qualifiedName.getName()));
	}

	@Test
	void test_MethodInvocation_shouldExcludeLocalVariableReference() throws Exception {
		MethodInvocation methodInvocation = TestHelper.createExpressionFromString("exampleMethod()",
				MethodInvocation.class);
		boolean variableBindingExcludedFor = NameLocationInParent
			.isReferenceToLocalVariableExcludedFor(methodInvocation.getName());
		assertTrue(variableBindingExcludedFor);
	}

	@Test
	void test_SimpleName_shouldNotExcludeLocalVariableBinding() throws Exception {
		SimpleName simpleName = TestHelper.createExpressionFromString("x", SimpleName.class);
		assertFalse(NameLocationInParent.isReferenceToLocalVariableExcludedFor(simpleName));
	}

	@Test
	void testCanBeReferenceToLocalVariable_shouldReturnTrue() throws Exception {
		SimpleName simpleName = TestHelper.createExpressionFromString("x", SimpleName.class);
		assertTrue(NameLocationInParent.canBeReferenceToLocalVariable(simpleName));

	}

	@Test
	void testCanBeReferenceToLocalVariable_shouldReturnFalse() throws Exception {
		MethodInvocation methodInvocation = TestHelper.createExpressionFromString("exampleMethod()",
				MethodInvocation.class);
		boolean canBeReferenceToLocalVariable = NameLocationInParent
			.canBeReferenceToLocalVariable(methodInvocation.getName());
		assertFalse(canBeReferenceToLocalVariable);
	}

	@Test
	void testCanHaveVariableBinding_shouldReturnTrue() throws Exception {
		SimpleName simpleName = TestHelper.createExpressionFromString("x", SimpleName.class);
		assertTrue(NameLocationInParent.canHaveVariableBinding(simpleName));
	}

	@Test
	void testCanHaveVariableBinding_shouldReturnFalse() throws Exception {
		MethodInvocation methodInvocation = TestHelper.createExpressionFromString("exampleMethod()",
				MethodInvocation.class);
		boolean canHaveVariableBinding = NameLocationInParent
			.canHaveVariableBinding(methodInvocation.getName());
		assertFalse(canHaveVariableBinding);
	}

	@Test
	void test_QualifierOfThisExpression_shouldExcludeLocalVariableBinding()
			throws Exception {
		ThisExpression thisExpression = (ThisExpression) TestHelper.createExpressionFromString("X.this",
				ThisExpression.class);
		assertTrue(
				NameLocationInParent.isReferenceToLocalVariableExcludedFor((SimpleName) thisExpression.getQualifier()));
	}

	@Test
	void test_QualifierOfSuperFieldAccess_shouldExcludeLocalVariableBinding()
			throws Exception {
		SuperFieldAccess superFieldAccess = (SuperFieldAccess) TestHelper.createExpressionFromString(
				"X.super.superField",
				SuperFieldAccess.class);
		assertTrue(NameLocationInParent
			.isReferenceToLocalVariableExcludedFor((SimpleName) superFieldAccess.getQualifier()));
	}

	@Test
	void test_QualifierOfSuperMethodInvocation_shouldExcludeLocalVariableBinding()
			throws Exception {
		SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) TestHelper.createExpressionFromString(
				"X.super.superMethod()",
				SuperMethodInvocation.class);
		assertTrue(NameLocationInParent
			.isReferenceToLocalVariableExcludedFor((SimpleName) superMethodInvocation.getQualifier()));
	}
}

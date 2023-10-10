package eu.jsparrow.core.visitor.impl.entryset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
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
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.BadLocationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.JdtUnitException;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.ExcludeVariableBinding;
import eu.jsparrow.rules.java16.javarecords.UseJavaRecordsASTVisitor;

class ExcludeVariableBindingTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new UseJavaRecordsASTVisitor());
		fixtureProject.setJavaVersion(JavaCore.VERSION_16);
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	private SimpleName findUniqueSimpleName(String code, String identifier, ChildPropertyDescriptor propertyDescriptor)
			throws JdtUnitException, JavaModelException, BadLocationException {

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> simpleNames = new ArrayList<>();
		ASTVisitor simpleNamesCollectorVisitor = new ASTVisitor() {

			@Override
			public boolean visit(SimpleName node) {
				if (node.getIdentifier()
					.equals(identifier) && node.getLocationInParent() == propertyDescriptor) {
					simpleNames.add(node);
				}
				return false;
			}

		};

		getCompilationUnit()
			.accept(simpleNamesCollectorVisitor);
		assertEquals(1, simpleNames.size());
		return simpleNames.get(0);
	}

	private QualifiedName findUniqueQualifiedName(String code, ChildPropertyDescriptor propertyDescriptor)
			throws JdtUnitException, JavaModelException, BadLocationException {

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<QualifiedName> qualifiedNames = new ArrayList<>();
		ASTVisitor simpleNamesCollectorVisitor = new ASTVisitor() {

			@Override
			public boolean visit(QualifiedName node) {
				if (node.getLocationInParent() == propertyDescriptor) {
					qualifiedNames.add(node);
				}
				return false;
			}
		};

		getCompilationUnit()
			.accept(simpleNamesCollectorVisitor);

		assertEquals(1, qualifiedNames.size());
		return qualifiedNames.get(0);
	}

	private CompilationUnit getCompilationUnit() {
		final TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		return ASTNodeUtil.getSpecificAncestor(typeDeclaration, CompilationUnit.class);
	}

	@Test
	void visit_MethodInvocation_shouldNotHaveVariableBinding()
			throws Exception {

		String code = "" +
				"		void callXMethod() {\n" +
				"			xMethod();\n" +
				"		}\n" +
				"\n" +
				"		void xMethod() {\n" +
				"		}";

		String identifier = "xMethod";
		SimpleName xMethodName = findUniqueSimpleName(code, identifier, MethodInvocation.NAME_PROPERTY);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(xMethodName));
		final IBinding binding = xMethodName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_SuperMethodInvocation_shouldNotHaveVariableBinding()
			throws Exception {

		String code = "" +
				"	static class SuperClass {\n" +
				"		void superMethod() {\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	static class SubClass extends SuperClass {\n" +
				"		void callSuperMethod() {\n" +
				"			super.superMethod();\n" +
				"		}\n" +
				"	}";

		String identifier = "superMethod";
		SimpleName xMethodName = findUniqueSimpleName(code, identifier, SuperMethodInvocation.NAME_PROPERTY);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(xMethodName));
		final IBinding binding = xMethodName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_ExpressionMethodReference_shouldNotHaveVariableBinding()
			throws Exception {

		String code = "" +
				"		Runnable r = this::xMethod;\n" +
				"\n" +
				"		void xMethod() {\n" +
				"		}";

		String identifier = "xMethod";
		SimpleName xMethodName = findUniqueSimpleName(code, identifier, ExpressionMethodReference.NAME_PROPERTY);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(xMethodName));
		final IBinding binding = xMethodName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);

	}

	@Test
	void visit_SuperMethodReference_shouldNotHaveVariableBinding()
			throws Exception {

		String code = "" +
				"		static class SuperClass {\n" +
				"			void superMethod() {\n" +
				"			}\n" +
				"		}\n" +
				"\n" +
				"		static class SubClass extends SuperClass {\n" +
				"			Runnable r = super::superMethod;\n" +
				"		}";

		String identifier = "superMethod";
		SimpleName xMethodName = findUniqueSimpleName(code, identifier, SuperMethodReference.NAME_PROPERTY);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(xMethodName));
		final IBinding binding = xMethodName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_TypeMethodReference_shouldNotHaveVariableBinding() throws Exception {

		String code = "" +
				"	Runnable r = InnerClass.@MarkerAnnotation InnerInnerClass::xMethod;\n" +
				"\n" +
				"	@interface MarkerAnnotation {\n" +
				"	}\n" +
				"\n" +
				"	static class InnerClass {\n" +
				"		static class InnerInnerClass {\n" +
				"			static void xMethod() {\n" +
				"			}\n" +
				"		}\n" +
				"	}";

		String identifier = "xMethod";
		SimpleName xMethodName = findUniqueSimpleName(code, identifier, TypeMethodReference.NAME_PROPERTY);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(xMethodName));
		final IBinding binding = xMethodName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	public static Stream<Arguments> arguments_NamePropertyOfDeclaration_notVariable() throws Exception {
		return Stream.of(
				Arguments.of(
						"" +
								"	void xMethod() {\n" +
								"	}",
						"xMethod",
						MethodDeclaration.NAME_PROPERTY),
				Arguments.of(
						"" +
								"	class XClass {\n" +
								"	}",
						"XClass",
						TypeDeclaration.NAME_PROPERTY),
				Arguments.of(
						"" +
								"	interface XInterface {\n" +
								"	}",
						"XInterface",
						TypeDeclaration.NAME_PROPERTY),
				Arguments.of(
						"" +
								"	@interface XAnnotation {\n" +
								"	}",
						"XAnnotation",
						AnnotationTypeDeclaration.NAME_PROPERTY),
				Arguments.of(
						"" +
								"	@interface XAnnotation {\n" +
								"			int x();\n" +
								"	}",
						"x",
						AnnotationTypeMemberDeclaration.NAME_PROPERTY),
				Arguments.of(
						"" +
								"	enum XEnum {\n" +
								"		X;\n" +
								"	}",
						"XEnum",
						EnumDeclaration.NAME_PROPERTY),
				Arguments.of(
						"" +
								"	record XRecord(int x) {\n" +
								"	}",
						"XRecord",
						RecordDeclaration.NAME_PROPERTY));
	}

	@ParameterizedTest
	@MethodSource("arguments_NamePropertyOfDeclaration_notVariable")
	void visit_NamePropertyOfDeclaration_shouldNotHaveVariableBinding(String code, String identifier,
			ChildPropertyDescriptor childPropertyDescriptor) throws Exception {

		SimpleName declarationNameProperty = findUniqueSimpleName(code, identifier, childPropertyDescriptor);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(declarationNameProperty));
		final IBinding binding = declarationNameProperty.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_SimpleNamePropertyOfSimpleType_shouldNotHaveVariableBinding() throws Exception {

		String code = "" +
				"	InnerClass x;\n" +
				"\n" +
				"	static class InnerClass {\n" +
				"	}";

		String identifier = "InnerClass";
		SimpleName innerClassName = findUniqueSimpleName(code, identifier, SimpleType.NAME_PROPERTY);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(innerClassName));
		final IBinding binding = innerClassName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_QualifiedNamePropertyOfSimpleType_shouldNotHaveVariableBinding() throws Exception {

		String code = "" +
				"	InnerClass.InnerInnerClass x;\n" +
				"\n" +
				"	static class InnerClass {\n" +
				"		static class InnerInnerClass {\n" +
				"		}\n" +
				"	}";

		QualifiedName innerInnerClassName = findUniqueQualifiedName(code, SimpleType.NAME_PROPERTY);
		assertEquals("InnerClass.InnerInnerClass", innerInnerClassName.getFullyQualifiedName());
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(innerInnerClassName));
		final IBinding binding = innerInnerClassName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_NamePropertyOfNameQualifiedType_shouldNotHaveVariableBinding() throws Exception {

		String code = "" +
				"	InnerClass.@MarkerAnnotation InnerInnerClass x;\n" +
				"\n" +
				"	@interface MarkerAnnotation {\n" +
				"	}\n" +
				"\n" +
				"	static class InnerClass {\n" +
				"		static class InnerInnerClass {\n" +
				"		}\n" +
				"	}";

		String identifier = "InnerInnerClass";
		SimpleName innerClassName = findUniqueSimpleName(code, identifier, NameQualifiedType.NAME_PROPERTY);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(innerClassName));
		final IBinding binding = innerClassName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_NamePropertyOfQualifiedType_shouldNotHaveVariableBinding() throws Exception {

		String code = "" +
				"	InnerClass.@MarkerAnnotation InnerInnerClass.InnermostClass x;\n" +
				"\n" +
				"	@interface MarkerAnnotation {\n" +
				"	}\n" +
				"\n" +
				"	static class InnerClass {\n" +
				"		static class InnerInnerClass {\n" +
				"			class InnermostClass {\n" +
				"			}\n" +
				"		}\n" +
				"	}";

		String identifier = "InnermostClass";
		SimpleName innerClassName = findUniqueSimpleName(code, identifier, QualifiedType.NAME_PROPERTY);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(innerClassName));
		final IBinding binding = innerClassName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_SimpleQualifierOfNameQualifiedType_shouldNotHaveVariableBinding() throws Exception {

		String code = "" +
				"	InnerClass.@MarkerAnnotation InnerInnerClass x;\n" +
				"\n" +
				"	@interface MarkerAnnotation {\n" +
				"	}\n" +
				"\n" +
				"	static class InnerClass {\n" +
				"		static class InnerInnerClass {\n" +
				"		}\n" +
				"	}";

		String identifier = "InnerClass";
		SimpleName innerClassName = findUniqueSimpleName(code, identifier, NameQualifiedType.QUALIFIER_PROPERTY);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(innerClassName));
		final IBinding binding = innerClassName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_QualifiedQualifierOfNameQualifiedType_shouldNotHaveVariableBinding() throws Exception {

		String code = "" +
				"	InnerClass.InnerInnerClass.@MarkerAnnotation InnermostClass x;\n" +
				"\n" +
				"	@interface MarkerAnnotation {\n" +
				"	}\n" +
				"\n" +
				"	static class InnerClass {\n" +
				"		static class InnerInnerClass {\n" +
				"			class InnermostClass {\n" +
				"			}\n" +
				"		}\n" +
				"	}";

		QualifiedName innerInnerClassName = findUniqueQualifiedName(code, NameQualifiedType.QUALIFIER_PROPERTY);
		assertEquals("InnerClass.InnerInnerClass", innerInnerClassName.getFullyQualifiedName());
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(innerInnerClassName));
		final IBinding binding = innerInnerClassName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_NamePropertyOfMemberValuePair_shouldNotHaveVariableBinding() throws Exception {

		String code = "" +
				"	@XAnnotation(x=1) int x;\n" +
				"	\n" +
				"	@interface XAnnotation {\n" +
				"		int x();\n" +
				"	}";

		String identifier = "x";
		SimpleName innerClassName = findUniqueSimpleName(code, identifier, MemberValuePair.NAME_PROPERTY);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(innerClassName));
		final IBinding binding = innerClassName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_TypeNamePropertyOfMarkerAnnotation_shouldNotHaveVariableBinding() throws Exception {

		String code = "" +
				"	@MarkerAnnotation int x;\n" +
				"	\n" +
				"	@interface MarkerAnnotation {\n" +
				"	}";

		String identifier = "MarkerAnnotation";
		SimpleName innerClassName = findUniqueSimpleName(code, identifier, MarkerAnnotation.TYPE_NAME_PROPERTY);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(innerClassName));
		final IBinding binding = innerClassName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_TypeNamePropertyOfSingleMemberAnnotation_shouldNotHaveVariableBinding() throws Exception {

		String code = "" +
				"	@SingleMemberAnnotation(1) int x;\n" +
				"	\n" +
				"	@interface SingleMemberAnnotation {\n" +
				"		int value();\n" +
				"	}";

		String identifier = "SingleMemberAnnotation";
		SimpleName innerClassName = findUniqueSimpleName(code, identifier, SingleMemberAnnotation.TYPE_NAME_PROPERTY);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(innerClassName));
		final IBinding binding = innerClassName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_TypeNamePropertyOfNormalAnnotation_shouldNotHaveVariableBinding() throws Exception {

		String code = "" +
				"	@NormalAnnotation(x = 1)\n" +
				"	int x;\n" +
				"\n" +
				"	@interface NormalAnnotation {\n" +
				"		int x();\n" +
				"	}";

		String identifier = "NormalAnnotation";
		SimpleName innerClassName = findUniqueSimpleName(code, identifier, NormalAnnotation.TYPE_NAME_PROPERTY);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(innerClassName));
		final IBinding binding = innerClassName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_AnnotationWithQualifiedTypeName_shouldNotHaveVariableBinding() throws Exception {

		String code = "" +
				"	@java.lang.Deprecated\n" +
				"	void deprecatedMethod() {\n" +
				"		\n" +
				"	}";

		QualifiedName qualifiedAnnotationTypeName = findUniqueQualifiedName(code, MarkerAnnotation.TYPE_NAME_PROPERTY);
		assertEquals("java.lang.Deprecated", qualifiedAnnotationTypeName.getFullyQualifiedName());
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(qualifiedAnnotationTypeName));
		final IBinding binding = qualifiedAnnotationTypeName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_fieldDeclaration_shouldHaveVariableBinding() throws Exception {

		String code = "int x;";
		String identifier = "x";
		SimpleName variableName = findUniqueSimpleName(code, identifier, VariableDeclarationFragment.NAME_PROPERTY);
		assertFalse(ExcludeVariableBinding.isVariableBindingExcludedFor(variableName));
		final IBinding binding = variableName.resolveBinding();
		assertNotNull(binding);
		assertTrue(binding instanceof IVariableBinding);

	}

	@Test
	void visit_QualifiedFieldDeclarationInitializer_shouldHaveVariableBinding() throws Exception {
		String code = "int max = Integer.MAX_VALUE;";

		QualifiedName qualifiedNameAsInitializer = findUniqueQualifiedName(code,
				VariableDeclarationFragment.INITIALIZER_PROPERTY);
		assertEquals("Integer.MAX_VALUE", qualifiedNameAsInitializer.getFullyQualifiedName());
		assertFalse(ExcludeVariableBinding.isVariableBindingExcludedFor(qualifiedNameAsInitializer));
		final IBinding binding = qualifiedNameAsInitializer.resolveBinding();
		assertNotNull(binding);
		assertTrue(binding instanceof IVariableBinding);
	}

	@Test
	void visit_NamePropertyOfQualifiedName_shouldHaveVariableBinding() throws Exception {
		String code = "int max = Integer.MAX_VALUE;";

		String identifier = "MAX_VALUE";
		SimpleName namePropertyOfQualifiedName = findUniqueSimpleName(code, identifier,
				QualifiedName.NAME_PROPERTY);
		assertFalse(ExcludeVariableBinding.isVariableBindingExcludedFor(namePropertyOfQualifiedName));
		final IBinding binding = namePropertyOfQualifiedName.resolveBinding();
		assertNotNull(binding);
		assertTrue(binding instanceof IVariableBinding);
	}

	@Test
	void visit_NamePropertyOfQualifiedName_shouldNotHaveVariableBinding() throws Exception {

		String code = "" +
				"	@java.lang.Deprecated\n" +
				"	void deprecatedMethod() {\n" +
				"		\n" +
				"	}";

		String identifier = "Deprecated";
		SimpleName namePropertyOfQualifiedName = findUniqueSimpleName(code, identifier,
				QualifiedName.NAME_PROPERTY);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(namePropertyOfQualifiedName));
		final IBinding binding = namePropertyOfQualifiedName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	public static Stream<Arguments> arguments_LabelX_notVariable() throws Exception {
		return Stream.of(
				Arguments.of(
						"" +
								"		x: while (true)\n" +
								"			break;",
						LabeledStatement.LABEL_PROPERTY),
				Arguments.of(
						"" +
								"		x: while (true)\n" +
								"			break x;",
						BreakStatement.LABEL_PROPERTY),
				Arguments.of(
						"" +
								"		while (true) {\n" +
								"			x: while (true)\n" +
								"				continue x;\n" +
								"		}",
						ContinueStatement.LABEL_PROPERTY));
	}

	@ParameterizedTest
	@MethodSource("arguments_LabelX_notVariable")
	void visit_LabelX_shouldNotHaveVariableBinding(String code, ChildPropertyDescriptor childPropertyDescriptor)
			throws Exception {

		String identifier = "x";
		String method = "" +
				"	void simpleNameAsLabelProperty() {\n" +
				code + "\n" +
				"	}";

		SimpleName innerClassName = findUniqueSimpleName(method, identifier, childPropertyDescriptor);
		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(innerClassName));
		final IBinding binding = innerClassName.resolveBinding();
		assertNull(binding);
	}

	@Test
	void visit_importStaticField_shouldNotExcludeVariableBinding()
			throws Exception {

		defaultFixture.addImport("java.lang.Integer.MAX_VALUE", true, false);

		String code = "int max = MAX_VALUE;";
		QualifiedName staticImportName = findUniqueQualifiedName(code, ImportDeclaration.NAME_PROPERTY);
		assertFalse(ExcludeVariableBinding.isVariableBindingExcludedFor(staticImportName));
		final IBinding binding = staticImportName.resolveBinding();
		assertNotNull(binding);
		assertTrue(binding instanceof IVariableBinding);
	}

	@Test
	void visit_importStaticMethod_shouldNotExcludeVariableBinding()
			throws Exception {

		defaultFixture.addImport("java.lang.Integer.valueOf", true, false);

		String code = "int x = valueOf(1);";
		QualifiedName staticImportName = findUniqueQualifiedName(code, ImportDeclaration.NAME_PROPERTY);
		assertFalse(ExcludeVariableBinding.isVariableBindingExcludedFor(staticImportName));
		final IBinding binding = staticImportName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}

	@Test
	void visit_invalidStaticImport_shouldNotExcludeVariableBinding()
			throws Exception {

		defaultFixture.addImport("java.lang.Integer.INVALID", true, false);

		String code = "int x = INVALID;";
		QualifiedName staticImportName = findUniqueQualifiedName(code, ImportDeclaration.NAME_PROPERTY);
		assertFalse(ExcludeVariableBinding.isVariableBindingExcludedFor(staticImportName));
		final IBinding binding = staticImportName.resolveBinding();
		assertNull(binding);
	}
}

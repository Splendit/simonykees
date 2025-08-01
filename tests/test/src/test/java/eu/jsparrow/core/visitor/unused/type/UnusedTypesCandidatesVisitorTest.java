package eu.jsparrow.core.visitor.unused.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

class UnusedTypesCandidatesVisitorTest extends UsesJDTUnitFixture {

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	private static Stream<Arguments> emptyNestedTypeDeclarations() throws Exception {
		return Stream.of(
				Arguments.of(
						"" +
								"	private class UnusedClass {\n" +
								"		\n" +
								"	}",
						"UnusedClass"),
				Arguments.of(
						"" +
								"	private interface UnusedInterface {\n" +
								"		\n" +
								"	}",

						"UnusedInterface"));
	}

	@ParameterizedTest
	@MethodSource(value = "emptyNestedTypeDeclarations")
	void testEmptyNestedTypeDeclarations_shouldBeRemoved(String unusedTypeDeclaration, String unusedTypeName)
			throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, unusedTypeDeclaration);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertEquals(1, removedUnusedTypes.size());
		String removedUnusedTypeName = removedUnusedTypes.get(0)
			.getClassMemberIdentifier();
		assertEquals(unusedTypeName, removedUnusedTypeName);
	}

	@Test
	void testEmptyLocalClassDeclaration_shouldBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("local-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String methodWithUusedLocalTypeDeclaration = "" +
				"	void unusedLocalType() {\n" +
				"		class UnusedLocalClass {\n" +
				"\n" +
				"		}\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithUusedLocalTypeDeclaration);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedLocalTypes();
		assertEquals(1, removedUnusedTypes.size());
		String removedUnusedTypeName = removedUnusedTypes.get(0)
			.getClassMemberIdentifier();
		assertEquals("UnusedLocalClass", removedUnusedTypeName);
	}

	@Disabled("local interface declaration is not recognized and therefore not removed.")
	@Test
	void testLocalInterfaceDeclaration_shouldBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("local-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String methodWithUusedLocalTypeDeclaration = "" +
				"	void unusedLocalType() {\n" +
				"		interface UnusedLocalInterface {\n" +
				"\n" +
				"		}\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithUusedLocalTypeDeclaration);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedLocalTypes();
		assertEquals(1, removedUnusedTypes.size());
		String removedUnusedTypeName = removedUnusedTypes.get(0)
			.getClassMemberIdentifier();
		assertEquals("UnusedLocalInterface", removedUnusedTypeName);
	}

	private static Stream<Arguments> nonPrivateModifiers() throws Exception {
		return Stream.of(
				Arguments.of(
						"package-private-classes",
						Collections.emptyList()),
				Arguments.of(
						"protected-classes",
						Arrays.asList("protected")),
				Arguments.of(
						"public-classes",
						Arrays.asList("public")));
	}

	@ParameterizedTest
	@MethodSource(value = "nonPrivateModifiers")
	void testNonPrivateClassDeclarations_shouldBeNonPrivateCandidate(String classModifierOption, List<String> modifiers)
			throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put(classModifierOption, true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, "int dummyField;", modifiers);
		defaultFixture.accept(visitor);

		List<NonPrivateUnusedTypeCandidate> unusedTypeCandidates = visitor.getNonPrivateCandidates();
		assertEquals(1, unusedTypeCandidates.size());
		String unusedTypeCandidateName = unusedTypeCandidates.get(0)
			.getTypeDeclaration()
			.getName()
			.getIdentifier();
		assertEquals(DEFAULT_TYPE_DECLARATION_NAME, unusedTypeCandidateName);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"		UsedClass field;",
			"" +
					"		Object field = new UsedClass();",
			"" +
					"		void consumeUsedClass(UsedClass usedClass) {\n" +
					"		}",
			"" +
					"		void declareUsedClassLocalVariable() {\n" +
					"			UsedClass usedClass;\n" +
					"		}",
			"" +
					"		UsedClass getUsedClass() {\n" +
					"			return null;\n" +
					"		}",
			"" +
					"		class SubclassOfUsedClass extends UsedClass {\n" +
					"		}",

	})
	void testPrivateNestedClassReferenceBySimpleName_shouldNotBeRemoved(String referenceBySimpleName)
			throws Exception {

		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String code = "" +
				referenceBySimpleName + "\n" +
				"\n" +
				"		private class UsedClass {\n" +
				"\n" +
				"		}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		defaultFixture.accept(visitor);
		assertTrue(visitor.getUnusedPrivateTypes()
			.isEmpty());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"UsedClass",
			"? extends UsedClass",
			"? super UsedClass",
	})
	void testPrivateNestedClassUsedAsTypeArgument_shouldNotBeRemoved(String typeArgument)
			throws Exception {

		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		defaultFixture.addImport(java.util.List.class.getName());
		String code = "" +
				"		List<" + typeArgument + "> usedClassList;\n" +
				"\n" +
				"		private class UsedClass {\n" +
				"		}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		defaultFixture.accept(visitor);
		assertTrue(visitor.getUnusedPrivateTypes()
			.isEmpty());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"			ClassReferencingItself referencingItself;",
			"" +
					"			ClassReferencingItself getNullValue() {\n" +
					"				return null;\n" +
					"			}",
			"" +
					"			void use(ClassReferencingItself usedByItself) {\n" +
					"			}",
			"" +
					"			Object callDefaultConstructor() {\n" +
					"				return new ClassReferencingItself();\n" +
					"			}",
	})
	void testPrivateNestedClassReferencingItself_shouldNotBeRemoved(String referenceOnItself)
			throws Exception {

		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classReferencingItself = "" +
				"		private class ClassReferencingItself {\n" +
				referenceOnItself + "\n" +
				"		}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, classReferencingItself);
		defaultFixture.accept(visitor);
		assertTrue(visitor.getUnusedPrivateTypes()
			.isEmpty());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"		UnusedClass: while (true)\n" +
					"			break UnusedClass;",
			"" +
					"		while (true) {\n" +
					"			UnusedClass: while (true)\n" +
					"				continue UnusedClass;\n" +
					"		}",
	})
	void testLabelHavingSameNameAsPrivateUnusedClass_shouldRemoveClass(
			String statementWithLabel) throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String code = "" +
				"	void labelWithSameNameAsUnusedClass() {\n" +
				statementWithLabel + "\n" +
				"	}\n" +
				"	\n" +
				"	private class UnusedClass {\n" +
				"\n" +
				"	}\n" +
				"";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertEquals(1, removedUnusedTypes.size());
		String removedUnusedTypeName = removedUnusedTypes.get(0)
			.getClassMemberIdentifier();
		assertEquals("UnusedClass", removedUnusedTypeName);

	}

	@Test
	void testLocalClassOptionFalse_shouldNotBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("local-classes", false);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String methodWithUusedLocalTypeDeclaration = "" +
				"	void unusedLocalType() {\n" +
				"		class UnusedLocalClass {\n" +
				"\n" +
				"		}\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithUusedLocalTypeDeclaration);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedLocalTypes();
		assertTrue(removedUnusedTypes.isEmpty());
	}

	@Test
	void testClassWithConstructorDeclaration_shouldBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classWithConstructorDeclaration = "" +
				"	private class UnusedClass {\n"
				+ "		UnusedClass() {\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, classWithConstructorDeclaration);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertEquals(1, removedUnusedTypes.size());
		String removedUnusedTypeName = removedUnusedTypes.get(0)
			.getClassMemberIdentifier();
		assertEquals("UnusedClass", removedUnusedTypeName);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"	@interface ExampleAnnotation {\n" +
					"		String value();\n" +
					"	}",
			"" +
					"	enum ExampleEnum {\n" +
					"		ENTRY;\n" +
					"	}",
			"" +
					"	class ExampleClass {\n" +
					"	}",
			"" +
					"	interface ExampleInterface {\n"
					+ "	}",
	})
	void testUnusedNestedClassAmongOtherNestedTypes_shouldBeRemoved(String additionalDeclaration) throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classWithConstructorDeclaration = "" +
				additionalDeclaration + "\n" +
				"	\n" +
				"	private class UnusedClass {\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, classWithConstructorDeclaration);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertEquals(1, removedUnusedTypes.size());
		String removedUnusedTypeName = removedUnusedTypes.get(0)
			.getClassMemberIdentifier();
		assertEquals("UnusedClass", removedUnusedTypeName);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"	@interface ExampleAnnotation {\n" +
					"		String value();\n" +
					"	}",
			"" +
					"	enum ExampleEnum {\n" +
					"		ENTRY;\n" +
					"	}",
			"" +
					"	class ExampleClass {\n" +
					"	}",
			"" +
					"	interface ExampleInterface {\n"
					+ "	}",

	})
	void testClassWithNestedTypeDeclarations_shouldNotBeRemoved(String unsupportedDeclaration) throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classWithUnsupportedDeclaration = "" +
				"	private class ClassWithNestedTypeDeclarations {\n" +
				unsupportedDeclaration + "\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, classWithUnsupportedDeclaration);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertTrue(removedUnusedTypes.isEmpty());

	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"			Object o = new Object() {\n" +
					"			};",
			"" +
					"			void methodWithLocalClass() {\n" +
					"				class LocalClass {\n" +
					"				}\n" +
					"			}",
			"" +
					"			{\n" +
					"				class LocalClass {\n" +
					"				}\n" +
					"			}",
	})
	void testClassWithMembersContainingUnsupportedDeclaration_shouldNotBeRemoved(String unsupportedDeclaration)
			throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classWithUnsupportedDeclaration = "" +
				"		private class ClassWithUnuspportedDeclaration {\n" +
				unsupportedDeclaration + "\n" +
				"		}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, classWithUnsupportedDeclaration);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertTrue(removedUnusedTypes.isEmpty());

	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"		@KeepMe\n" +
					"		int x;",
			"" +
					"		@KeepMe\n" +
					"		void doNothing() {\n" +
					"		}",
			"" +
					"		void doNothing(@KeepMe int x) {\n" +
					"		}",
	})
	void testClassWithAnnotatedMember_shouldNotBeRemoved(String annotatedMember)
			throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classWithUnsupportedDeclaration = "" +
				"	@interface KeepMe {		\n" +
				"	}\n" +
				"	private class ClassWithAnnotatedMember {\n" +
				annotatedMember + "\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, classWithUnsupportedDeclaration);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertTrue(removedUnusedTypes.isEmpty());

	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"		@SuppressWarnings(\"unused\")\n" +
					"		private int i;",
			"" +
					"		@SuppressWarnings(\"unused\")\n" +
					"		private void privateUnusedMethod() {\n" +
					"		}",
			"" +
					"		void methodWithUnusedParameter(@SuppressWarnings(\"unused\") int unusedParameter) {\n" +
					"		}",

	})
	void testUnusedNestedClassWithMembersHavingSuppressWarnings_shouldBeRemoved(
			String memberWithHavingSuppressWarnings)
			throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String unusedClassWithMemberContainingSuppressWarnings = "" +
				"	private class UnusedClass {\n" +
				memberWithHavingSuppressWarnings + "\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME,
				unusedClassWithMemberContainingSuppressWarnings);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertEquals(1, removedUnusedTypes.size());
		String removedUnusedTypeName = removedUnusedTypes.get(0)
			.getClassMemberIdentifier();
		assertEquals("UnusedClass", removedUnusedTypeName);
	}

	@Test
	void testLocalClassWithAnonymousClass_shouldNotBeRemoved()
			throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("local-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String methodWithLocalClass = "" +
				"	void methodWithLocalClass() {\n"
				+ "		class LocalClassWithAnonymousClass {\n"
				+ "			Object o = new Object() {\n"
				+ "			};\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithLocalClass);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertTrue(removedUnusedTypes.isEmpty());

	}

	@Test
	void testClassWithAnnotation_shouldNotBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classWithAnnotation = "" +
				"	@interface KeepMe {		\n" +
				"	}\n" +
				"	@KeepMe\n" +
				"	private class ClassWithAnnotation {\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, classWithAnnotation);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertTrue(removedUnusedTypes.isEmpty());

	}

	@Test
	void testClassWithUndefinedVariable_shouldNotBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classWithAnnotation = "" +
				"	private class ClassWithUndefinedVariable {\n"
				+ "		\n"
				+ "		Object returnUndefinedVariable() {\n"
				+ "			\n"
				+ "			return ClassWithUndefinedVariable;\n"
				+ "		}\n"
				+ "	}\n"
				+ "";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, classWithAnnotation);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertTrue(removedUnusedTypes.isEmpty());

	}

	@ParameterizedTest
	@ValueSource(strings = {
			"this",
			"ClassWithActiveThisReference.this",
	})
	void testClassWithActiveThisReference_shouldNotBeRemoved(String activeThisReference) throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classWithActiveThisReference = "" +
				"		private class ClassWithActiveThisReference {\n" +
				"			Object o = " + activeThisReference + ";\n" +
				"		}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, classWithActiveThisReference);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertTrue(removedUnusedTypes.isEmpty());

	}

	@Test
	void testClassWithThisReferenceOfUndefinedClass_shouldNotBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classWithActiveThisReference = "" +
				"	private class ClassWithThisReferenceOfUndefinedClass {\n" +
				"		Object o = UndefinedClass.this;\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, classWithActiveThisReference);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertTrue(removedUnusedTypes.isEmpty());

	}

	@Test
	void testClassWithThisOfEnclosingClass_shouldBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classWithThisOfEnclosingClass = "" +
				"	class EnclosingClass {\n" +
				"		private class ClassReferencingThisOfEnclosingClass {\n" +
				"			Object o = EnclosingClass.this;\n" +
				"		}\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, classWithThisOfEnclosingClass);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertEquals(1, removedUnusedTypes.size());
		String removedUnusedTypeName = removedUnusedTypes.get(0)
			.getClassMemberIdentifier();
		assertEquals("ClassReferencingThisOfEnclosingClass", removedUnusedTypeName);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"			void callDoSomething() {\n" +
					"				this.doSomething();\n" +
					"			}\n" +
					"\n" +
					"			void doSomething() {\n" +
					"			}",
			"" +
					"			int x = 0;\n" +
					"			int y = this.x;",
	})
	void testClassWithThisAsMemberQualifier_shouldBeRemoved(String members) throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classWithThisAsMemberQualifier = "" +
				"		private class ClassWithMemberAccessQualifiedByThis {\n" +
				"\n" +
				members + "\n" +
				"		}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, classWithThisAsMemberQualifier);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertEquals(1, removedUnusedTypes.size());
		String removedUnusedTypeName = removedUnusedTypes.get(0)
			.getClassMemberIdentifier();
		assertEquals("ClassWithMemberAccessQualifiedByThis", removedUnusedTypeName);
	}

	@Test
	void testGenericClassReferencedByField_shouldNotBeRemoved()
			throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String genericClassReferencedByField = "" +
				"	private Wrapper<Integer> integerWrapper;\n" +
				"	private class Wrapper<T> {\n" +
				"		\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, genericClassReferencedByField);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertTrue(removedUnusedTypes.isEmpty());

	}

	@Test
	void testClassAndFieldWithSameName_shouldBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classAndFieldWithSameName = "" +
				"	private class UnusedClass {" +
				"	}\n" +
				"	int UnusedClass = 0;\n" +
				"	int x = UnusedClass;";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, classAndFieldWithSameName);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertEquals(1, removedUnusedTypes.size());
		String removedUnusedTypeName = removedUnusedTypes.get(0)
			.getClassMemberIdentifier();
		assertEquals("UnusedClass", removedUnusedTypeName);
	}

	@Test
	void testClassReferencingOtherClassWithSameName_shouldBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classReferencingOtherClassWithSameName = "" +
				"	class Example1 {\n"
				+ "		private class UnusedClass {\n"
				+ "			Example2.UnusedClass unusedClassFromExample2;\n"
				+ "		}\n"
				+ "	}\n"
				+ "	class Example2 {\n"
				+ "		class UnusedClass {\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME,
				classReferencingOtherClassWithSameName);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertEquals(1, removedUnusedTypes.size());
		String removedUnusedTypeName = removedUnusedTypes.get(0)
			.getClassMemberIdentifier();
		assertEquals("UnusedClass", removedUnusedTypeName);
	}

	@Test
	void testClassReferencingQualifiedStaticMethodWithinItself_shouldBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classReferencingStaticMethodFieldWithinItself = "" +
				"	private static class UnusedClass {\n"
				+ "		static void staticMethod() {\n"
				+ "			\n"
				+ "		}\n"
				+ "		void useStaticMethod() {\n"
				+ "			UnusedClass.staticMethod();\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME,
				classReferencingStaticMethodFieldWithinItself);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertEquals(1, removedUnusedTypes.size());
		String removedUnusedTypeName = removedUnusedTypes.get(0)
			.getClassMemberIdentifier();
		assertEquals("UnusedClass", removedUnusedTypeName);
	}

	@Test
	void testClassReferencingQualifiedStaticFieldWithinItself_shouldBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		String classReferencingStaticFieldWithinItself = "" +
				"	private static class UnusedClass {\n"
				+ "		static final int ZERO = 0;\n"
				+ "		int useStaticField() {\n"
				+ "			return UnusedClass.ZERO;\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME,
				classReferencingStaticFieldWithinItself);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertEquals(1, removedUnusedTypes.size());
		String removedUnusedTypeName = removedUnusedTypes.get(0)
			.getClassMemberIdentifier();
		assertEquals("UnusedClass", removedUnusedTypeName);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "		int x = ClassDeclaringZERO.ZERO;\n"
					+ "\n"
					+ "		private static class ClassDeclaringZERO {\n"
					+ "			private static final int ZERO = 0;\n"
					+ "\n"
					+ "		}",
			""
					+ "		int x = ClassDeclaringGetZero.getZero();\n"
					+ "\n"
					+ "		private static class ClassDeclaringGetZero {\n"
					+ "			static int getZero() {\n"
					+ "				return 0;\n"
					+ "			}\n"
					+ "		}",
			""
					+ "		int x = EnclosingNestedClass.ClassDeclaringGetZero.getZero();\n"
					+ "\n"
					+ "		static class EnclosingNestedClass {\n"
					+ "			private static class ClassDeclaringGetZero {\n"
					+ "				static int getZero() {\n"
					+ "					return 0;\n"
					+ "				}\n"
					+ "			}\n"
					+ "		}",
	})
	void testClassWithStaticMembersReferencedOutside_shouldNotBeRemoved(String classWithStaticMembersReferencedOutside)
			throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-classes", true);
		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(options);

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME,
				classWithStaticMembersReferencedOutside);
		defaultFixture.accept(visitor);

		List<UnusedTypeWrapper> removedUnusedTypes = visitor.getUnusedPrivateTypes();
		assertTrue(removedUnusedTypes.isEmpty());
	}
}

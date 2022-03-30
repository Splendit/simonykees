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

@SuppressWarnings("nls")
class UnusedTypesCandidatesVisitorTest extends UsesJDTUnitFixture {

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	private static Stream<Arguments> privateNestedTypeDeclarations() throws Exception {
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
	@MethodSource(value = "privateNestedTypeDeclarations")
	void testPrivateNestedTypeDeclarations_shouldBeRemoved(String unusedTypeDeclaration, String unusedTypeName)
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
	void testLocalClassDeclaration_shouldBeRemoved() throws Exception {
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
			"" +
					"		java.util.List<UsedClass> usedClassList;",
			"" +
					"		class WrapperOfUsedClass<T extends UsedClass> {\n" +
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

		List<NonPrivateUnusedTypeCandidate> unusedTypeCandidates = visitor.getNonPrivateCandidates();
		assertTrue(unusedTypeCandidates.isEmpty());
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
					"			void test() {\n" +
					"				referencingItself = null;\n" +
					"			}\n" +
					"\n" +
					"			ClassReferencingItself referencingItself;",
			"" +
					"			Object callDefaultConstructor() {\n" +
					"				return new ClassReferencingItself();\n" +
					"			}",
			"" +
					"			Object o = getNullValue();\n" +
					"\n" +
					"			ClassReferencingItself getNullValue() {\n" +
					"				return null;\n" +
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

		List<NonPrivateUnusedTypeCandidate> unusedTypeCandidates = visitor.getNonPrivateCandidates();
		assertTrue(unusedTypeCandidates.isEmpty());
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
					"	void exampleMethod(int parameter) {\n" +
					"		\n" +
					"	}",
			"" +
					"	enum ExampleEnum {\n" +
					"		ENTRY;\n" +
					"	}",
			"" +
					"	@interface ExampleAnnotation {\n" +
					"		String value();\n" +
					"	}",
	})
	void testUnusedClassAndDeclaration_shouldBeRemoved(String additionalDeclaration) throws Exception {
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
					"			enum Enum {\n" +
					"				ENTRY;\n" +
					"			}",
			"" +
					"			class InnermostClass {\n" +
					"			}",
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
	void testClassContainingUnsupportedDeclaration_shouldNotBeRemoved(String unsupportedDeclaration) throws Exception {
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

}

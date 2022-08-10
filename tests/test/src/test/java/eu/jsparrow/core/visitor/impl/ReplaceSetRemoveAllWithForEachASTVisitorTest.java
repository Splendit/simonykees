package eu.jsparrow.core.visitor.impl;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

class ReplaceSetRemoveAllWithForEachASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new ReplaceSetRemoveAllWithForEachASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	public static Stream<Arguments> arguments_removeAllWithSupportedTypes() {
		return Stream.of(
				Arguments.of(java.util.Set.class, java.util.List.class),
				Arguments.of(java.util.AbstractSet.class, java.util.List.class),
				Arguments.of(java.util.HashSet.class, java.util.List.class),
				Arguments.of(java.util.HashSet.class, java.util.ArrayList.class));
	}

	@ParameterizedTest
	@MethodSource("arguments_removeAllWithSupportedTypes")
	void visit_SetRemoveAll_shouldTransform(Class<?> setType, Class<?> listType) throws Exception {
		defaultFixture.addImport(setType.getName());
		defaultFixture.addImport(listType.getName());

		String testMethodParameters = String.format("%s<String> strings, %s<String> stringsToRemove",
				setType.getSimpleName(), listType.getSimpleName());
		String original = "" +
				"	void removeElementsFromSet(" + testMethodParameters + ") {\n" +
				"		strings.removeAll(stringsToRemove);\n" +
				"	}";

		String expected = "" +
				"	void removeElementsFromSet(" + testMethodParameters + ") {\n" +
				"		stringsToRemove.forEach(strings::remove);\n" +
				"	}";

		assertChange(original, expected);
	}

	public static Stream<Arguments> arguments_removeAllWithUnsupportedTypes() {
		return Stream.of(
				Arguments.of(java.util.List.class, java.util.List.class),
				Arguments.of(java.util.Set.class, java.util.Collection.class),
				Arguments.of(java.util.Set.class, java.util.Set.class));
	}

	@ParameterizedTest
	@MethodSource("arguments_removeAllWithUnsupportedTypes")
	void visit_RemoveAllWithUnsupportedTypes_shouldNotTransform(Class<?> setType, Class<?> listType) throws Exception {
		defaultFixture.addImport(setType.getName());
		defaultFixture.addImport(listType.getName());

		String testMethodParameters = String.format("%s<String> strings, %s<String> stringsToRemove",
				setType.getSimpleName(), listType.getSimpleName());
		String original = "" +
				"	void removeElementsFromSet(" + testMethodParameters + ") {\n" +
				"		strings.removeAll(stringsToRemove);\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	void visit_InvocationArgumentTypeNotImported_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.Set.class.getName());
		String original = "" +
				"	void exampleWithParametersForSetAndList(Set<String> stringSet, List<String> stringsToRemove) {\n" +
				"		stringSet.removeAll(stringsToRemove);\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	void visit_InvocationExpressionTypeNotImported_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.List.class.getName());
		String original = "" +
				"	void exampleWithParametersForSetAndList(Set<String> stringSet, List<String> stringsToRemove) {\n" +
				"		stringSet.removeAll(stringsToRemove);\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_MethodNameNotRemoveAll_shouldNotTransform() throws Exception {
		String original = "" +
				"Integer integer = Integer.valueOf(0);";
		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"boolean removed = removeAll(1);",
			"removeAll(1);",
			"this.removeAll(1,2,3);"
	})
	void visit_RemoveAllMethodNotInExpressionStatement_shouldNotTransform(String statement) throws Exception {

		String original = "" +
				"	void callRemoveAll() {\n" +
				"		" + statement + "\n" +
				"	}\n" +
				"	\n" +
				"	boolean removeAll(Integer... arguments) {\n" +
				"		return true;\n" +
				"	}";
		assertNoChange(original);
	}

}
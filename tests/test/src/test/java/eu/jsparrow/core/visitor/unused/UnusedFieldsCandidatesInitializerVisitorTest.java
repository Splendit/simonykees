package eu.jsparrow.core.visitor.unused;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings("nls")
public class UnusedFieldsCandidatesInitializerVisitorTest extends UsesJDTUnitFixture {

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}
	
	class TestRemoveUnusedPrivateField {
		
	}

	private static Stream<String> privateFragmentWithInitializer() {
		return Stream.of(
				"private Object unusedField = new Object();",
				"private String unusedField = new String();",
				"private String unusedField = new String(\"s\");",
				"" +
						"private String s = \"s\";\n" +
						"private String unusedField = new String(s);\n",
				"private Double unusedField = new Double(0);",
				"private ArrayList<String> unusedField = new ArrayList<>();",
				"" +
						"private int arrayListCapacity = 10;\n" +
						"private ArrayList<String> unusedField = new ArrayList<>(this.arrayListCapacity);\n",
				"private ArrayList<String> unusedField = new ArrayList<>(new ArrayList<>());",
				"private ArrayList<String> unusedField = new ArrayList<>(Arrays.asList(\"s1\", \"s2\"));",
				"private HashMap<String, String> unusedField = new HashMap<>();");
	}

	@ParameterizedTest
	@MethodSource(value = "privateFragmentWithInitializer")
	void testPrivateFragmentWithInitializer_shouldBeRemoved(String original) throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		options.put("remove-initializers-side-effects", false);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);

		defaultFixture.addImport(java.util.ArrayList.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());
		defaultFixture.addImport(java.util.HashMap.class.getName());

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> removedUnusedFields = visitor.getUnusedPrivateFields();
		assertFalse(removedUnusedFields.isEmpty());
		assertEquals(1, removedUnusedFields.size());
		String removedUnusedFieldName = removedUnusedFields.get(0).getFieldName();
		assertEquals("unusedField", removedUnusedFieldName);
	}
}

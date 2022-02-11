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

class UnusedFieldsCandidatesVisitorTest extends UsesJDTUnitFixture {
	
	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}
	
	private static Stream<String> unusedPrivate() {
		String sample1 = "private int unusedPrivate = 0;";
		String sample1_1 = "private Class<?> unusedPrivate = String.class;";
		String sample1_2 = "private char unusedPrivate = 'c';";
		String sample1_3 = "private String unusedPrivate = \"string\";";
		String sample1_4 = "private Object unusedPrivate = null;";
		String sample1_5 = "private Boolean unusedPrivate = false;";
		String sample1_6 = "private Object o = new Object(); private Object unusedPrivate = this.o;";
		String sample1_7 = "private Object unusedPrivate;";
		String sample1_8 = "private Object o = new Object(); private String unusedPrivate = \"\";";
		String sample2 = ""
				+ "private int value2 = 0;\n"
				+ "private int unusedPrivate = value2;";
		String sample3 = ""
				+ "public int value = 0;\n"
				+ "private int unusedPrivate = 0;";
		String sample3_1 = ""
				+ "protected int value = 0;\n"
				+ "private int unusedPrivate = 0;";
		String sample3_2 = ""
				+ "int value = 0;\n"
				+ "private int unusedPrivate = 0;";
		String sample4 = ""
				+ "private int unusedPrivate = 0;\n"
				+ "private int used = 0;\n"
				+ "private void sample() {\n"
				+ "	unusedPrivate = 1;\n"
				+ "	used = 10;\n"
				+ "	System.out.println(used);\n"
				+ "}\n";
		return Stream.of(
				sample1, 
				sample1_1,
				sample1_2,
				sample1_3,
				sample1_4,
				sample1_5,
				sample1_6,
				sample1_7,
				sample1_8,
				sample2,
				sample3,
				sample3_1,
				sample3_2,
				sample4);
	}
	
	@ParameterizedTest
	@MethodSource(value = "unusedPrivate")
	void testUnusedPrivateField(String original) throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);
		
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		defaultFixture.accept(visitor);
		
		List<UnusedFieldWrapper> unusedPrivateFields = visitor.getUnusedPrivateFields();
		assertFalse(unusedPrivateFields.isEmpty());
		assertEquals(1, unusedPrivateFields.size());
		UnusedFieldWrapper unusedField = unusedPrivateFields.get(0);
		String unusedFieldName = unusedField.getFieldName();
		assertEquals("unusedPrivate", unusedFieldName);
	}
}

package eu.jsparrow.core.visitor.unused;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

class UnusedFieldsCandidatesVisitorTest extends UsesJDTUnitFixture {
	
	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}
	
	private static Stream<String> unusedPrivate() {
		String sample1_0 = "private int unusedPrivate = 0;";
		String sample1_1 = "private Class<?> unusedPrivate = String.class;";
		String sample1_2 = "private char unusedPrivate = 'c';";
		String sample1_3 = "private String unusedPrivate = \"string\";";
		String sample1_4 = "private Object unusedPrivate = null;";
		String sample1_5 = "private Boolean unusedPrivate = false;";
		String sample1_6 = "private Object o = new Object() {}; private Object unusedPrivate = this.o;";
		String sample1_7 = "private Object unusedPrivate;";
		String sample1_8 = "private Object o = new Object() {}; private String unusedPrivate = \"\";";
		String sample1_9 = "private String unusedPrivate = \"\", secondUnusedFragment = \"shouldNotBeRemoved\";";
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
				sample1_0, 
				sample1_1,
				sample1_2,
				sample1_3,
				sample1_4,
				sample1_5,
				sample1_6,
				sample1_7,
				sample1_8,
				sample1_9,
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
	
	private static Stream<Arguments> unusedNonPrivate() {
		String sample1 = "protected int unusedNonPrivate = 0;";
		String sample2 = ""
				+ "private int value = 0;\n"
				+ "protected int unusedNonPrivate = 0;";
		String sample2_1 = ""
				+ "int value = 0;\n"
				+ "protected int unusedNonPrivate = 0;";
		String sample2_2 = ""
				+ "public int value = 0;\n"
				+ "protected int unusedNonPrivate = 0;";
		
		
		String sample3 = "int unusedNonPrivate = 0;";
		String sample3_0 = ""
				+ "private int value = 0;\n"
				+ "int unusedNonPrivate = 0;";
		String sample3_1 = ""
				+ "public int value = 0;\n"
				+ "int unusedNonPrivate = 0;";
		String sample3_2 = ""
				+ "protected int value = 0;\n"
				+ "int unusedNonPrivate = 0;";
		
		String sample4 = "public int unusedNonPrivate = 0;";
		String sample4_0 = ""
				+ "private int value = 0;\n"
				+ "public int unusedNonPrivate = 0;";
		String sample4_1 = ""
				+ "protected int value = 0;\n"
				+ "public int unusedNonPrivate = 0;";
		String sample4_2 = ""
				+ "int value = 0;\n"
				+ "public int unusedNonPrivate = 0;";
		
		String sample4_3 = ""
				+ "@java.io.Serial\n"
				+ "public String value = \"\";\n"
				+ "public int unusedNonPrivate = 0;";
		
		String sample4_4 = ""
				+ "@Deprecated\n"
				+ "public int unusedNonPrivate = 0;";
		
		String sample4_5 = ""
				+ "@SuppressWarnings(\"unused\")\n"
				+ "public int unusedNonPrivate = 0;";
		
		
		String protectedFields = "protected-fields";
		String packagePrivateFields = "package-private-fields";
		String publicFields = "public-fields";
		
		return Stream.of(
				Arguments.of(sample1, protectedFields), 
				Arguments.of(sample2, protectedFields),
				Arguments.of(sample2_1, protectedFields),
				Arguments.of(sample2_2, protectedFields),
				
				Arguments.of(sample3, packagePrivateFields), 
				Arguments.of(sample3_0, packagePrivateFields),
				Arguments.of(sample3_1, packagePrivateFields),
				Arguments.of(sample3_2, packagePrivateFields),
				
				Arguments.of(sample4, publicFields), 
				Arguments.of(sample4_0, publicFields),
				Arguments.of(sample4_1, publicFields),
				Arguments.of(sample4_2, publicFields),
				Arguments.of(sample4_3, publicFields),
				Arguments.of(sample4_4, publicFields),
				Arguments.of(sample4_5, publicFields)
				
			);
	}
	
	
	@ParameterizedTest
	@MethodSource(value = "unusedNonPrivate")
	void test_nonPrivateUnusedField(String code, String selectedModifier) throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put(selectedModifier, true);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);
		
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		defaultFixture.accept(visitor);
		
		List<UnusedFieldWrapper> unusedPrivateFields = visitor.getUnusedPrivateFields();
		assertTrue(unusedPrivateFields.isEmpty());
		List<NonPrivateUnusedFieldCandidate> candidates = visitor.getNonPrivateCandidates();
		assertEquals(1, candidates.size());
		NonPrivateUnusedFieldCandidate candidate = candidates.get(0);
		String unusedFieldName = candidate.getFragment().getName().getIdentifier();
		assertEquals("unusedNonPrivate", unusedFieldName);
	}
	
	
	private static Stream<String> serialVersionUIDDeclarations() {
		return Stream.of(
				"private static final long serialVersionUID = 0L;",
				"protected static final long serialVersionUID = 0L;",
				"static final long serialVersionUID = 0L;",
				"public static final long serialVersionUID = 0L;"
				);
	}
	
	@ParameterizedTest
	@MethodSource(value = "serialVersionUIDDeclarations")
	void test_keepingSerialVersionUID(String code) throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		options.put("protected-fields", true);
		options.put("package-private-fields", true);
		options.put("public-fields", true);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);
		
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		defaultFixture.accept(visitor);
		
		List<UnusedFieldWrapper> unusedPrivateFields = visitor.getUnusedPrivateFields();
		List<NonPrivateUnusedFieldCandidate> candidates = visitor.getNonPrivateCandidates();
		assertTrue(unusedPrivateFields.isEmpty());
		assertTrue(candidates.isEmpty());
	}
	
}

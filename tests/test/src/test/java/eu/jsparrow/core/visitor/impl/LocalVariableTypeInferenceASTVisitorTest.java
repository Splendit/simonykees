package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.java10.LocalVariableTypeInferenceASTVisitor;

@SuppressWarnings("nls")
public class LocalVariableTypeInferenceASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private static final String JAVA_UTIL_MAP = java.util.Map.class.getName();
	private static final String JAVA_UTIL_LIST = java.util.List.class.getName();
	private static final String JAVA_UTIL_LINKED_LIST = java.util.LinkedList.class.getName();
	private static final String JAVA_UTIL_ARRAY_LIST = java.util.ArrayList.class.getName();
	private static final String JAVA_UTIL_HASH_MAP = java.util.HashMap.class.getName();
	private static final String JAVA_UTIL_ARRAYS = java.util.Arrays.class.getName();
	private static final String JAVA_IO_FILE = java.io.File.class.getName();
	private static final String JAVA_UTIL_DATE = java.util.Date.class.getName();

	@BeforeEach
	public void setUp() {
		setVisitor(new LocalVariableTypeInferenceASTVisitor());
	}

	@Test
	public void visit_simpleTypeInitialization_shouldReplace() throws Exception {		
		String original = "Date date = new Date();";
		String expected = "var date = new Date();";
		
		fixture.addImport(JAVA_UTIL_DATE);
		
		assertChange(original, expected);
	}

	@Test
	public void visit_enhancedForLoopDeclaration_shouldReplace() throws Exception {		
		String original = "for(String string : Arrays.asList(\"1\", \"2\", \"3\")) {}";
		String expected = "for(var string : Arrays.asList(\"1\", \"2\", \"3\")) {}";
		
		fixture.addImport(JAVA_UTIL_ARRAYS);
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_forLoop_shouldReplace() throws Exception {
		String original = "String[]values = Arrays.asList(\"1\", \"2\", \"3\"); int i =0; for(String value = values[i]; i < values.length; i++) {}";
		String expected = "String[]values = Arrays.asList(\"1\", \"2\", \"3\"); int i =0; for(var value = values[i]; i < values.length; i++) {}";

		fixture.addImport(JAVA_UTIL_ARRAYS);
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_forLoopMultipleInitializations_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_ARRAYS);
		
		assertNoChange("String[]values = Arrays.asList(\"1\", \"2\", \"3\"); int i =0; for(String value = values[i], suffix = \"s\"; i < values.length; i++) {}");
	}

	@Test
	public void visit_varInEnhancedForLoop_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_ARRAYS);		
		
		assertNoChange("for(var string : Arrays.asList(\"1\", \"2\", \"3\")) {}");
	}

	@Test
	public void visit_arrayInEnhancedForLoop_shouldReplace() throws Exception {
		String original = "String [] strings; strings = new String [] {\"\", \"\"}; for(String string : strings) {}";
		String expected = "String [] strings; strings = new String [] {\"\", \"\"}; for(var string : strings) {}";
		
		assertChange(original, expected);
	}

	@Test
	public void visit_rawTypes_shouldReplace() throws Exception {
		String original = "HashMap map = new HashMap();";
		String expected = "var map = new HashMap();";
		
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		
		assertChange(original, expected);
	}

	@Test
	public void visit_rawInitializer_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		
		assertNoChange("HashMap<String, String> map = new HashMap();");
	}

	@Test
	public void visit_rawDeclarationType_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		
		assertNoChange("HashMap map = new HashMap<String, String>();");
	}

	@Test
	public void visit_primitiveType_shouldNotReplace() throws Exception {
		assertNoChange("int i = 0;");
	}

	@Test
	public void visit_alreadyVar_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_DATE);
		
		assertNoChange("var map = new Date();");
	}

	@Test
	public void visit_diamondInInitialization_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		
		assertNoChange("HashMap<String, String> map = new HashMap<>();");
	}

	@Test
	public void visit_wildcardInInitialization_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		
		assertNoChange("HashMap<Object, Object> map = new HashMap<?, ?>();");
	}

	@Test
	public void visit_multipleDeclarationFragments_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		
		assertNoChange("HashMap<String, String> map1 = new HashMap<String, String>(), map2 = new HashMap<String, String>();");
	}

	@Test
	public void visit_nullTypeInitialization_shouldNotReplace() throws Exception {
		assertNoChange("String nullValue = null;");
	}

	@Test
	public void visit_subTypeInitialization_shouldReplace() throws Exception {
		String original = "Map<String, String> map = new HashMap<String, String>();";
		String expected = "var map = new HashMap<String, String>();";

		fixture.addImport(JAVA_UTIL_HASH_MAP);
		fixture.addImport(JAVA_UTIL_MAP);
		
		assertChange(original, expected);
	}

	@Test
	public void visit_incompatibleSiblingReInitialization_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_ARRAY_LIST);
		fixture.addImport(JAVA_UTIL_LINKED_LIST);
		fixture.addImport(JAVA_UTIL_LIST);
		
		assertNoChange("int anotherVariable = 0; List<String> list = new ArrayList<String>(); list = new LinkedList<>(); anotherVariable = 1;");
	}

	@Test
	public void visit_initializationWithWildcard_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		fixture.addImport(JAVA_UTIL_MAP);
		
		assertNoChange("Map<Object, Object> map = new HashMap<?,?>();");
	}

	@Test
	public void visit_simpleTypeWithoutInitialization_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_DATE);
		
		assertNoChange("Date date; date = new Date();");
	}

	@Test
	public void visit_singleVariableDeclarationInCatchBlock_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_IO_FILE);
		
		assertNoChange("try {new File(\"file.name\");} catch(NullPointerException e) {}");
	}

	@Test
	public void visit_initializationWithLambda_shouldNotReplace() throws Exception {
		assertNoChange("Runnable r = () -> {};");
	}

	@Test
	public void visit_singleDeclarationInLambdaParameter_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Predicate");
		
		assertNoChange("Predicate<String>  myPredicate = (String value) -> value.isEmpty();");
	}

	@Test
	public void visit_missingSpaceBetweenTypeAndName_shouldReplace() throws Exception {
		String original = "List<String>name = new ArrayList<String>();";
		String expected = "var name = new ArrayList<String>();";
		
		fixture.addImport(JAVA_UTIL_LIST);
		fixture.addImport(JAVA_UTIL_ARRAY_LIST);
		
		assertChange(original, expected);
	}

	@Test
	public void visit_arrayTypeWithDimensions_shouldTransform() throws Exception {
		String original = "String []names[] = new String[][] {};";
		String expected = "var names = new String[][]{};";
		
		assertChange(original, expected);
	}

	@Test
	public void visit_arrayInitializer_shouldNotTransform() throws Exception {
		assertNoChange("String []names[] = {{}};");
	}
	
	@Test
	public void visit_methodReferenceInitializer_shouldNotTransform() throws Exception {
		assertNoChange("Runnable r = this::hashCode;");
	}

	@Test
	public void visit_2dArray_shouldTransform() throws Exception {
		String original = "String names[][] = {}; for(String t[] : names) { }";
		String expected = "String names[][] = {}; for(var t : names) { }";
		
		assertChange(original, expected);
	}

	@Test
	public void visit_conditionalExpressionHavingDiamond_shouldNotTransform() throws Exception {
		
		fixture.addImport(JAVA_UTIL_LIST);
		fixture.addImport(JAVA_UTIL_ARRAY_LIST);
		fixture.addImport(JAVA_UTIL_LINKED_LIST);
		
		assertNoChange("List<String> names = true ? new ArrayList<>() : new LinkedList<>();");
	}

	@Test
	public void visit_conditionalExpression_shouldTransform() throws Exception {
		String original = "List<String> names = true ? new ArrayList<String>() : new LinkedList<String>();";
		String expected = "var names = true ? new ArrayList<String>() : new LinkedList<String>();";
		
		fixture.addImport(JAVA_UTIL_LIST);
		fixture.addImport(JAVA_UTIL_ARRAY_LIST);
		fixture.addImport(JAVA_UTIL_LINKED_LIST);
		
		assertChange(original, expected);
	}

	@Test
	public void visit_anonymousClasses_shouldNotTransform() throws Exception {
		assertNoChange("Runnable r = new Runnable() { @Override public void run() { }};");
	}
}

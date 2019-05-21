package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.rules.java10.factory.methods.CollectionsFactoryMethodsASTVisitor;

@SuppressWarnings("nls")
public class CollectionsFactoryMethodsASTVisitorTest extends UsesJDTUnitFixture {
	
	private static final String JAVA_UTIL_MAP = java.util.Map.class.getName();
	private static final String JAVA_UTIL_LIST = java.util.List.class.getName();
	private static final String JAVA_UTIL_LINKED_LIST = java.util.LinkedList.class.getName();
	private static final String JAVA_UTIL_ARRAY_LIST = java.util.ArrayList.class.getName();
	private static final String JAVA_UTIL_HASH_MAP = java.util.HashMap.class.getName();
	private static final String JAVA_UTIL_ARRAYS = java.util.Arrays.class.getName();
	private static final String JAVA_UTIL_COLLECTIONS = java.util.Collections.class.getName();

	
	private CollectionsFactoryMethodsASTVisitor visitor;
	
	@BeforeEach
	public void setUp() throws Exception {
		visitor = new CollectionsFactoryMethodsASTVisitor();
		fixture.addImport(JAVA_UTIL_COLLECTIONS);
	}
	
	@Test
	public void visit_immutableListOfArraysAsList_shouldTransform() throws Exception {
		
		String original = "List<String> list = Collections.unmodifiableList(Arrays.asList(\"1\", \"2\"));";
		String expected = "List<String> list = List.of(\"1\", \"2\");";
		fixture.addImport(JAVA_UTIL_ARRAYS);
		fixture.addImport(JAVA_UTIL_LIST);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_immutableListOfAnonymousArrayList_shouldTransform() throws Exception {
		
		String original = "List<String> list = Collections.unmodifiableList(new ArrayList<String>() {{\n" + 
				"			add(\"1\");\n" + 
				"			add(\"2\");\n" + 
				"		}});";
		String expected = "List<String> list = List.of(\"1\", \"2\");";
		fixture.addImport(JAVA_UTIL_LIST);
		fixture.addImport(JAVA_UTIL_ARRAY_LIST);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_emptyUnmodifiableList_shouldTransform() throws Exception {
		
		String original = "List<String> list = Collections.unmodifiableList(new ArrayList<String>() {{\n" + 
				"		}});";
		String expected = "List<String> list = List.of();";
		fixture.addImport(JAVA_UTIL_LIST);
		fixture.addImport(JAVA_UTIL_ARRAY_LIST);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_immutableMapOfAnonymousArrayList_shouldTransform() throws Exception {
		
		String original = "Map<String, String> map = Collections.unmodifiableMap(new HashMap<String, String>() {{\n" + 
				"			put(\"1\", \"one\");\n" + 
				"			put(\"2\", \"two\");\n" + 
				"		}});";
		String expected = "Map<String, String> map = Map.of(\"1\", \"one\", \"2\", \"two\");";
		fixture.addImport(JAVA_UTIL_MAP);
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_nullElement_shouldNotTransform() throws Exception {
		
		String original = "List<String> list = Collections.unmodifiableList(Arrays.asList(\"1\", \"2\", null));";
		fixture.addImport(JAVA_UTIL_ARRAYS);
		fixture.addImport(JAVA_UTIL_LIST);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_emptyList_shouldNotTransform() throws Exception {
		
		String original = "List<String> list = Collections.emptyList();";
		fixture.addImport(JAVA_UTIL_ARRAYS);
		fixture.addImport(JAVA_UTIL_LIST);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_convertingToImmutableMap_shouldTransform() throws Exception {
		
		String original = "" +
				"		Map<String, String> map = new HashMap<>();\n" + 
				"		map.put(\"1\", \"one\");\n" + 
				"		map.put(\"2\", \"two\");\n" + 
				"		Map<String, String> m = Collections.unmodifiableMap(map);";
		String expected = "Map<String, String> m = Map.of(\"1\", \"one\", \"2\", \"two\");";
		fixture.addImport(JAVA_UTIL_MAP);
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}

	@Test
	public void visit_emptyMap_shouldTransform() throws Exception {
		
		String original = "" +
				"		Map<String, String> map = new HashMap<>();\n" + 
				"		Map<String, String> m = Collections.unmodifiableMap(map);";
		String expected = "Map<String, String> m = Map.of();";
		fixture.addImport(JAVA_UTIL_MAP);
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_skipUnrelatedStatements_shouldTransform() throws Exception {
		
		String original = "" +
				"		Map<String, String> map3 = new HashMap<>();\n" + 
				"		Map<String, String> map = new HashMap<>();\n" + 
				"		map.put(\"1\", \"one\");\n" + 
				"		map.put(\"2\", \"two\");\n" + 
				"		map3.put(\"3\", \"three\");\n" + 
				"		Map<String, String> m = Collections.unmodifiableMap(map);";
		String expected = "" +
				"		Map<String, String> map3 = new HashMap<>();\n" +
				"		map3.put(\"3\", \"three\");\n" +
				"		Map<String, String> m = Map.of(\"1\", \"one\", \"2\", \"two\");";
		fixture.addImport(JAVA_UTIL_MAP);
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_reuseAfterUnmodifiableInvocation_shouldNotTransform() throws Exception {
		
		String original = "" +
				"		Map<String, String> map = new HashMap<>();\n" + 
				"		map.put(\"1\", \"one\");\n" + 
				"		map.put(\"2\", \"two\");\n" + 
				"		Map<String, String> m = Collections.unmodifiableMap(map);\n" + 
				"		map.put(\"4\", \"5\");";

		fixture.addImport(JAVA_UTIL_MAP);
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingDeclaration_shouldNotTransform() throws Exception {
		
		String original = "" +
				"		Map<String, String> map = new HashMap<>();\n" +
				"		if(true) {" + 
				"			map.put(\"1\", \"one\");\n" + 
				"			map.put(\"2\", \"two\");\n" + 
				"			Map<String, String> m = Collections.unmodifiableMap(map);\n" + 
				"		}";

		fixture.addImport(JAVA_UTIL_MAP);
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_unremovableStatement_shouldNotTransform() throws Exception {
		
		String original = "" +
				"		Map<String, String> map = new HashMap<>();\n" +
				"		if(true) {" + 
				"			map.put(\"1\", \"one\");\n" + 
				"		}\n" +
				"		map.put(\"2\", \"two\");\n" + 
				"		Map<String, String> m = Collections.unmodifiableMap(map);\n" + 
				"		";

		fixture.addImport(JAVA_UTIL_MAP);
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_unremovableDeclaration_shouldNotTransform() throws Exception {
		
		String original = "" +
				"		Map<String, String> map = new HashMap<>();\n" +
				"		Map<String, String> map2 = map;\n" +
				"		map.put(\"1\", \"one\");\n" + 
				"		map.put(\"2\", \"two\");\n" + 
				"		Map<String, String> m = Collections.unmodifiableMap(map);\n" + 
				"		";

		fixture.addImport(JAVA_UTIL_MAP);
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	/*
	 * Anonymous Class Argument - Negative tests. 
	 */
	
	@Test
	public void visit_missingAnonymousClass_shouldNotTransform() throws Exception {
		
		String original = "" +
				"List<String> list2 = Collections.unmodifiableList(new ArrayList<>());";

		fixture.addImport(JAVA_UTIL_LIST);
		fixture.addImport(JAVA_UTIL_ARRAY_LIST);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_usingAddAll_shouldNotTransform() throws Exception {
		
		String original = "" +
				"		List<String> list1 = Collections.unmodifiableList(new ArrayList<String>() {{\n" + 
				"			add(\"value\");\n" + 
				"			add(\"value2\");\n" + 
				"			addAll(Arrays.asList(\"\" , \"\"));\n" + 
				"		}});";

		fixture.addImport(JAVA_UTIL_LIST);
		fixture.addImport(JAVA_UTIL_ARRAY_LIST);
		fixture.addImport(JAVA_UTIL_ARRAYS);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_unmodifiableCollection_shouldNotTransform() throws Exception {
		
		String original = "" +
				"Collection<String> collection = Collections.unmodifiableCollection(new ArrayList<String>() {{}});";

		fixture.addImport(java.util.Collection.class.getName());
		fixture.addImport(JAVA_UTIL_ARRAY_LIST);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	private void sampleCode() {
		List<String> list = Collections.unmodifiableList(Arrays.asList("1", "2"));
		list = Collections.unmodifiableList(new ArrayList<String>() {{
			add("1");
			add("2");
		}});
		
		Collections.emptyList();
		
		Map<String, String> map2 = Collections.unmodifiableMap(new HashMap<String, String>() {{
			put("1", "one");
			put("2", "two");
		}});
		
		Map<String, String> map = new HashMap<>();
		map.put("1", "one");
		map.put("2", "two");
		Map<String, String> m = Collections.unmodifiableMap(map);
		map.put("4", "5");
		
	}

}

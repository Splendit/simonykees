package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.rules.java10.factorymethods.CollectionsFactoryMethodsASTVisitor;

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
	public void setUp() {
		visitor = new CollectionsFactoryMethodsASTVisitor();

	}
	
	@Test
	public void visit_immutableListOfArraysAsList_shouldTransform() throws Exception {
		
		String original = "List<String> list = Collections.unmodifiableList(Arrays.asList(\"1\", \"2\"));";
		String expected = "List<String> list = List.of(\"1\", \"2\");";
		fixture.addImport(JAVA_UTIL_ARRAYS);
		fixture.addImport(JAVA_UTIL_LIST);
		fixture.addImport(JAVA_UTIL_COLLECTIONS);
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
		fixture.addImport(JAVA_UTIL_COLLECTIONS);
		fixture.addImport(JAVA_UTIL_ARRAY_LIST);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	private void sampleCode() {
		List<String> list = Collections.unmodifiableList(Arrays.asList("1", "2"));
		list = Collections.unmodifiableList(new ArrayList<String>() {{
			add("1");
			add("2");
		}});
	}

}

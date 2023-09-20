package eu.jsparrow.core.visitor.impl.entryset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

class IterateMapEntrySetASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new IterateMapEntrySetASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_iterateMapKeyAndValue_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.Map.class.getName());
		
		String useKeyAndValueMethod = "\n" +
				"	static <K, V> void useKeyAndValue(K key, V value) {\n" +
				"	}";

		String original = "" +
				"	void iterateMapKeyAndValue(Map<String, Integer> map) {\n" +
				"		for (String key : map.keySet()) {\n" +
				"			Integer value = map.get(key);\n" +
				"			useKeyAndValue(key, value);\n" +
				"		}\n" +
				"	}" +
				useKeyAndValueMethod;

		String expected = "" +
				"	void iterateMapKeyAndValue(Map<String, Integer> map) {\n" +
				"		for (Map.Entry<String,Integer> entry : map.entrySet()) {\n" +
				"			String key = entry.getKey();\n" +
				"			Integer value = entry.getValue();\n" +
				"			useKeyAndValue(key, value);\n" +
				"		}\n" +
				"	}" +
				useKeyAndValueMethod;

		assertChange(original, expected);
	}

	// @Test
	// void visit__shouldTransform() throws Exception {
	// String original = "";
	// String expected = "";
	// assertChange(original, expected);
	// }
}

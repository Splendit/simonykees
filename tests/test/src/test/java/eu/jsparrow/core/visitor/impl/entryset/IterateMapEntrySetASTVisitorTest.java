package eu.jsparrow.core.visitor.impl.entryset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

class IterateMapEntrySetASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		defaultFixture.addImport(java.util.Map.class.getName());
		setDefaultVisitor(new IterateMapEntrySetASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_iterateMapKeyAndValue_shouldTransform() throws Exception {
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

	@Test
	void visit_avoidEntryAsDuplicateVariable_shouldTransform() throws Exception {
		String useKeyAndValueMethod = "\n" +
				"	static <K, V> void useKeyAndValue(K key, V value) {\n" +
				"	}";

		String original = "" +
				"	void iterateMapKeyAndValue(Map<String, Integer> map) {\n" +
				"		Object entry;" +
				"		for (String key : map.keySet()) {\n" +
				"			Integer value = map.get(key);\n" +
				"			useKeyAndValue(key, value);\n" +
				"		}\n" +
				"	}" +
				useKeyAndValueMethod;

		String expected = "" +
				"	void iterateMapKeyAndValue(Map<String, Integer> map) {\n" +
				"		Object entry;" +
				"		for (Map.Entry<String,Integer> entry1 : map.entrySet()) {\n" +
				"			String key = entry1.getKey();\n" +
				"			Integer value = entry1.getValue();\n" +
				"			useKeyAndValue(key, value);\n" +
				"		}\n" +
				"	}" +
				useKeyAndValueMethod;

		assertChange(original, expected);
	}


	@Test
	void visit_introduceEntryVariableThreeTimes_shouldTransform() throws Exception {

		String useKeyAndValueMethod = "\n" +
				"	static <K, V> void useKeyAndValue(K key, V value) {\n" +
				"	}";

		String original = "" +
				"	void iterateMapKeyAndValue(Map<String, Integer> map) {\n" +
				"		for (String key : map.keySet()) {\n" +
				"			Integer value = map.get(key);\n" +
				"			useKeyAndValue(key, value);\n" +
				"		}\n" +
				"		for (String key : map.keySet()) {\n" +
				"			Integer value = map.get(key);\n" +
				"			useKeyAndValue(key, value);\n" +
				"		}\n" +
				"		for (String key : map.keySet()) {\n" +
				"			Integer value = map.get(key);\n" +
				"			useKeyAndValue(key, value);\n" +
				"		}\n" +
				"	}" +
				useKeyAndValueMethod;

		String expected = "" +
				"	void iterateMapKeyAndValue(Map<String, Integer> map) {\n" +
				"		for (Map.Entry<String, Integer> entry : map.entrySet()) {\n" +
				"			String key = entry.getKey();\n" +
				"			Integer value = entry.getValue();\n" +
				"			useKeyAndValue(key, value);\n" +
				"		}\n" +
				"		for (Map.Entry<String, Integer> entry1 : map.entrySet()) {\n" +
				"			String key = entry1.getKey();\n" +
				"			Integer value = entry1.getValue();\n" +
				"			useKeyAndValue(key, value);\n" +
				"		}\n" +
				"		for (Map.Entry<String, Integer> entry2 : map.entrySet()) {\n" +
				"			String key = entry2.getKey();\n" +
				"			Integer value = entry2.getValue();\n" +
				"			useKeyAndValue(key, value);\n" +
				"		}\n" +
				"	}" +
				useKeyAndValueMethod;

		assertChange(original, expected);
	}
	
	@Test
	void visit_keyWithExtraDimensions_shouldTransform() throws Exception {

		String useKeyAndValueMethod = "\n" +
				"	static <K, V> void useKeyAndValue(K key, V value) {\n" +
				"	}";

		String original = "" +
				"		void iterateMapKeyAndValue2(Map<String[], Integer> map) {\n"
				+ "			for (String key[] : map.keySet()) {\n"
				+ "				Integer value = map.get(key);\n"
				+ "				useKeyAndValue(key, value);\n"
				+ "			}\n"
				+ "		}" +
				useKeyAndValueMethod;

		String expected = "" +
				"		void iterateMapKeyAndValue2(Map<String[], Integer> map) {\n"
				+ "			for (Map.Entry<String[], Integer> entry : map.entrySet()) {\n"
				+ "				String key[] = entry.getKey();\n"
				+ "				Integer value = entry.getValue();\n"
				+ "				useKeyAndValue(key, value);\n"
				+ "			}\n"
				+ "		}" +
				useKeyAndValueMethod;

		assertChange(original, expected);
	}

	@Test
	void visit_NoKeyVariableNecessary_shouldTransform() throws Exception {


		String original = "" +
				"	void iterateMap(Map<String, Integer> map) {\n"
				+ "		for (String key : map.keySet()) {\n"
				+ "			Integer value = map.get(key);\n"
				+ "			useOnlyValue(value);\n"
				+ "		}\n"
				+ "	}\n"
				+ "	\n"
				+ "	static <V> void useOnlyValue(V value) {\n"
				+ "	}";

		String expected = "" +
				"	void iterateMap(Map<String, Integer> map) {\n"
				+ "		for (Map.Entry<String, Integer> entry : map.entrySet()) {\n"
				+ "			Integer value = entry.getValue();\n"
				+ "			useOnlyValue(value);\n"
				+ "		}\n"
				+ "	}\n"
				+ "	\n"
				+ "	static <V> void useOnlyValue(V value) {\n"
				+ "	}";

		assertChange(original, expected);
	}

	// @Test
	// void visit__shouldTransform() throws Exception {
	// String original = "";
	// String expected = "";
	// assertChange(original, expected);
	// }
}

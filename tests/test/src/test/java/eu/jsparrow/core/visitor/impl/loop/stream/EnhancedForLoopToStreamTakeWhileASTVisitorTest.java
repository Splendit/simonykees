package eu.jsparrow.core.visitor.impl.loop.stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamTakeWhileASTVisitor;

@SuppressWarnings("nls")
public class EnhancedForLoopToStreamTakeWhileASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		visitor = new EnhancedForLoopToStreamTakeWhileASTVisitor();
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
	}

	@Test
	public void visit_baseCase_shouldTransform() throws Exception {
		String original = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	for(String string : strings) {\n" +
				"		if(string.isEmpty()) { \n" +
				"			break;\n" +
				"		}\n" +
				"		string.length();\n" +
				"	}";
		String expected = "" +
				"	List<String> strings=new ArrayList<>();\n" +
				"	strings.stream().takeWhile(string -> !string.isEmpty()).forEach(string -> {string.length();});";

		assertChange(original, expected);
	}

	@Test
	public void visit_singleThenStatement_shouldTransform() throws Exception {
		String original = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	for(String string : strings) {\n" +
				"		if(string.isEmpty())  \n" +
				"			break;\n" +
				"		string.length();\n" +
				"	}";
		String expected = "" +
				"	List<String> strings=new ArrayList<>();\n" +
				"	strings.stream().takeWhile(string -> !string.isEmpty()).forEach(string -> {string.length();});";

		assertChange(original, expected);
	}

	@Test
	public void visit_negatingCondition_shouldTransform() throws Exception {
		String original = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	for(String string : strings) {\n" +
				"		if(!string.isEmpty()) { \n" +
				"			break;\n" +
				"		}\n" +
				"		string.length();\n" +
				"	}";
		String expected = "" +
				"	List<String> strings=new ArrayList<>();\n" +
				"	strings.stream().takeWhile(string -> string.isEmpty()).forEach(string -> {string.length();});";

		assertChange(original, expected);
	}

	@Test
	public void visit_multipleRemainingStatements_shouldTransform() throws Exception {
		String original = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	for(String string : strings) {\n" +
				"		if(!string.isEmpty()) { \n" +
				"			break;\n" +
				"		}\n" +
				"		string.length();\n" +
				"		value.length();\n" +
				"		value.chars();" +
				"	}";
		String expected = "" +
				"	List<String> strings=new ArrayList<>();\n" +
				"	strings.stream().takeWhile(string -> string.isEmpty()).forEach(string -> {string.length();value.length();value.chars();});";

		assertChange(original, expected);
	}

	@Test
	public void visit_iteratingOverMap_shouldTransform() throws Exception {
		String original = "" +
				"		Map<String, String> map = new HashMap<>();\n" +
				"		Set<Entry<String, String>> entrySet = map.entrySet();\n" +
				"		for(Map.Entry<String, String> entry : entrySet) {\n" +
				"			if(!entry.getKey().isEmpty()) {\n" +
				"				break;\n" +
				"			}\n" +
				"			System.out.println(entry.getValue());\n" +
				"		}";
		String expected = "" +
				"	Map<String, String> map = new HashMap<>();\n" +
				"	Set<Entry<String, String>> entrySet = map.entrySet();\n" +
				"	entrySet.stream()\n" +
				"		.takeWhile(entry -> entry.getKey().isEmpty())\n" +
				"		.forEach(entry -> {System.out.println(entry.getValue());});";

		fixture.addImport("java.util.Map");
		fixture.addImport("java.util.HashMap");
		fixture.addImport("java.util.Set");
		fixture.addImport("java.util.Map.Entry");

		assertChange(original, expected);
	}

	/*
	 * Negative test cases
	 */

	@Test
	public void visit_nonFinalVariables_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> values = new ArrayList<>();" +
				"		int i = 0;\n" +
				"		for(String value : values) {\n" +
				"			if(value.isEmpty()) {\n" +
				"				break;\n" +
				"			}\n" +
				"			i++;\n" +
				"			System.out.println(value);\n" +
				"		}");
	}

	@Test
	public void visit_multipleStatementsInIfBody_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> values = new ArrayList<>();" +
				"		for(String value : values) {\n" +
				"			if(value.isEmpty()) {\n" +
				"				value.length();\n" +
				"				break;\n" +
				"			}\n" +
				"			System.out.println(value);\n" +
				"		}");
	}

	@Test
	public void visit_ifThenElseStatement_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> values = new ArrayList<>();" +
				"		for(String value : values) {\n" +
				"			if(value.isEmpty()) {\n" +
				"				break;\n" +
				"			} else {\n" +
				"				value.length();\n" +
				"			}\n" +
				"			System.out.println(value);\n" +
				"		}");
	}

	@Test
	public void visit_missingIfStatement_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> values = new ArrayList<>();" +
				"		for(String value : values) {\n" +
				"			values.add(\"\");" +
				"			System.out.println(value);\n" +
				"		}");
	}

	@Test
	public void visit_missingBreakStatement_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> values = new ArrayList<>();" +
				"		for(String value : values) {\n" +
				"			if(value.isEmpty()) {\n" +
				"				value.length();\n" +
				"			}\n" +
				"			System.out.println(value);\n" +
				"		}");
	}

	@Test
	public void visit_singleLoopBodyStatement_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> values = new ArrayList<>();" +
				"		for(String value : values) \n" +
				"			if(value.isEmpty()) {\n" +
				"				value.length();\n" +
				"			}\n" +
				"		System.out.println(values);\n");
	}

	@Test
	public void visit_havingReturnStatement_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> values = new ArrayList<>();" +
				"		for(String value : values) {\n" +
				"			if(value.isEmpty()) {\n" +
				"				return;\n" + // <--
				"			}\n" +
				"			System.out.println(value);\n" +
				"		}");
	}

	@Test
	public void visit_havingBreakAndReturnStatement_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> values = new ArrayList<>();" +
				"		for(String value : values) {\n" +
				"			if(value.isEmpty()) {\n" +
				"				break;\n" + // <--
				"			}\n" +
				"			System.out.println(value);\n" +
				"			return;\n" + // <--
				"		}");
	}

	@Test
	public void visit_havingThrowStatement_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> values = new ArrayList<>();" +
				"		for(String value : values) {\n" +
				"			if(value.isEmpty()) {\n" +
				"				break;\n" +
				"			}\n" +
				"			System.out.println(value);\n" +
				"			throw new RuntimeException();\n" + // <--
				"		}");
	}

	@Test
	public void visit_havingContinueStatement_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> values = new ArrayList<>();" +
				"		for(String value : values) {\n" +
				"			if(value.isEmpty()) {\n" +
				"				break;\n" +
				"			}\n" +
				"			if(value.length() > 1) {\n" +
				"				continue;\n" + // <--
				"			}\n" +
				"			System.out.println(value);\n" +
				"		}");
	}

	@Test
	public void visit_emptyRemainingBody_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> values = new ArrayList<>();" +
				"		for(String value : values) {\n" +
				"			if(value.isEmpty()) {\n" +
				"				break;\n" +
				"			}\n" +
				"		}");
	}

	@Test
	public void visit_iteratingOverArrays_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		String[] values = {};" +
				"		for(String value : values) {\n" +
				"			if(value.isEmpty()) {\n" +
				"				break;\n" +
				"			}\n" +
				"			System.out.println(value);\n" +
				"		}");
	}
}

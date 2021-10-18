package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

public class UseArraysStreamASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new UseArraysStreamASTVisitor());
	}

	@Test
	public void visit_stringArrayAsParameter_shouldTransform() throws Exception {
		fixture.addImport("java.util.Arrays");
		String original = "" +
				"String[] values = new String[] {\"1\", \"2\", \"3\"};\n" +
				"Arrays.asList(values).stream();";
		String expected = "" +
				"String[] values = new String[] {\"1\", \"2\", \"3\"};\n" +
				"Stream.of(values);";
		assertChange(original, expected);
	}

	@Test
	public void visit_varArgsAsParameter_shouldTransform() throws Exception {
		fixture.addImport("java.util.Arrays");
		String original = "" +
				"Arrays.asList(\"1\", \"2\", \"3\").stream();";
		String expected = "" +
				"Stream.of(\"1\", \"2\", \"3\");";
		assertChange(original, expected);
	}

	@Test
	public void visit_intVarArgs_shouldTransform() throws Exception {
		fixture.addImport("java.util.Arrays");
		String original = "" +
				"Arrays.asList(1, 2, 3).stream();";
		String expected = "" +
				"Arrays.stream(new int[] {1, 2, 3});";
		assertChange(original, expected);
	}

	@Test
	public void visit_incompatibleStreamChain_shouldNotTransform() throws Exception {
		fixture.addImport("java.util.Arrays");
		String original = "" +
				"Arrays.asList(1, 2, 3).stream()\n" +
				".filter(value -> value > 0)\n" +
				".filter(value -> {\n" +
				"	System.out.println(value);\n" +
				"	return true;\n" +
				"})\n" +
				".mapToInt(i -> i);";
		assertNoChange(original);
	}

	@Test
	public void visit_invokeMethodsInBoxedType_shouldNotTransform() throws Exception {
		fixture.addImport("java.util.Arrays");
		String original = "" +
				"		Arrays.asList(1, 2, 3).stream()\n" +
				"			.map(value -> value.toString())\n" +
				"			.forEach(val -> {});";
		assertNoChange(original);
	}

	@Test
	public void visit_usingCollect_shouldNotTransform() throws Exception {
		fixture.addImport("java.util.Arrays");
		fixture.addImport("java.util.stream.Collectors");
		String original = "" +
				"Arrays.asList(1, 2, 3).stream()\n" +
				"	.collect(Collectors.toList());";
		assertNoChange(original);
	}

	@Test
	public void visit_usingExplicitParameterType_shouldNotTransform() throws Exception {
		fixture.addImport("java.util.Arrays");
		String original = "" +
				"Arrays.asList(1, 2, 3).stream()\n" +
				"	.filter((Integer value) -> value > 0)\n" +
				"	.forEach(i -> {});";
		assertNoChange(original);
	}

	@Test
	public void visit_usingMethodReferences_shouldNotTransform() throws Exception {
		fixture.addImport("java.util.Arrays");
		String original = "" +
				"Integer expected = 3;\n" +
				"Arrays.asList(1, 2, 3).stream()\n" +
				"	.filter(expected::equals)\n" +
				"	.forEach(val -> {});";
		assertNoChange(original);
	}

	@Test
	public void visit_missingStream_shouldNotTransform() throws Exception {
		fixture.addImport("java.util.Arrays");
		String original = "" +
				"Arrays.asList(1, 2, 3);";
		assertNoChange(original);
	}

	@Test
	public void visit_missingArgumentsInAsList_shouldNotTransform() throws Exception {
		fixture.addImport("java.util.Arrays");
		String original = "" +
				"Arrays.asList().stream().forEach(val -> {});";
		assertNoChange(original);
	}

	@Test
	public void visit_staticImportToAsList_shouldTransform() throws Exception {
		fixture.addImport("java.util.Arrays.asList", true, false);
		String original = "" +
				"asList(1, 2, 3).stream().forEach(val -> {});";
		String expected = "" +
				"Arrays.stream(new int[] {1, 2, 3}).forEach(val -> {});";
		assertChange(original, expected);
	}

	@Test
	public void visit_ArraysCannotBeImported_shouldTransform() throws Exception {
		fixture.addImport("java.util.Arrays", true, true);
		String original = "" +
				"class Arrays {}\n" +
				"asList(1, 2, 3).stream().forEach(val -> {});";
		String expected = "" +
				"class Arrays {}\n" +
				"java.util.Arrays.stream(new int[] {1, 2, 3}).forEach(val -> {});";
		assertChange(original, expected);
	}
	
	@Test
	public void visit_StreamCannotBeImported_shouldTransform() throws Exception {
		fixture.addImport("java.util.Arrays");
		String original = "" +
				"		class Stream {}\n" + 
				"		Arrays.asList(\"1\", \"2\", \"3\").stream();";
		String expected = "" +
				"		class Stream {}\n" + 
				"		java.util.stream.Stream.of(\"1\", \"2\", \"3\");";
		assertChange(original, expected);	
	}

	@Test
	public void visit_compatibleStreamChain_shouldTransform() throws Exception {
		fixture.addImport("java.util.Arrays");
		String original = "" +
				"Arrays.asList(1, 2, 3).stream()\n" +
				"	.filter(value -> value > 0)\n" +
				"	.filter(value -> value < 1)\n" +
				"	.forEach(val -> {});";
		String expected = "" +
				"Arrays.stream(new int[] {1, 2, 3})\n" +
				"	.filter(value -> value > 0)\n" +
				"	.filter(value -> value < 1)\n" +
				"	.forEach(val -> {});";
		assertChange(original, expected);
	}

}

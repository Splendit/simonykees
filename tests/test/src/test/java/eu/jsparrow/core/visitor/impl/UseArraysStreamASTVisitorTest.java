package eu.jsparrow.core.visitor.impl;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UseArraysStreamASTVisitorTest extends UsesSimpleJDTUnitFixture {
	
	
	@BeforeEach
	public void setUp() {
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
	
	
	private void sampleCode() {
		Arrays.asList("1", "2", "3").stream();
		String[] values = new String[] {"1", "2", "3"};
		Arrays.stream(values);
	}
	
	private void sampleCode2() {
		Arrays.asList("1", "2", "3").stream();
		Arrays.stream(new String[] {"1", "2", "3"});
	}
	
	private void sampleCodeWithSpezialedStream() {
		Arrays.asList(1, 2, 3).stream();
		Arrays.stream(new int[] {1, 2, 3});
	}
	
	private void sampleCodeCompatibleStreamChain() {
		Arrays.asList(1, 2, 3).stream()
		.filter(value -> value > 0)
		.filter(value -> {
			System.out.println(value);
			return true;
		})
		.mapToInt(i -> i);
		
Arrays.asList(1, 2, 3).stream()
	.filter(value -> value > 0)
	.filter(value -> value < 1)
	.forEach(val -> {});
	}
	
	private void invoceMethodInBoxedType() {
		Arrays.asList(1, 2, 3).stream()
			.map((Integer value) -> value + 3)
			.forEach(val -> {});
		
		Arrays.stream(new int[] {1, 2, 3})
		.map(value -> value + 3)
		.forEach(val -> {});
	}

}

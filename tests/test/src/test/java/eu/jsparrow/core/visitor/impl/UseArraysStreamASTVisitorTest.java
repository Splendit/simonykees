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
				"Arrays.stream(values);";
		assertChange(original, expected);
	}
	
	
	@Test
	public void visit_varArgsAsParameter_shouldTransform() throws Exception {
		fixture.addImport("java.util.Arrays");
		String original = "" +
				"Arrays.asList(\"1\", \"2\", \"3\").stream();";
		String expected = "" +
				"Arrays.stream(new String[] {\"1\", \"2\", \"3\"});";
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

}

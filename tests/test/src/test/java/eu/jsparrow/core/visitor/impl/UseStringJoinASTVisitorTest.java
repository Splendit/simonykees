package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
public class UseStringJoinASTVisitorTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	public void setUp() throws Exception {
		visitor = new UseStringJoinASTVisitor();
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		fixture.addImport(java.util.stream.Collectors.class.getName());
	}
	
	@Test
	public void visit_usingDelimiter_shouldTransform() throws Exception {
		String original = "" +
				"List<String> values = new ArrayList<>();\n" + 
				"values.stream().collect(Collectors.joining(\",\"));";
		String expected = "" +
				"List<String> values = new ArrayList<>();\n" + 
				"String.join(\",\", values);";
		assertChange(original, expected);
	}
	
	@Test
	public void visit_staticImport_shouldTransform() throws Exception {
		String original = "" +
				"List<String> values = new ArrayList<>();\n" + 
				"values.stream().collect(joining(\",\"));";
		String expected = "" +
				"List<String> values = new ArrayList<>();\n" + 
				"String.join(\",\", values);";
		fixture.addImport(java.util.stream.Collectors.class.getName() + ".joining", true, false);
		assertChange(original, expected);
	}
	
	@Test
	public void visit_noDelimiter_shouldTransform() throws Exception {
		String original = "" +
				"List<String> values = new ArrayList<>();\n" + 
				"values.stream().collect(Collectors.joining());";
		String expected = "" +
				"List<String> values = new ArrayList<>();\n" + 
				"String.join(\"\", values);";
		assertChange(original, expected);
	}
	
	@Test
	public void visit_multipleJoiningParameters_shouldNotTransform() throws Exception {
		String original = "" +
				"List<String> values = new ArrayList<>();\n" + 
				"values.stream().collect(Collectors.joining(\",\", \"pre\", \"suf\"));";
		assertNoChange(original);
	}
	
	@Test
	public void visit_streamOfIntegers_shouldNotTransform() throws Exception {
		String original = "" +
				"List<Integer> integers = new ArrayList<>();\n" + 
				"integers.stream().map(i -> Integer.toString(i)).collect(Collectors.joining(\",\"));";
		assertNoChange(original);
	}
	
	@Test
	public void visit_missingCollect_shouldNotTransform() throws Exception {
		String original = "" +
				"Collectors.joining(\",\");";
		assertNoChange(original);
	}
	
	@Test
	public void visit_missingStreamInvocation_shouldNotTransform() throws Exception {
		String original = "" +
				"List<String> values = new ArrayList<>();\n" + 
				"Stream<String> stream = values.stream();\n" + 
				"stream.collect(Collectors.joining());";
		fixture.addImport(java.util.stream.Stream.class.getName());
		assertNoChange(original);
	}
	
	@Test
	public void visit_usingLocalCollectInvocation_shouldNotTransform() throws Exception {
		String original = "" +
				"abstract class InnerClass {\n" + 
				"	public void useLocalCollect() {\n" + 
				"		collect(Collectors.joining());\n" + 
				"	}\n" + 
				"	public abstract void collect(Collector<CharSequence, ?, String> collector);\n" + 
				"}";
		fixture.addImport(java.util.stream.Collector.class.getName());
		assertNoChange(original);
	}
	
	@Test
	public void visit_nonMatchingCollectName_shouldNotTransform() throws Exception {
		String original = "" +
				"abstract class InnerClass {\n" + 
				"	public void useLocalCollect() {\n" + 
				"		collect2(Collectors.joining());\n" + 
				"	}\n" + 
				"	public abstract void collect2(Collector<CharSequence, ?, String> collector);\n" + 
				"}";
		fixture.addImport(java.util.stream.Collector.class.getName());
		assertNoChange(original);
	}
	
	@Test
	public void visit_usingLocalJoining_shouldNotTransform() throws Exception {
		String original = "" +
				"abstract class LocalClass {\n" + 
				"	public void useLocalJoining() {\n" + 
				"		stream().collect(joining());\n" + 
				"	}\n" + 
				"	public abstract LocalClass stream();\n" + 
				"	public abstract Collector<CharSequence, ?, String> joining();\n" + 
				"	public abstract void collect(Collector<CharSequence, ?, String> collector);\n" + 
				"}";
		fixture.addImport(java.util.stream.Collector.class.getName());
		assertNoChange(original);
	}
	
	@Test
	public void visit_usingLocalStream_shouldNotTransform() throws Exception {
		String original = "" +
				"abstract class LocalStream {\n" + 
				"	public void useLocalCollect() {\n" + 
				"		createStream().stream().collect(Collectors.joining());\n" + 
				"	}\n" + 
				"	\n" + 
				"	public abstract Stream<String> stream();\n" + 
				"	public abstract LocalStream createStream();\n" + 
				"}";
		fixture.addImport(java.util.stream.Stream.class.getName());
		assertNoChange(original);
	}
	
	@Test
	public void visit_missingStreamExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"abstract class LocalStream extends ArrayList<String> {\n" + 
				"	public void useLocalCollect() {\n" + 
				"		stream().collect(Collectors.joining());\n" + 
				"	}\n" + 
				"}";
		assertNoChange(original);	}
	
	@Test
	public void visit_missingCollectExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"abstract class LocalStream implements Stream<String> {\n" + 
				"	public void useLocalCollect() {\n" + 
				"		collect(Collectors.joining());\n" + 
				"	}\n" + 
				"}";
		fixture.addImport(java.util.stream.Stream.class.getName());
		assertNoChange(original);
	}

}

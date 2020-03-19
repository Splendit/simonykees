package eu.jsparrow.core.visitor.impl.loop.stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamAnyMatchASTVisitor;

/**
 * Visitor tests for {@link EnhancedForLoopToStreamAnyMatchASTVisitor}.
 * 
 * @since 3.3.0
 *
 */
@SuppressWarnings("nls")
public class EnhancedForLoopToStreamAnyMatchASTVisitorTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	public void setUp() {
		setVisitor(new EnhancedForLoopToStreamAnyMatchASTVisitor());
	}
	
	/*
	 * Tests with allMatch
	 */
	
	@Test
	public void visit_loopWithBreakToAllMatch_shouldTransform() throws Exception {
		
		String original = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	boolean allEmpty = true;\n" +
				"	for(String string : strings) {\n" + 
				"		if(!string.isEmpty()) { \n" +
				"			allEmpty = false;\n" +
				"			break;\n" +
				"		}\n" +
				"	}";
		String expected = "" +
				"	List<String> strings=new ArrayList<>();\n" + 
				"	boolean allEmpty=strings.stream().allMatch(string -> string.isEmpty());";
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_loopWithReturnToAllMatch_shouldTranform() throws Exception {
		
		String original = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	for(String string : strings) {\n" + 
				"		if(!string.isEmpty()) {\n" + 
				"			return false;\n" + 
				"		}\n" + 
				"	}\n" + 
				"	return true;";
		String expected = "" +
				"	List<String> strings=new ArrayList<>();\n" + 
				"	return strings.stream().allMatch(string -> string.isEmpty());";
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		
		assertChange(original, expected);
	}
	
	/*
	 * Tests with anyMatch
	 */
	
	@Test
	public void visit_loopWithBreakToAnyMatch_shouldTranform() throws Exception {
		
		String original = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	boolean anyEmpty = false;\n" +
				"	for(String string : strings) {\n" + 
				"		if(string.isEmpty()) { \n" +
				"			anyEmpty = true;\n" +
				"			break;\n" +
				"		}\n" +
				"	}";
		String expected = "" +
				"	List<String> strings=new ArrayList<>();\n" + 
				"	boolean anyEmpty=strings.stream().anyMatch(string -> string.isEmpty());";
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_loopWithReturnToAnyMatch_shouldTranform() throws Exception {
		
		String original = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	for(String string : strings) {\n" + 
				"		if(string.isEmpty()) {\n" + 
				"			return true;\n" + 
				"		}\n" + 
				"	}\n" + 
				"	return false;";
		String expected = "" +
				"	List<String> strings=new ArrayList<>();\n" + 
				"	return strings.stream().anyMatch(string -> string.isEmpty());";
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		
		assertChange(original, expected);
	}
	
	
	/*
	 * Tests with noneMatch
	 */
	
	@Test
	public void visit_loopWithBreakToNoneMatch_shouldTranform() throws Exception {
		
		String original = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	boolean allNonEmpty = true;\n" +
				"	for(String string : strings) {\n" + 
				"		if(string.isEmpty()) { \n" +
				"			allNonEmpty = false;\n" +
				"			break;\n" +
				"		}\n" +
				"	}";
		String expected = "" +
				"	List<String> strings=new ArrayList<>();\n" + 
				"	boolean allNonEmpty=strings.stream().noneMatch(string -> string.isEmpty());";
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_loopWithRetunrToNoneMatch_shouldTranform() throws Exception {
		
		String original = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	for(String string : strings) {\n" + 
				"		if(string.isEmpty()) {\n" + 
				"			return false;\n" + 
				"		}\n" + 
				"	}\n" + 
				"	return true;";
		String expected = "" +
				"	List<String> strings=new ArrayList<>();\n" + 
				"	return strings.stream().noneMatch(string -> string.isEmpty());";
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		
		assertChange(original, expected);
	}
}

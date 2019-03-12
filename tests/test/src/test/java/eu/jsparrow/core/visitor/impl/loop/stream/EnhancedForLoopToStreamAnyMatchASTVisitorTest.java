package eu.jsparrow.core.visitor.impl.loop.stream;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamAnyMatchASTVisitor;

@SuppressWarnings("nls")
public class EnhancedForLoopToStreamAnyMatchASTVisitorTest extends UsesJDTUnitFixture {
	
	private EnhancedForLoopToStreamAnyMatchASTVisitor visitor;
	
	@BeforeEach
	public void setUp() {
		visitor = new EnhancedForLoopToStreamAnyMatchASTVisitor();
	}
	
	@Test
	public void visit_loopWithBreakToAllMatch_shouldNotTranform() throws Exception {
		
		String block = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	boolean allEmpty = true;\n" +
				"	for(String string : strings) {\n" + 
				"		if(!string.isEmpty()) { \n" +
				"			allEmpty = false;\n" +
				"			break;\n" +
				"		}\n" +
				"	}";
		String expectedContent = "" +
				"	List<String> strings=new ArrayList<>();\n" + 
				"	boolean allEmpty=strings.stream().allMatch(string -> string.isEmpty());";
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedContent);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_loopWithBreakToNoneMatch_shouldNotTranform() throws Exception {
		
		String block = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	boolean allNonEmpty = true;\n" +
				"	for(String string : strings) {\n" + 
				"		if(string.isEmpty()) { \n" +
				"			allNonEmpty = false;\n" +
				"			break;\n" +
				"		}\n" +
				"	}";
		String expectedContent = "" +
				"	List<String> strings=new ArrayList<>();\n" + 
				"	boolean allNonEmpty=strings.stream().noneMatch(string -> string.isEmpty());";
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedContent);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_loopWithReturnToAllMatch_shouldNotTranform() throws Exception {
		
		String block = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	for(String string : strings) {\n" + 
				"		if(!string.isEmpty()) {\n" + 
				"			return false;\n" + 
				"		}\n" + 
				"	}\n" + 
				"	return true;";
		String expectedContent = "" +
				"	List<String> strings=new ArrayList<>();\n" + 
				"	return strings.stream().allMatch(string -> string.isEmpty());";
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedContent);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_loopWithRetunrToNoneMatch_shouldNotTranform() throws Exception {
		
		String block = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	for(String string : strings) {\n" + 
				"		if(string.isEmpty()) {\n" + 
				"			return false;\n" + 
				"		}\n" + 
				"	}\n" + 
				"	return true;";
		String expectedContent = "" +
				"	List<String> strings=new ArrayList<>();\n" + 
				"	return strings.stream().noneMatch(string -> string.isEmpty());";
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedContent);
		assertMatch(expected, fixture.getMethodBlock());
	}

}

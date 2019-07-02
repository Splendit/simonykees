package eu.jsparrow.core.visitor.impl.loop.stream;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamTakeWhileASTVisitor;

public class EnhancedForLoopToStreamTakeWhileASTVisitorTest extends UsesJDTUnitFixture {
	
	private EnhancedForLoopToStreamTakeWhileASTVisitor visitor;
	
	@BeforeEach
	public void setUp() {
		visitor = new EnhancedForLoopToStreamTakeWhileASTVisitor();
	}
	
	@Test
	public void test() throws Exception {
		String block = "" +
				"	List<String> strings = new ArrayList<>();\n" +
				"	for(String string : strings) {\n" + 
				"		if(string.isEmpty()) { \n" +
				"			break;\n" +
				"		}\n" +
				"		string.length();\n" +
				"	}";
		String expectedContent = "" +
				"	List<String> strings=new ArrayList<>();\n" + 
				"	strings.stream().takeWhile(string -> !string.isEmpty()).forEach(string -> {string.length();});";
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedContent);
		assertMatch(expected, fixture.getMethodBlock());
	}
}

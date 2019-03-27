package eu.jsparrow.core.visitor.impl.lambdaforeach;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachMapASTVisitor;

@SuppressWarnings("nls")
public class LambdaForEachMapASTVisitorTest extends UsesJDTUnitFixture {
	
	private LambdaForEachMapASTVisitor visitor;
	
	@BeforeEach
	public void setUp() {
		visitor = new LambdaForEachMapASTVisitor();
	}
	
	@Test
	public void visit_rawStream_shouldNotReplace() throws Exception  {
		
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		
		String block = "" +
				"		List strings = new ArrayList<String>();\n" + 
				"		strings.forEach(value -> {\n" + 
				"			String subValue = value.toString();\n" + 
				"			int length = subValue.length();\n" + 
				"			if(length > 0) {\n" + 
				"				\n" + 
				"			}\n" + 
				"		});";
		
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_typedStream_shouldReplace() throws Exception  {
		
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		
		String block = "" +
				"	List<String> strings = new ArrayList<String>();\n" + 
				"	strings.forEach(value -> {\n" + 
				"		String subValue = value.toString();\n" + 
				"		int length = subValue.length();\n" + 
				"		if(length > 0) {\n" + 
				"			\n" + 
				"		}\n" + 
				"	});";
		
		String expectedBlock = "" +
				"List<String> strings=new ArrayList<String>();\n" + 
				"strings.stream()" +
				"	.map(value -> value.toString())" +
				"	.forEach(subValue -> {\n" + 
				"		int length=subValue.length();\n" + 
				"		if (length > 0) {\n" + 
				"			\n" + 
				" 		}\n" + 
				"	});";
		
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedBlock);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	

}

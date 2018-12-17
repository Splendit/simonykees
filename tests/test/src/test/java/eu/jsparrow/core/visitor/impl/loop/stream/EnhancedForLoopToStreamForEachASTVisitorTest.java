package eu.jsparrow.core.visitor.impl.loop.stream;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamForEachASTVisitor;

@SuppressWarnings("nls")
public class EnhancedForLoopToStreamForEachASTVisitorTest extends UsesJDTUnitFixture {
	
	private EnhancedForLoopToStreamForEachASTVisitor visitor;
	
	@Before
	public void setUp() {
		visitor = new EnhancedForLoopToStreamForEachASTVisitor();
	}
	
	@Test
	public void visit_rawCollection_shouldNotTranform() throws Exception {
		
		String block = "" +
				"		List strings = new ArrayList<String>();\n" + 
				"		for(Object string : strings) {\n" + 
				"			int hashCode = string.hashCode();\n" + 
				"		}";
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_typedCollection_shouldTranform() throws Exception {
		
		String block = "" +
				"		List<String> strings = new ArrayList<String>();\n" + 
				"		for(Object string : strings) {\n" + 
				"			int hashCode = string.hashCode();\n" + 
				"		}";
		String expectedBlock = "" + 
				"		List<String> strings = new ArrayList<String>();\n" + 
				"		strings.forEach(string -> {\n" + 
				"			int hashCode = string.hashCode();\n" + 
				"		});";

		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedBlock);
		assertMatch(expected, fixture.getMethodBlock());
		
	}

}

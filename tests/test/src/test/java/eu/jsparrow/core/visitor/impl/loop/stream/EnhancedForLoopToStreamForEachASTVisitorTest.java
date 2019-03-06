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
	public void visit_conditionalLoopExpression_shouldNotTranform() throws Exception {
		String block = "" +
				"		List<String> strings = new ArrayList<String>();\n" + 
				"		List<String> strings2 = new ArrayList<>();\n" + 
				"		Object object = new Object();\n" + 
				"		for(Object string : object == null ? strings : strings2) {\n" + 
				"			int hashCode = string.hashCode(); \n" + 
				"			}";
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
	
	@Test
	public void modifierers_should_not_be_lost() throws Exception {
		
		String block = "" +
				"		List<String> strings = new ArrayList<String>();\n" + 
				"		for(final Object string : strings) {\n" + 
				"			int hashCode = string.hashCode();\n" + 
				"		}";
		String expectedBlock = "" + 
				"		List<String> strings = new ArrayList<String>();\n" + 
				"		strings.forEach((final Object string) -> {\n" + 
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
	
	@Test
	public void annotations_should_not_be_lost() throws Exception {
		String block = "" +
				"		List<String> strings = new ArrayList<String>();\n" + 
				"		for(@InitParam(name = \"\", value = \"\") Object string : strings) {\n" + 
				"			int hashCode = string.hashCode();\n" + 
				"		}";
		String expectedBlock = "" + 
				"		List<String> strings = new ArrayList<String>();\n" + 
				"		strings.forEach((@InitParam(name = \"\", value = \"\")  Object string) -> {\n" + 
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

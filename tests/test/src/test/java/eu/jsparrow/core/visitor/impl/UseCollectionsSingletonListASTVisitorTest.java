package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
public class UseCollectionsSingletonListASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private UseCollectionsSingletonListASTVisitor visitor;
	
	@BeforeEach
	public void setUp() throws Exception {
		visitor = new UseCollectionsSingletonListASTVisitor();
		fixture.addImport(java.util.Arrays.class.getName());
	}
	
	@Test
	public void visit_asListZeroArguments_shouldTransform() throws Exception {
		String original = "List<String> strings = Arrays.asList();";
		String expected = "List<String> strings = Collections.emptyList();";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_asListOneArgument_shouldTransform() throws Exception {
		String original = "List<String> strings = Arrays.asList(\"value\");";
		String expected = "List<String> strings = Collections.singletonList(\"value\");";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_asListStaticImport_shouldTransform() throws Exception {
		String original = "List<String> strings = asList(\"value\");";
		String expected = "List<String> strings = Collections.singletonList(\"value\");";
		fixture.addImport(java.util.Arrays.class.getName() + ".asList", true, false);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	
	@Test
	public void visit_asListEmptyListStaticImport_shouldTransform() throws Exception {
		String original = "List<String> strings = asList();";
		String expected = "List<String> strings = Collections.emptyList();";
		fixture.addImport(java.util.Arrays.class.getName() + ".asList", true, false);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_asListMoreThanOneArguments_shouldNotTransform() throws Exception {
		String original = "List<String> strings = Arrays.asList(\"value1\", \"value2\");";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	

	
	@Test
	public void visit_usingArrayAsArgument_shouldNotTransform() throws Exception {
		String original = "String[]array = {}; List<String> strings = Arrays.asList(array);";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
}

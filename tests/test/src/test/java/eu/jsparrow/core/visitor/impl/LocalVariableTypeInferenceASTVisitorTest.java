package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.jsparrow.rules.java10.LocalVariableTypeInferenceASTVisitor;

@SuppressWarnings("nls")
public class LocalVariableTypeInferenceASTVisitorTest extends UsesJDTUnitFixture {

	private LocalVariableTypeInferenceASTVisitor visitor;
	
	@Before
	public void setUp() {
		visitor = new LocalVariableTypeInferenceASTVisitor();
	}
	
	@Test
	public void visit_simpleTypeInitialization_shouldReplace() throws Exception {
		fixture.addImport("java.util.Date");
		String block = "Date date = new Date();";
		String varDeclaration = "var date = new Date();";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(varDeclaration);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_enhancedForLoopDeclaration_shouldReplace() throws Exception {
		String block = "for(String string : Arrays.asList(\"1\", \"2\", \"3\")) {}";
		String varDeclaration = "for(var string : Arrays.asList(\"1\", \"2\", \"3\")) {}";

		fixture.addImport("java.util.Arrays");
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(varDeclaration);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_varInEnhancedForLoop_shouldNotReplace() throws Exception {
		String block = "for(var string : Arrays.asList(\"1\", \"2\", \"3\")) {}";

		fixture.addImport("java.util.Arrays");
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_arrayInEnhancedForLoop_shouldReplace() throws Exception {
		String block = "String [] strings; strings = new String [] {\"\", \"\"}; for(String string : strings) {}";
		String expectedBlockContent = "String [] strings; strings = new String [] {\"\", \"\"}; for(var string : strings) {}";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(expectedBlockContent);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_rawTypes_shouldReplace() throws Exception {
		fixture.addImport("java.util.HashMap");
		String block = "HashMap map = new HashMap();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);
		
		fixture.hasChanged();

		Block expected = createBlock("var map = new HashMap();");
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_rawInitializer_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.HashMap");
		String block = "HashMap<String, String> map = new HashMap();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);
		
		fixture.hasChanged();

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_rawDeclarationType_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.HashMap");
		String block = "HashMap map = new HashMap<String, String>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);
		
		fixture.hasChanged();

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_primitiveType_shouldNotReplace() throws Exception {
		String block = "int i = 0;";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_alreadyVar_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Date");
		String block = "var map = new Date();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_diamondInInitialization_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.HashMap");
		String block = "HashMap<String, String> map = new HashMap<>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_wildcardInInitialization_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.HashMap");
		String block = "HashMap<Object, Object> map = new HashMap<?, ?>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_multipleDeclarationFragments_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.HashMap");
		String block = "HashMap<String, String> map1 = new HashMap<String, String>(), map2 = new HashMap<String, String>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test 
	public void visit_subTypeInitialization_shouldReplace() throws Exception {
		fixture.addImport("java.util.HashMap");
		fixture.addImport("java.util.Map");
		String block = "Map<String, String> map = new HashMap<String, String>();";
		String expectedContent = "var map = new HashMap<String, String>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(expectedContent);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test 
	public void visit_incompatibleSiblingReInitialization_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.ArrayList");
		fixture.addImport("java.util.LinkedList");
		fixture.addImport("java.util.List");
		String block = "int anotherVariable = 0; List<String> list = new ArrayList<String>(); list = new LinkedList<>(); anotherVariable = 1;";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_initializationWithWildcard_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.HashMap");
		fixture.addImport("java.util.Map");
		String block = "Map<Object, Object> map = new HashMap<?,?>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_simpleTypeWithoutInitialization_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Date");
		String block = "Date date; date = new Date();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_singleVariableDeclarationInCatchBlock_shouldNotReplace() throws Exception {
		fixture.addImport("java.io.File");
		String block = "try {new File(\"file.name\");} catch(NullPointerException e) {}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_initializationWithLambda_shouldNotReplace() throws Exception {
		String block = "Runnable r = () -> {};";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_singleDeclarationInLambdaParameter_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Predicate");
		String block = "Predicate<String>  myPredicate = (String value) -> value.isEmpty();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Ignore
	@Test
	public void visit_missingSpaceBetweenTypeAndName_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		String block = "List<String>name = new ArrayList<String>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_arrayTypeWithDimensions_shouldTransform() throws Exception {
		String block = "String []names[] = new String[][] {};";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock("var names = new String[][]{};");
		assertMatch(expectedBlock, fixture.getMethodBlock());
		
	}
	
	@Test
	public void visit_arrayInitializer_shouldNotTransform() throws Exception {
		String block = "String []names[] = {{}};";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
		
	}
	
	@Test
	public void visit_2dArray_shouldTransform() throws Exception {
		String block = "String names[][] = {}; for(String t[] : names) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock("String names[][] = {}; for(var t : names) { }");
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
}

package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
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

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(varDeclaration);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_methodWithoutArguments_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Map");
		fixture.addImport("java.util.HashMap");
		String block = "Map map = new HashMap();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);
		
		fixture.hasChanged();

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_initizationHavingDiamond_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.HashMap");
		String block = "HashMap<String, String> map = new HashMap<>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_initizationWithSubtype_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.HashMap");
		fixture.addImport("java.util.Map");
		String block = "Map<String, String> map = new HashMap<String, String>();";
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
		String block = "Predicate<String> myPredicate; myPredicate = (String value) -> value.isEmpty();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
}

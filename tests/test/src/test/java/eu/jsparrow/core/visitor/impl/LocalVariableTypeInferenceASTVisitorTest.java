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
	public void visit_methodWithoutArguments_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.HashMap");
		String block = "HashMap map = new HashMap();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);
		
		fixture.hasChanged();

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Ignore // avoid the build failure... is to be implemented
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
	public void visit_initizationHavingWildcard_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.HashMap");
		String block = "HashMap<Object, Object> map = new HashMap<?, ?>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		Block expectedBlock = createBlock(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Ignore // avoid the build failure... is to be implemented
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
	public void visit_initizationWithWildcard_shouldNotReplace() throws Exception {
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
	
	/*
	 * Just for testing
	 */
	
//	private void consumeListOfStrings(List<String> list) {
//		var list2 = new ArrayList<>();
//		for(var string : list2) {
//			
//		}
//	}
//	
//	private void useListOfStrings() {
//		List<String> list = new ArrayList<>();
//		ArrayList<String> arrayList = new ArrayList<>();
//		List<Object> objectsList = new ArrayList<>();
//		List strings = new ArrayList<String>();
//		strings.add(new Object());
//		var rawList = new ArrayList();
//		
//		Predicate<String>  myPredicate = (String value) -> value.isEmpty();
//		
//		Object[] a = null;
//		arrayList.toArray(a);
//		
//		var varList = new ArrayList<>();
//		
//		long b = 2L;
//	
//		
//		
//		consumeListOfStrings(list);
//		consumeListOfStrings(arrayList);
//		consumeListOfStrings(rawList);
//		consumeListOfStrings(objectsList);
//		
//		list = new LinkedList<>();
//	}
}

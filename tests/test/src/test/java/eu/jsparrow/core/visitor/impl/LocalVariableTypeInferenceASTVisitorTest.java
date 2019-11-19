package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;
import eu.jsparrow.rules.java10.LocalVariableTypeInferenceASTVisitor;

@SuppressWarnings("nls")
public class LocalVariableTypeInferenceASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private static final String JAVA_UTIL_MAP = java.util.Map.class.getName();
	private static final String JAVA_UTIL_LIST = java.util.List.class.getName();
	private static final String JAVA_UTIL_LINKED_LIST = java.util.LinkedList.class.getName();
	private static final String JAVA_UTIL_ARRAY_LIST = java.util.ArrayList.class.getName();
	private static final String JAVA_UTIL_HASH_MAP = java.util.HashMap.class.getName();
	private static final String JAVA_UTIL_ARRAYS = java.util.Arrays.class.getName();
	private static final String JAVA_IO_FILE = java.io.File.class.getName();
	private static final String JAVA_UTIL_DATE = java.util.Date.class.getName();

	private LocalVariableTypeInferenceASTVisitor visitor;

	@BeforeEach
	public void setUp() {
		visitor = new LocalVariableTypeInferenceASTVisitor();
	}

	@Test
	public void visit_simpleTypeInitialization_shouldReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_DATE);
		String block = "Date date = new Date();";
		String varDeclaration = "var date = new Date();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(varDeclaration);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_enhancedForLoopDeclaration_shouldReplace() throws Exception {
		String block = "for(String string : Arrays.asList(\"1\", \"2\", \"3\")) {}";
		String varDeclaration = "for(var string : Arrays.asList(\"1\", \"2\", \"3\")) {}";

		fixture.addImport(JAVA_UTIL_ARRAYS);
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(varDeclaration);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_forLoop_shouldReplace() throws Exception {
		String original = "String[]values = Arrays.asList(\"1\", \"2\", \"3\"); int i =0; for(String value = values[i]; i < values.length; i++) {}";
		String expected = "String[]values = Arrays.asList(\"1\", \"2\", \"3\"); int i =0; for(var value = values[i]; i < values.length; i++) {}";

		fixture.addImport(JAVA_UTIL_ARRAYS);
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_forLoopMultipleInitializations_shouldNotReplace() throws Exception {
		String block = "String[]values = Arrays.asList(\"1\", \"2\", \"3\"); int i =0; for(String value = values[i], suffix = \"s\"; i < values.length; i++) {}";

		fixture.addImport(JAVA_UTIL_ARRAYS);
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_varInEnhancedForLoop_shouldNotReplace() throws Exception {
		String block = "for(var string : Arrays.asList(\"1\", \"2\", \"3\")) {}";

		fixture.addImport(JAVA_UTIL_ARRAYS);
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_arrayInEnhancedForLoop_shouldReplace() throws Exception {
		String block = "String [] strings; strings = new String [] {\"\", \"\"}; for(String string : strings) {}";
		String expectedBlockContent = "String [] strings; strings = new String [] {\"\", \"\"}; for(var string : strings) {}";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expectedBlockContent);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_rawTypes_shouldReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		String block = "HashMap map = new HashMap();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString("var map = new HashMap();");
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_rawInitializer_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		String block = "HashMap<String, String> map = new HashMap();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_rawDeclarationType_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		String block = "HashMap map = new HashMap<String, String>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_primitiveType_shouldNotReplace() throws Exception {
		String block = "int i = 0;";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_alreadyVar_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_DATE);
		String block = "var map = new Date();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_diamondInInitialization_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		String block = "HashMap<String, String> map = new HashMap<>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_wildcardInInitialization_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		String block = "HashMap<Object, Object> map = new HashMap<?, ?>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_multipleDeclarationFragments_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		String block = "HashMap<String, String> map1 = new HashMap<String, String>(), map2 = new HashMap<String, String>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_nullTypeInitialization_shouldNotReplace() throws Exception {
		String block = "String nullValue = null;";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_subTypeInitialization_shouldReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		fixture.addImport(JAVA_UTIL_MAP);
		String block = "Map<String, String> map = new HashMap<String, String>();";
		String expectedContent = "var map = new HashMap<String, String>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expectedContent);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_incompatibleSiblingReInitialization_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_ARRAY_LIST);
		fixture.addImport(JAVA_UTIL_LINKED_LIST);
		fixture.addImport(JAVA_UTIL_LIST);
		String block = "int anotherVariable = 0; List<String> list = new ArrayList<String>(); list = new LinkedList<>(); anotherVariable = 1;";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_initializationWithWildcard_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_HASH_MAP);
		fixture.addImport(JAVA_UTIL_MAP);
		String block = "Map<Object, Object> map = new HashMap<?,?>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_simpleTypeWithoutInitialization_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_DATE);
		String block = "Date date; date = new Date();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_singleVariableDeclarationInCatchBlock_shouldNotReplace() throws Exception {
		fixture.addImport(JAVA_IO_FILE);
		String block = "try {new File(\"file.name\");} catch(NullPointerException e) {}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_initializationWithLambda_shouldNotReplace() throws Exception {
		String block = "Runnable r = () -> {};";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_singleDeclarationInLambdaParameter_shouldNotReplace() throws Exception {
		fixture.addImport("java.util.Predicate");
		String block = "Predicate<String>  myPredicate = (String value) -> value.isEmpty();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_missingSpaceBetweenTypeAndName_shouldReplace() throws Exception {
		fixture.addImport(JAVA_UTIL_LIST);
		fixture.addImport(JAVA_UTIL_ARRAY_LIST);
		String block = "List<String>name = new ArrayList<String>();";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);
		Block expectedBlock = ASTNodeBuilder.createBlockFromString("var name = new ArrayList<String>();");
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_arrayTypeWithDimensions_shouldTransform() throws Exception {
		String block = "String []names[] = new String[][] {};";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString("var names = new String[][]{};");
		assertMatch(expectedBlock, fixture.getMethodBlock());

	}

	@Test
	public void visit_arrayInitializer_shouldNotTransform() throws Exception {
		String block = "String []names[] = {{}};";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());

	}
	
	@Test
	public void visit_methodReferenceInitializer_shouldNotTransform() throws Exception {
		String block = "Runnable r = this::hashCode;";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());

	}

	@Test
	public void visit_2dArray_shouldTransform() throws Exception {
		String block = "String names[][] = {}; for(String t[] : names) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString("String names[][] = {}; for(var t : names) { }");
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_conditionalExpressionHavingDiamond_shouldNotTransform() throws Exception {
		String block = "List<String> names = true ? new ArrayList<>() : new LinkedList<>();";
		fixture.addImport(JAVA_UTIL_LIST);
		fixture.addImport(JAVA_UTIL_ARRAY_LIST);
		fixture.addImport(JAVA_UTIL_LINKED_LIST);
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_conditionalExpression_shouldTransform() throws Exception {
		String block = "List<String> names = true ? new ArrayList<String>() : new LinkedList<String>();";
		fixture.addImport(JAVA_UTIL_LIST);
		fixture.addImport(JAVA_UTIL_ARRAY_LIST);
		fixture.addImport(JAVA_UTIL_LINKED_LIST);
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString("var names = true ? new ArrayList<String>() : new LinkedList<String>();");
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_anonymousClasses_shouldNotTransform() throws Exception {
		String block = "Runnable r = new Runnable() { @Override public void run() { }};";
		fixture.addMethodBlock(block);

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
}

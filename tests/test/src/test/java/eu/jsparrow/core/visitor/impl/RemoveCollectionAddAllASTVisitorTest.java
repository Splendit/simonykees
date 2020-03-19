package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings("nls")
public class RemoveCollectionAddAllASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private RemoveCollectionAddAllASTVisitor visitor;

	@BeforeEach
	public void setUp() throws Exception {
		visitor = new RemoveCollectionAddAllASTVisitor();
		fixture.addImport(java.util.Collection.class.getName());
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Set.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		fixture.addImport(java.util.HashSet.class.getName());
		fixture.addImport(java.util.Arrays.class.getName());
	}

	@Test
	public void visit_InvokeAddAllOnSetVariable_shouldTransform() throws Exception {
		String before = "Set<String> set = new HashSet<>();\n" +
				"set.addAll(Arrays.asList(\"value1\", \"value2\"));";

		String afterExpected = "Set<String> set = new HashSet<>(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	@Test
	public void visit_InvokeAddAllOnListVariable_shouldTransform() throws Exception {
		String before = "List<String> list = new ArrayList<>();\n" +
				"list.addAll(Arrays.asList(\"value1\", \"value2\"));";
		String afterExpected = "List<String> list = new ArrayList<>(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	@Test
	public void visit_InvokeAddAllWithoutInvocationExpression_shouldNotTransform() throws Exception {

		String before = "class ArrayListSubclass extends ArrayList {\n" + 
				"    ArrayListSubclass() {\n" + 
				"	    addAll(Arrays.asList(\"value1\", \"value2\"));\n" + 
				"    }\n" + 
				"}";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);

	}

	@Test
	public void visit_InvokeAddAllOnNotInitializedSet_shouldNotTransform() throws Exception {
		String before = "Set<String> set;\n" +
				"set.addAll(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_InvokeAddAllNotInitializedWithConstructor_shouldNotTransform() throws Exception {
		String before = "Set<String> set0 = new HashSet<>();\n" +
				"Set<String> set1 = set0;\n" +
				"set1.addAll(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_InvokeContainsOnSet_shouldNotTransform() throws Exception {
		String before = "Set<String> set = new HashSet<>();\n" +
				"set.contains(\"value1\");";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	/**
	 * Expected to transform.
	 * 
	 * @throws Exception
	 */
	@Test
	public void visit_InvokeAddAll_NotWithinBlock_shouldNotTransform() throws Exception {
		String before = "Set<String> set = new HashSet<>();\n" +
				"if(true)set.addAll(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	/**
	 * Expected to transform.
	 * 
	 * @throws Exception
	 */
	@Test
	public void visit_InvokeAddAll_NotAfterVariableDeclaration_shouldNotTransform() throws Exception {
		String before = "Set<String> set = new HashSet<>();\n" +
				"set.contains(\"value1\");" +
				"set.addAll(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_InvokeAddAllOnArrayListAsCollection_shouldTransform() throws Exception {
		String before = "Collection<String> collection = new ArrayList<>();\n" +
				"collection.addAll(Arrays.asList(\"value1\", \"value2\"));";
		String afterExpected = "Collection<String> collection = new ArrayList<>(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	@Test
	public void visit_InvokeAddAllFirstInSubordinateBlock_shouldNotTransform() throws Exception {
		String before = "Set<String> set = new HashSet<>();\n" +
				"{\nset.addAll(Arrays.asList(\"value1\", \"value2\"));\n}";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_InvokeAddAll_NotImmediatelyAfterDeclaration_shouldNotTransform() throws Exception {
		String before = "List<String> list = new ArrayList<>();\n" +
				"List<String> otherList = new ArrayList<>();\n" +
				"list.addAll(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_InvokeAddAll_AfterNotEmptyConstructor_shouldNotTransform() throws Exception {
		String before = "List<String> list = new ArrayList<>(Arrays.asList(\"value1\", \"value2\"));\n" +
				"list.addAll(Arrays.asList(\"value3\", \"value4\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_InvokeAddAll_AfterVariableDeclarationWithTowFragments_shouldNotTransform() throws Exception {
		String before = "List<String> list0 = new ArrayList<>(), list1 = new ArrayList<>();\n" +
				"list1.addAll(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_InvokeAddAllOnExpression_shouldNotTransform() throws Exception {
		String before = "Iterable<String> iterable = new ArrayList<>();\n" +
				"((Collection)iterable).addAll(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_InvokeAddAllAfterAnonymousClassInstantiation_shouldNotTransform() throws Exception {
		String before = "List<String> list = new ArrayList<String>() {};\n" +
				"list.addAll(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_InvokeAddAllAddingListToItself_shouldNotTransform() throws Exception {
		String before = "List<String> list = new ArrayList<>();\n" +
				"list.addAll(list);";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_InvokeAddAllOverload_shouldNotTransform() throws Exception {
		String before = "ArrayList<String> list = new ArrayList<>();\n" +
				"list.addAll(1, Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_InvokeAddAllAssignedToBoolean_shouldNotTransform() throws Exception {
		String before = "ArrayList<String> list = new ArrayList<>();\n" +
				"boolean b = list.addAll(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_InvokeAddAllAfterNoJavaUtilClassConstruction_shouldNotTransform() throws Exception {
		String before = "class ArrayListSubclass<T> extends ArrayList<T> {};\n" +
				"ArrayListSubclass<String> list = new ArrayListSubclass<>();\n" +
				"list.addAll(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}
}

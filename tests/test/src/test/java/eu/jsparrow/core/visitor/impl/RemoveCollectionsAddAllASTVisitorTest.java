package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings("nls")
public class RemoveCollectionsAddAllASTVisitorTest extends UsesSimpleJDTUnitFixture {
	

	private RemoveCollectionsAddAllASTVisitor visitor;

	@BeforeEach
	public void setUp() throws Exception {
		visitor = new RemoveCollectionsAddAllASTVisitor();
		fixture.addImport(java.util.Collection.class.getName());
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Set.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		fixture.addImport(java.util.HashSet.class.getName());
		fixture.addImport(java.util.Arrays.class.getName());
	}

	/**
	 * Expected to transform.
	 * 
	 * @throws Exception
	 */
	@Test
	public void addAll_SetVariable_HashSetConstructor() throws Exception {
		String before = "Set<String> set = new HashSet<>();\n" +
				"set.addAll(Arrays.asList(\"value1\", \"value2\"));";

		String afterExpected = "Set<String> set = new HashSet<>(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	/**
	 * Expected to transform.
	 * 
	 * @throws Exception
	 */
	@Test
	public void addAll_ListVariable_ArrayListConstructor() throws Exception {
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
	public void visit_InvokeAddAll_OnArrayListAsCollection_shouldTransform() throws Exception {
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
	public void visit_InvokeAddAll_FirstInSubordinateBlock_shouldNotTransform() throws Exception {
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
	public void visit_InvokeAddAll_AddListToItself_shouldNotTransform() throws Exception {
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
	public void visit_InvokeAddAll_NoJavaUtilClassConstruction_shouldNotTransform() throws Exception {		
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

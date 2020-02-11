package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import java.util.Collection;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings("nls")
public class RemoveCollectionsAddAllASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private RemoveCollectionsAddAllASTVisitor visitor;

	@BeforeEach
	public void setUp() {
		visitor = new RemoveCollectionsAddAllASTVisitor();
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

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.Set.class.getName());
		fixture.addImport(java.util.HashSet.class.getName());
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

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
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
	public void addAll_CollectionVariable_ArrayListConstructor() throws Exception {
		String before = "Collection<String> collection = new ArrayList<>();\n" +
				"collection.addAll(Arrays.asList(\"value1\", \"value2\"));";
		String afterExpected = "Collection<String> collection = new ArrayList<>(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.Collection.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	/**
	 * Expected not to transform.
	 * 
	 * @throws Exception
	 */
	@Test
	public void addAll_SubordinateBlock() throws Exception {
		String before = "Set<String> set = new HashSet<>();\n" +
				"{\nset.addAll(Arrays.asList(\"value1\", \"value2\"));\n}";

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.Set.class.getName());
		fixture.addImport(java.util.HashSet.class.getName());
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	/**
	 * Expected not to transform.
	 * 
	 * @throws Exception
	 */
	@Test
	public void addAll_notImmediatelyAfterDeclaraton() throws Exception {
		String before = "List<String> list = new ArrayList<>();\n" +
				"List<String> otherList = new ArrayList<>();\n" +
				"list.addAll(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	/**
	 * Expected not to transform.
	 * 
	 * @throws Exception
	 */
	@Test
	public void addAll_AfterNotEmptyConstructor() throws Exception {
		String before = "List<String> list = new ArrayList<>(Arrays.asList(\"value1\", \"value2\"));\n" +
				"list.addAll(Arrays.asList(\"value3\", \"value4\"));";

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	/**
	 * Expected not to transform.
	 * 
	 * @throws Exception
	 */
	@Test
	public void addAll_TwoVariableDeclarationFragments() throws Exception {
		String before = "List<String> list0 = new ArrayList<>(), list1 = new ArrayList<>();\n" +
				"list1.addAll(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	/**
	 * Method {@link Collection#addAll(Collection)} is not called on a simple
	 * local variable name but on a cast expression. Expected not to transform.
	 * 
	 * @throws Exception
	 */
	@Test
	public void addAll_IterableVariable_ArrayListConstructor() throws Exception {
		String before = "Iterable<String> iterable = new ArrayList<>();\n" +
				"((Collection)iterable).addAll(Arrays.asList(\"value1\", \"value2\"));";

		fixture.addImport(java.lang.Iterable.class.getName());
		fixture.addImport(java.util.Collection.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

}

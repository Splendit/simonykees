package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
public class RemoveCollectionAddAllASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new RemoveCollectionAddAllASTVisitor());
		fixture.addImport(java.util.Collection.class.getName());
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Set.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		fixture.addImport(java.util.HashSet.class.getName());
		fixture.addImport(java.util.Arrays.class.getName());
	}

	@Test
	public void visit_InvokeAddAllOnSetVariable_shouldTransform() throws Exception {
		String original = "" +
				"Set<String> set = new HashSet<>();\n" +
				"set.addAll(Arrays.asList(\"value1\", \"value2\"));";
		String expected = "Set<String> set = new HashSet<>(Arrays.asList(\"value1\", \"value2\"));";
		
		assertChange(original, expected);
	}

	@Test
	public void visit_InvokeAddAllOnListVariable_shouldTransform() throws Exception {
		String original = "" +
				"List<String> list = new ArrayList<>();\n" +
				"list.addAll(Arrays.asList(\"value1\", \"value2\"));";
		String expected = "List<String> list = new ArrayList<>(Arrays.asList(\"value1\", \"value2\"));";
		
		assertChange(original, expected);
	}

	@Test
	public void visit_InvokeAddAllWithoutInvocationExpression_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"class ArrayListSubclass extends ArrayList {\n" + 
				"    ArrayListSubclass() {\n" + 
				"	    addAll(Arrays.asList(\"value1\", \"value2\"));\n" + 
				"    }\n" + 
				"}");
	}

	@Test
	public void visit_InvokeAddAllOnNotInitializedSet_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"Set<String> set;\n" +
				"set.addAll(Arrays.asList(\"value1\", \"value2\"));");
	}

	@Test
	public void visit_InvokeAddAllNotInitializedWithConstructor_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"Set<String> set0 = new HashSet<>();\n" +
				"Set<String> set1 = set0;\n" +
				"set1.addAll(Arrays.asList(\"value1\", \"value2\"));");
	}

	@Test
	public void visit_InvokeContainsOnSet_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"Set<String> set = new HashSet<>();\n" +
				"set.contains(\"value1\");");
	}

	/**
	 * Expected to transform.
	 * 
	 * @throws Exception
	 */
	@Test
	public void visit_InvokeAddAll_NotWithinBlock_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"Set<String> set = new HashSet<>();\n" +
				"if(true)set.addAll(Arrays.asList(\"value1\", \"value2\"));");
	}

	/**
	 * Expected to transform.
	 * 
	 * @throws Exception
	 */
	@Test
	public void visit_InvokeAddAll_NotAfterVariableDeclaration_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"Set<String> set = new HashSet<>();\n" +
				"set.contains(\"value1\");" +
				"set.addAll(Arrays.asList(\"value1\", \"value2\"));");
	}

	@Test
	public void visit_InvokeAddAllOnArrayListAsCollection_shouldTransform() throws Exception {
		String original = "" +
				"Collection<String> collection = new ArrayList<>();\n" +
				"collection.addAll(Arrays.asList(\"value1\", \"value2\"));";
		String expected = "" +
				"Collection<String> collection = new ArrayList<>(Arrays.asList(\"value1\", \"value2\"));";

		assertChange(original, expected);
	}

	@Test
	public void visit_InvokeAddAllFirstInSubordinateBlock_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"Set<String> set = new HashSet<>();\n" +
				"{\nset.addAll(Arrays.asList(\"value1\", \"value2\"));\n}");
	}

	@Test
	public void visit_InvokeAddAll_NotImmediatelyAfterDeclaration_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"List<String> list = new ArrayList<>();\n" +
				"List<String> otherList = new ArrayList<>();\n" +
				"list.addAll(Arrays.asList(\"value1\", \"value2\"));");
	}

	@Test
	public void visit_InvokeAddAll_AfterNotEmptyConstructor_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"List<String> list = new ArrayList<>(Arrays.asList(\"value1\", \"value2\"));\n" +
				"list.addAll(Arrays.asList(\"value3\", \"value4\"));");
	}

	@Test
	public void visit_InvokeAddAll_AfterVariableDeclarationWithTowFragments_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"List<String> list0 = new ArrayList<>(), list1 = new ArrayList<>();\n" +
				"list1.addAll(Arrays.asList(\"value1\", \"value2\"));");
	}

	@Test
	public void visit_InvokeAddAllOnExpression_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"Iterable<String> iterable = new ArrayList<>();\n" +
				"((Collection)iterable).addAll(Arrays.asList(\"value1\", \"value2\"));");
	}

	@Test
	public void visit_InvokeAddAllAfterAnonymousClassInstantiation_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"List<String> list = new ArrayList<String>() {};\n" +
				"list.addAll(Arrays.asList(\"value1\", \"value2\"));");
	}

	@Test
	public void visit_InvokeAddAllAddingListToItself_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"List<String> list = new ArrayList<>();\n" +
				"list.addAll(list);");
	}

	@Test
	public void visit_InvokeAddAllOverload_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"ArrayList<String> list = new ArrayList<>();\n" +
				"list.addAll(1, Arrays.asList(\"value1\", \"value2\"));");
	}

	@Test
	public void visit_InvokeAddAllAssignedToBoolean_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"ArrayList<String> list = new ArrayList<>();\n" +
				"boolean b = list.addAll(Arrays.asList(\"value1\", \"value2\"));");
	}

	@Test
	public void visit_InvokeAddAllAfterNoJavaUtilClassConstruction_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"class ArrayListSubclass<T> extends ArrayList<T> {};\n" +
				"ArrayListSubclass<String> list = new ArrayListSubclass<>();\n" +
				"list.addAll(Arrays.asList(\"value1\", \"value2\"));");
	}
	
	@Test
	public void visit_stackAddAll_shouldNotTransform() throws Exception {
		fixture.addImport(java.util.Stack.class.getName());
		fixture.addImport(java.util.Collections.class.getName());
		assertNoChange("" +
				"Stack<String> stack = new Stack<>();\n" +
				"stack.addAll(Collections.singletonList(\"value\"));");
	}
}

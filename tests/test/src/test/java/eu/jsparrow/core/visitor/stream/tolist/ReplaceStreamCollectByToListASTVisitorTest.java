package eu.jsparrow.core.visitor.stream.tolist;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

@SuppressWarnings("nls")
public class ReplaceStreamCollectByToListASTVisitorTest extends UsesJDTUnitFixture {

	private static final String TEST_METHOD_PARAMETERS = ""
			+ "Collection<String> collection"
			+ ", UnaryOperator<String> unaryOperator"
			+ ", Predicate<String> predicate";

	private static final String METHOD_INVOCATION_EXPRESSION = "collection.stream()\n" +
			"				.map(unaryOperator)\n" +
			"				.filter(predicate)\n" +
			"				";

	@BeforeEach
	public void setUpDefaultVisitor() throws Exception {
		setDefaultVisitor(new ReplaceStreamCollectByToListASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_CollectorsToUnmodifiableList_shouldTransform() throws Exception {

		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	List<String> testCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		return " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toUnmodifiableList());\n" +
				"	}";

		String expected = "" +
				"	List<String> testCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		return " + METHOD_INVOCATION_EXPRESSION + ".toList();\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_UnmodifiableListFromCollectorsToList_shouldTransform() throws Exception {

		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.Collections.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	List<String> testStreamCollect (" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		return Collections.unmodifiableList (" +
				METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList())" + ");\n" +
				"	}";

		String expected = "" +
				"	List<String> testStreamCollect (" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		return Collections.unmodifiableList (" +
				METHOD_INVOCATION_EXPRESSION + ".toList()" + ");\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_CollectFromCollectorsToListAsEnhancedForExpression_shouldTransform() throws Exception {

		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	void testStreamCollect (" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		for (String s : " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList())" + ") {\n" +
				"		}" +
				"	}";

		String expected = "" +
				"	void testStreamCollect (" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		for (String s : " + METHOD_INVOCATION_EXPRESSION + ".toList()" + ") {\n" +
				"		}" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_SizeOfCollectFromCollectorsToList_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	int testStreamCollect (" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		return " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList()).size();\n" +
				"	}";

		String expected = "" +
				"	int testStreamCollect (" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		return " + METHOD_INVOCATION_EXPRESSION + ".toList().size();\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_CollectCollectorsToListAsVariableInitializer_shouldTransform() throws Exception {

		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		List<String> list = " +
				METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList());\n" +
				"	}";

		String expected = "" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		List<String> list = " +
				METHOD_INVOCATION_EXPRESSION + ".toList();\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_CollectCollectorsToListAsAssignmentRightHandSide_shouldTransform() throws Exception {

		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		List<String> list = null;\n" +
				"		list = " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList());\n" +
				"	}";

		String expected = "" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		List<String> list = null;\n" +
				"		list = " + METHOD_INVOCATION_EXPRESSION + ".toList();\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_UnmodifiableListFromListVariable_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.Collections.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	List<String> testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		List<String> list = " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList());\n" +
				"		return Collections.unmodifiableList(list);\n" +
				"	}";

		String expected = "" +
				"	List<String> testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		List<String> list = " + METHOD_INVOCATION_EXPRESSION + ".toList();\n" +
				"		return Collections.unmodifiableList(list);\n" +
				"	}";

		assertChange(original, expected);

	}

	@Test
	public void visit_SizeOfListVariable_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.Collections.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		List<String> list = " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList());\n" +
				"		int count = list.size();\n" +
				"	}";

		String expected = "" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		List<String> list = " + METHOD_INVOCATION_EXPRESSION + ".toList();\n" +
				"		int count=list.size();\n" +
				"	}";

		assertChange(original, expected);

	}

	@Test
	public void visit_ListVariableAsEnhancedLoopExpression_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.Collections.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		List<String> list = " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList());\n" +
				"		for(String s : list) {\n" +
				"		}\n" +
				"	}";

		String expected = "" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		List<String> list = " + METHOD_INVOCATION_EXPRESSION + ".toList();\n" +
				"		for(String s : list) {\n" +
				"		}\n" +
				"	}";

		assertChange(original, expected);

	}

	@Test
	public void visit_CollectInvocationWithoutExpression_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());
		defaultFixture.addImport(java.util.stream.Stream.class.getName());

		String original = "" +
				"	interface StringStream extends Stream<String> {\n"
				+ "\n"
				+ "		default List<String> toImmutableList() {\n"
				+ "			return collect(Collectors.toUnmodifiableList());\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	interface StringStream extends Stream<String> {\n"
				+ "\n"
				+ "		default List<String> toImmutableList() {\n"
				+ "			return toList();\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_ReturnListVariableFromCollectingToList_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	List<String> testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		List<String> list = " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList());\n" +
				"		return list;\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AssignCollectCollectorsToListToField_shouldNotTransform() throws Exception {

		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	List<String> list;\n" +
				"\n" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		list = " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList());\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AssignCollectCollectorsToListToThisList_shouldNotTransform() throws Exception {

		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	List<String> list;\n" +
				"\n" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		this.list = " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList());\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_ReturnCollectCollectorsToList_shouldNotTransform() throws Exception {

		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	List<String> testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		return " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList());\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_ReturnCollectorsToUnmodifiableList_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.stream.Collector.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	List<String> returnCollectorsToUnmodifiableList (" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		return Collectors.toUnmodifiableList();\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_CollectorsToUnmodifiableListAsArgument_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.stream.Collector.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	void testUseCollectorToUnmodifiableList() {\n"
				+ "		useCollector(Collectors.toUnmodifiableList());\n"
				+ "	}\n"
				+ "\n"
				+ "	void useCollector(Collector<String, ?, List<String>> collector) {\n"
				+ "	}\n"
				+ "";

		assertNoChange(original);
	}

	@Test
	public void visit_UnresolvedCollectMethod_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.stream.Collector.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	List<String> testCollectCollectorsToList() {\n"
				+ "		return collect(Collectors.toList());\n"
				+ "	}";
		assertNoChange(original);
	}

	@Test
	public void visit_CollectNotStreamMethod_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.stream.Collector.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	void testCollectNozByStream() {\n"
				+ "		collect(Collectors.toUnmodifiableList());\n"
				+ "	}\n"
				+ "	\n"
				+ "	private void collect(Collector<Object, ?, List<Object>> unmodifiableList) {\n"
				+ "	}";
		assertNoChange(original);
	}
}

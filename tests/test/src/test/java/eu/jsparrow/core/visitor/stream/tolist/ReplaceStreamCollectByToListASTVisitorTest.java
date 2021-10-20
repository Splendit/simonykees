package eu.jsparrow.core.visitor.stream.tolist;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

class ReplaceStreamCollectByToListASTVisitorTest extends UsesJDTUnitFixture {

	private static final String TEST_METHOD_PARAMETERS = ""
			+ "Collection<String> collection"
			+ ", UnaryOperator<String> unaryOperator"
			+ ", Predicate<String> predicate";

	private static final String METHOD_INVOCATION_EXPRESSION = "collection.stream()\n" +
			"				.map(unaryOperator)\n" +
			"				.filter(predicate)\n" +
			"				";

	@BeforeEach
	void setUpDefaultVisitor() throws Exception {
		setDefaultVisitor(new ReplaceStreamCollectByToListASTVisitor());
		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.Collections.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());
		defaultFixture.addImport(java.util.stream.Collector.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_CollectorsToUnmodifiableList_shouldTransform() throws Exception {
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
	void visit_UnmodifiableListFromCollectorsToList_shouldTransform() throws Exception {
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
	void visit_CollectFromCollectorsToListAsEnhancedForExpression_shouldTransform() throws Exception {
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
	void visit_SizeOfCollectFromCollectorsToList_shouldTransform() throws Exception {
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
	void visit_CollectCollectorsToListAsVariableInitializer_shouldTransform() throws Exception {
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
	void visit_CollectCollectorsToListAsAssignmentRightHandSide_shouldTransform() throws Exception {
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
	void visit_UnmodifiableListFromListVariable_shouldTransform() throws Exception {
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

	@ParameterizedTest
	@ValueSource(strings = {
			"Collections.min(listCopy)",
			"Collections.min(listCopy)",
			"Collections.min(listCopy, String::compareToIgnoreCase)",
			"Collections.max(listCopy)",
			"Collections.max(listCopy, String::compareToIgnoreCase)",
			"Collections.frequency(listCopy, \"1\")",
			"Collections.disjoint(listCopy, Arrays.asList(\"1\", \"2\", \"3\", \"4\"))",
			"Collections.indexOfSubList(listCopy, Arrays.asList(\"2\", \"3\", \"4\"))",
			"Collections.indexOfSubList(Arrays.asList(\"0\", \"1\", \"2\", \"3\", \"4\"), listCopy)",
			"Collections.lastIndexOfSubList(listCopy, Arrays.asList(\"2\", \"3\", \"4\"))",
			"Collections.lastIndexOfSubList(Arrays.asList(\"0\", \"1\", \"2\", \"3\", \"4\"), listCopy)",
			"Collections.unmodifiableCollection(listCopy)",
			"Collections.unmodifiableList(listCopy)"
	})
	void visit_ArgumentOfStaticCollectionsMethod_shouldTransform(String collectionsMethodInvocation)
			throws Exception {

		String original = "" +
				"	void testStreamCollect() {\n" +
				"		List<String> list = Arrays.asList(\"1\", \"2\", \"3\", \"4\");\n" +
				"		List<String> listCopy = list.stream().collect(Collectors.toList());\n" +
				"		" + collectionsMethodInvocation + ";\n" +
				"	}";

		String expected = "" +
				"	void testStreamCollect() {\n" +
				"		List<String> list = Arrays.asList(\"1\", \"2\", \"3\", \"4\");\n" +
				"		List<String> listCopy = list.stream().toList();\n" +
				"		" + collectionsMethodInvocation + ";\n" +
				"	}";
		assertChange(original, expected);

	}

	@ParameterizedTest
	@ValueSource(strings = {
			"List.copyOf(listCopy)",
			"java.util.Set.copyOf(listCopy)",
			"list.addAll(listCopy)",
			"list.addAll(1, listCopy)",
			"list.containsAll(listCopy)",
			"list.removeAll(listCopy)",
			"list.retainAll(listCopy)"
	})
	void visit_ArgumentOfCollectionMethod_shouldTransform(String listMethodInvocation)
			throws Exception {
		String original = "" +
				"	void testWithListMethod() {\n" +
				"		List<String> list = Arrays.asList(\"1\", \"2\", \"3\", \"4\");\n" +
				"		List<String> listCopy = list.stream().collect(Collectors.toList());\n" +
				"		" + listMethodInvocation + ";\n" +
				"	}";

		String expected = "" +
				"	void testWithListMethod() {\n" +
				"		List<String> list = Arrays.asList(\"1\", \"2\", \"3\", \"4\");\n" +
				"		List<String> listCopy = list.stream().toList();\n" +
				"		" + listMethodInvocation + ";\n" +
				"	}";
		assertChange(original, expected);

	}

	@Test
	void visit_ArgumentOfEquals_shouldTransform()
			throws Exception {
		String original = "" +
				"	void testWithListMethod() {\n" +
				"		List<String> list = Arrays.asList(\"1\", \"2\", \"3\", \"4\");\n" +
				"		List<String> listCopy = list.stream().collect(Collectors.toList());\n" +
				"		list.equals(listCopy);\n" +
				"	}";

		String expected = "" +
				"	void testWithListMethod() {\n" +
				"		List<String> list = Arrays.asList(\"1\", \"2\", \"3\", \"4\");\n" +
				"		List<String> listCopy = list.stream().toList();\n" +
				"		list.equals(listCopy);\n" +
				"	}";
		assertChange(original, expected);

	}

	@Test
	void visit_ArgumentOfUseList_shouldNotTransform()
			throws Exception {
		String original = "" +
				"	void testWithUseList() {\n" +
				"		List<String> list = Arrays.asList(\"1\", \"2\", \"3\", \"4\");\n" +
				"		List<String> listCopy = list.stream().collect(Collectors.toList());\n" +
				"		useList(listCopy);\n" +
				"	}\n" +
				"	\n" +
				"	void useList(List<String> list) {\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	void visit_ArgumentOfUnresolvedMethod_shouldNotTransform()
			throws Exception {
		String original = "" +
				"	void testWithUseList() {\n" +
				"		List<String> list = Arrays.asList(\"1\", \"2\", \"3\", \"4\");\n" +
				"		List<String> listCopy = list.stream().collect(Collectors.toList());\n" +
				"		useList(listCopy);\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	void visit_CollectCollectorsToListToArray_shouldTransform() throws Exception {
		String original = "" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		String[] stringArray = " + METHOD_INVOCATION_EXPRESSION
				+ ".collect(Collectors.toList()).toArray();\n" +
				"	}";

		String expected = "" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		String[] stringArray = " + METHOD_INVOCATION_EXPRESSION + ".toList().toArray();\n" +
				"	}";

		assertChange(original, expected);

	}

	@Test
	void visit_CollectCollectorsToListSublist_shouldNotTransform() throws Exception {
		String original = "" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		List<String> sublist = " + METHOD_INVOCATION_EXPRESSION
				+ ".collect(Collectors.toList()).subList(1,2);\n" +
				"	}";

		assertNoChange(original);

	}

	@ParameterizedTest
	@ValueSource(strings = {
			"listCopy.hashCode();",
			"listCopy.equals(arraysAsList);",
			"listCopy.toString();",
			"listCopy.forEach(s -> {});",
			"listCopy.size();",
			"listCopy.isEmpty();",
			"listCopy.contains(\"1\");",
			"listCopy.toArray();",
			"listCopy.toArray(new String[4]);",
			"listCopy.containsAll(Arrays.asList(\"1\", \"2\", \"3\", \"4\"));",
			"listCopy.stream();",
			"listCopy.parallelStream();",
			"listCopy.indexOf(\"1\");",
			"listCopy.lastIndexOf(\"1\");",
			"listCopy.get(0);",
			"try { listCopy.notify(); } catch (Exception exception) { }",
			"try { listCopy.notifyAll(); } catch (Exception exception) { }",
			"try { listCopy.wait(); } catch (Exception exception) { }",
			"try { listCopy.wait(1000L); } catch (Exception exception) { }",
			"try { listCopy.wait(1000L, 100); } catch (Exception exception) { }"
	})
	void visit_InvocationOfMethodOnListVariable_shouldTransform(String methodInvocationOnList) throws Exception {
		String original = "" +
				"	void testMethodInvocationsOnList() {" +
				"		List<String> list = Arrays.asList(\"1\", \"2\", \"3\", \"4\");\n" +
				"		List<String> listCopy = list.stream().collect(Collectors.toList());\n" +
				"		" + methodInvocationOnList + "\n" +
				"	}";

		String expected = "" +
				"	void testMethodInvocationsOnList() {" +
				"		List<String> list = Arrays.asList(\"1\", \"2\", \"3\", \"4\");\n" +
				"		List<String> listCopy = list.stream().toList();\n" +
				"		" + methodInvocationOnList + "\n" +
				"	}";

		assertChange(original, expected);

	}

	@ParameterizedTest
	@ValueSource(strings = {
			"listCopy.add(\"5\");",
			"listCopy.add(0, \"0\");",
			"listCopy.addAll(Arrays.asList(\"6\", \"7\", \"8\", \"9\"));",
			"listCopy.addAll(1, Arrays.asList(\"1-1\", \"1-2\", \"1-3\", \"1-4\"));",
			"listCopy.set(0, \"0\");",
			"listCopy.remove(0);",
			"listCopy.remove(\"2\");",
			"listCopy.removeAll(Arrays.asList(\"1\", \"2\"));",
			"listCopy.removeIf(\"1\"::equals);",
			"listCopy.retainAll(Arrays.asList(\"1\", \"2\"));",
			"listCopy.clear();",
			"listCopy.replaceAll(s -> s + s);",
			"listCopy.sort((s1, s2) -> s1.compareTo(s2));",
			"listCopy.iterator();",
			"listCopy.listIterator();",
			"listCopy.listIterator(0);",
			"listCopy.spliterator();",
			"listCopy.subList(0, 1);",
			"listCopy.getClass();"
	})
	void visit_InvocationOfMethodOnListVariable_shouldNotTransform(String methodInvocationOnList)
			throws Exception {
		String original = "" +
				"	void testMethodInvocationsOnList() {" +
				"		List<String> list = Arrays.asList(\"1\", \"2\", \"3\", \"4\");\n" +
				"		List<String> listCopy = list.stream().collect(Collectors.toList());\n" +
				"		" + methodInvocationOnList + "\n" +
				"	}";

		assertNoChange(original);

	}

	@Test
	void visit_ListVariableAsEnhancedLoopExpression_shouldTransform() throws Exception {
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
	void visit_CollectInvocationWithoutExpression_shouldTransform() throws Exception {
		
		defaultFixture.addImport(java.util.stream.Stream.class.getName());

		String original = "" +
				"	interface StringStream extends Stream<String> {\n"
				+ "" + "		default List<String> toImmutableList() {\n"
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
	void visit_ReturnListVariableFromCollectingToList_shouldNotTransform() throws Exception {
		String original = "" +
				"	List<String> testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		List<String> list = " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList());\n" +
				"		return list;\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	void visit_AssignCollectCollectorsToListToField_shouldNotTransform() throws Exception {
		String original = "" +
				"	List<String> list;\n" +
				"\n" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		list = " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList());\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	void visit_AssignCollectCollectorsToListToThisList_shouldNotTransform() throws Exception {
		String original = "" +
				"	List<String> list;\n" +
				"\n" +
				"	void testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		this.list = " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList());\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	void visit_ReturnCollectCollectorsToList_shouldNotTransform() throws Exception {
		String original = "" +
				"	List<String> testStreamCollect(" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		return " + METHOD_INVOCATION_EXPRESSION + ".collect(Collectors.toList());\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	void visit_ReturnCollectorsToUnmodifiableList_shouldNotTransform() throws Exception {
		String original = "" +
				"	List<String> returnCollectorsToUnmodifiableList (" + TEST_METHOD_PARAMETERS + ") {\n" +
				"		return Collectors.toUnmodifiableList();\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	void visit_CollectorsToUnmodifiableListAsArgument_shouldNotTransform() throws Exception {
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
	void visit_UnresolvedCollectMethod_shouldNotTransform() throws Exception {
		String original = "" +
				"	List<String> testCollectCollectorsToList() {\n"
				+ "		return collect(Collectors.toList());\n"
				+ "	}";
		assertNoChange(original);
	}

	@Test
	void visit_CollectNotStreamMethod_shouldNotTransform() throws Exception {
		String original = "" +
				"	void testCollectNozByStream() {\n"
				+ "		collect(Collectors.toUnmodifiableList());\n"
				+ "	}\n"
				+ "	\n"
				+ "	private void collect(Collector<Object, ?, List<Object>> unmodifiableList) {\n"
				+ "	}";
		assertNoChange(original);
	}

	@Test
	void visit_ChangingElementTypeInVariableInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"	void cornerCaseWithTypeArguments(List<Object> objects) {\n"
				+ "		List<Object> objectsToString = objects.stream().map(Object::toString).collect(Collectors.toList());\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	void visit_ChangingElementTypeInAssignmentRightHandSide_shouldNotTransform() throws Exception {
		String original = "" +
				"	void cornerCaseWithTypeArguments(List<Object> objects) {\n"
				+ "		List<Object> objectsToString = null;\n"
				+ "		objectsToString = objects.stream().map(Object::toString).collect(Collectors.toList());\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	void visit_ChangingElementTypeInObjectVariableInitializer_shouldTransform() throws Exception {
		String original = "" +
				"	void cornerCaseWithObjectVariableInitialization(List<Object> objects) {\n"
				+ "		Object o = objects.stream().map(Object::toString).collect(Collectors.toList());\n"
				+ "	}";

		String expected = "" +
				"	void cornerCaseWithObjectVariableInitialization(List<Object> objects) {\n"
				+ "		Object o = objects.stream().map(Object::toString).toList();\n"
				+ "	}";

		assertChange(original, expected);
	}
}

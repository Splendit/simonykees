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
	public void visit_CollectorsToUnmodifiableList_ShouldTransform() throws Exception {

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
	public void visit_UnmodifiableListFromCollectorsToList_ShouldTransform() throws Exception {

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
	public void visit_CollectCollectorsToListAsVariableInitializer_ShouldTransform() throws Exception {

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
	public void visit_CollectCollectorsToListAsAssignmentLeftHandSide_ShouldTransform() throws Exception {

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
}

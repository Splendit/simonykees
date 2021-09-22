package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceStreamCollectByToListASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUpDefaultVisitor() throws Exception {
		setDefaultVisitor(new ReplaceStreamCollectByToListASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	/**
	 * SIM-2006: this test is expected to fail as soon as
	 * {@link ReplaceStreamCollectByToListASTVisitor} will have been implemented
	 * 
	 */
	@Test
	public void visit_CollectorsToUnmodifiableList_ShouldTransformWhenImplemented() throws Exception {

		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	List<String> testCollect(Collection<String> collection, UnaryOperator<String> unaryOperator,\n"
				+ "			Predicate<String> predicate) {\n"
				+ "		return collection.stream()\n"
				+ "				.map(unaryOperator)\n"
				+ "				.filter(predicate)\n"
				+ "				.collect(Collectors.toUnmodifiableList());\n"
				+ "	}";

		assertNoChange(original);
	}

	/**
	 * SIM-2006: this test is expected to fail as soon as
	 * {@link ReplaceStreamCollectByToListASTVisitor} will have been implemented
	 * 
	 */
	@Test
	public void visit_UnmodifiableListFromCollectorsToList_ShouldTransformWhenImplemented() throws Exception {

		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.Collections.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	List<String> testStreamCollect(Collection<String> collection, UnaryOperator<String> function,\n"
				+ "			Predicate<String> predicate) {\n"
				+ "		return Collections.unmodifiableList(collection //\n"
				+ "				.stream() //\n"
				+ "				.map(function) //\n"
				+ "				.filter(predicate) //\n"
				+ "				.collect(Collectors.toList()));\n"
				+ "	}";

		assertNoChange(original);
	}

	/**
	 * SIM-2006: this test is expected to fail as soon as
	 * {@link ReplaceStreamCollectByToListASTVisitor} will have been implemented
	 * 
	 */
	@Test
	public void visit_VariableFromCollectorsToList_ShouldTransformWhenImplemented() throws Exception {

		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.function.Predicate.class.getName());
		defaultFixture.addImport(java.util.function.UnaryOperator.class.getName());
		defaultFixture.addImport(java.util.stream.Collectors.class.getName());

		String original = "" +
				"	void testStreamCollect(Collection<String> collection, UnaryOperator<String> function, Predicate<String> predicate) {\n"
				+ "		List<String> list = collection \n"
				+ "				.stream() \n"
				+ "				.map(function) \n"
				+ "				.filter(predicate) \n"
				+ "				.collect(Collectors.toList());\n"
				+ "	}";

		assertNoChange(original);
	}
}

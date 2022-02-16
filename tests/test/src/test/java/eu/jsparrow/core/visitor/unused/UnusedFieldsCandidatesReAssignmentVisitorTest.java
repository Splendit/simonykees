package eu.jsparrow.core.visitor.unused;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings("nls")
public class UnusedFieldsCandidatesReAssignmentVisitorTest extends UsesJDTUnitFixture {

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	private static Stream<Arguments> privateFragmentWithReAssignment() throws Exception {
		return Stream.of(
				// Arguments.of("", ""),
				Arguments.of("Instant now=Instant.now();", "unusedField=now.getNano();"),
				Arguments.of("Exception exception =  new Exception(\"Message!\");",
						"unusedField=exception.getMessage();"),
				Arguments.of("Calendar gregorianCalendar=GregorianCalendar.getInstance();",
						"unusedField=gregorianCalendar.getTime();"),
				Arguments.of("", "unusedField=Collections.singletonList(\"s\");"),
				Arguments.of("Object o=new Object();", "unusedField=o.hashCode();"),
				Arguments.of("Object o=new Object();", "unusedField=o.getClass();"),
				Arguments.of("Object o=new Object();", "unusedField=o.equals(o);"),
				Arguments.of("String s=new String(\"\");", "unusedField=s.length();"),
				Arguments.of("Collection<String> collection=Collections.singletonList(\"s\");",
						"unusedField=collection.size();"),
				Arguments.of("List<String> list=Collections.singletonList(\"s\");", "unusedField=list.size();"),
				Arguments.of("Map<String, String> map=new HashMap<>();", "unusedField=map.size();"),
				Arguments.of("HashMap<String, String> map=new HashMap<>();", "unusedField=map.size();"));
	}

	@ParameterizedTest
	@MethodSource(value = "privateFragmentWithReAssignment")
	void testPrivateFragmentWithReAssignment_shouldBeRemoved(String usedFieldDeclaration, String reAssignmentStatement)
			throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		options.put("remove-initializers-side-effects", false);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);
		defaultFixture.addImport(java.util.Collections.class.getName());
		defaultFixture.addImport(java.util.Collection.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Map.class.getName());
		defaultFixture.addImport(java.util.HashMap.class.getName());
		defaultFixture.addImport(java.util.Calendar.class.getName());
		defaultFixture.addImport(java.util.GregorianCalendar.class.getName());
		defaultFixture.addImport(java.time.Instant.class.getName());

		String originalCode = String.format("" +
				"	%s\n" +
				"	private Object unusedField;\n" +
				"	void reAssignment() {\n" +
				"		%s\n" +
				"	}", usedFieldDeclaration, reAssignmentStatement);

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, originalCode);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> removedUnusedFields = visitor.getUnusedPrivateFields();
		assertEquals(1, removedUnusedFields.size());
		UnusedFieldWrapper unusedFieldWrapper = removedUnusedFields.get(0);
		String removedUnusedFieldName = unusedFieldWrapper
			.getFieldName();
		assertEquals("unusedField", removedUnusedFieldName);
		List<ExpressionStatement> unusedReassignments = unusedFieldWrapper.getUnusedReassignments();
		assertEquals(1, unusedReassignments.size());
		String actualRemovedAssignment = unusedReassignments.get(0)
			.toString()
			.trim();
		assertEquals(reAssignmentStatement, actualRemovedAssignment);

	}

	@Test
	void testReAssignmentNotInBlock_shouldNotBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		options.put("remove-initializers-side-effects", true);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);

		String originalCode = "" +
				"	private int x = 0;\n" +
				"	void assignmentNotInBlock() {\n" +
				"		boolean condition = true;\n" +
				"		if (condition) x = 1;\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, originalCode);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> removedUnusedFields = visitor.getUnusedPrivateFields();
		assertTrue(removedUnusedFields.isEmpty());
	}
}

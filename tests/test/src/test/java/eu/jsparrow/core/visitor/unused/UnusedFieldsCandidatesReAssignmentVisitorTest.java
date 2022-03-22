package eu.jsparrow.core.visitor.unused;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings("nls")
class UnusedFieldsCandidatesReAssignmentVisitorTest extends UsesJDTUnitFixture {

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
				Arguments.of("HashMap<String, String> map=new HashMap<>();", "unusedField=map.size();"),
				Arguments.of("HashMap<String, String> map=new HashMap<>();", "this.unusedField=map.size();"));
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
			.getClassMemberIdentifier();
		assertEquals("unusedField", removedUnusedFieldName);
		List<ExpressionStatement> unusedReassignments = unusedFieldWrapper.getUnusedReassignments();
		assertEquals(1, unusedReassignments.size());
		String actualRemovedAssignment = unusedReassignments.get(0)
			.toString()
			.trim();
		assertEquals(reAssignmentStatement, actualRemovedAssignment);

	}

	private static Stream<Arguments> assignmentToPrivateArrayElements() throws Exception {
		return Stream.of(
				Arguments.of("private int[] unusedArray = new int[10];",
						"unusedArray[0]=1;"),
				Arguments.of("private int[] unusedArray = new int[10];",
						"++unusedArray[0];"),
				Arguments.of("private int[] unusedArray = new int[10];",
						"unusedArray[0]++;"),
				Arguments.of("private Object o = new Object(); private int[] unusedArray = new int[10];",
						"unusedArray[0]=o.hashCode();"),
				Arguments.of("private Object o = new Object(); private int[] unusedArray = new int[10];",
						"this.unusedArray[0]=o.hashCode();"),
				Arguments.of("private int[] unusedArray = new int[5][5];",
						"unusedArray[0][0]=1;"),
				Arguments.of("private int[] unusedArray = new int[5][5];",
						"unusedArray[0]=new int[10];"));
	}

	@ParameterizedTest
	@MethodSource(value = "assignmentToPrivateArrayElements")
	void testAssignmentToPrivateArrayElements(String fieldDeclarations, String reAssignmentStatement)
			throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		options.put("remove-initializers-side-effects", false);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);

		String originalCode = String.format("" +
				"	%s\n" +
				"	void reAssignment() {\n" +
				"		%s\n" +
				"	}", fieldDeclarations, reAssignmentStatement);

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, originalCode);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> removedUnusedFields = visitor.getUnusedPrivateFields();
		assertEquals(1, removedUnusedFields.size());
		UnusedFieldWrapper unusedFieldWrapper = removedUnusedFields.get(0);
		String removedUnusedFieldName = unusedFieldWrapper
			.getClassMemberIdentifier();
		assertEquals("unusedArray", removedUnusedFieldName);
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

	@Test
	void testReAssignmentNotInExpressionStatement_shouldNotBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		options.put("remove-initializers-side-effects", true);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);

		String originalCode = "" +
				"	private int x = 0;\n"
				+ "	void assingnmentNotInExpressionStatement() {\n"
				+ "		if((x = 0) == 0) {\n"
				+ "			\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, originalCode);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> removedUnusedFields = visitor.getUnusedPrivateFields();
		assertTrue(removedUnusedFields.isEmpty());
	}

	@Test
	void testRemoveSideEffectsOfReAssignment_shouldBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		options.put("remove-initializers-side-effects", true);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);

		String originalCode = "" +
				"	private int unusedField;\n" +
				"	private int usedField;\n" +
				"	void reAssignmentWithAssignment() {\n" +
				"		unusedField = (usedField = 1);\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, originalCode);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> removedUnusedFields = visitor.getUnusedPrivateFields();
		assertEquals(1, removedUnusedFields.size());
		UnusedFieldWrapper unusedFieldWrapper = removedUnusedFields.get(0);
		String removedUnusedFieldName = unusedFieldWrapper.getClassMemberIdentifier();
		assertEquals("unusedField", removedUnusedFieldName);
		List<ExpressionStatement> unusedReassignments = unusedFieldWrapper.getUnusedReassignments();
		assertEquals(1, unusedReassignments.size());
		String actualRemovedAssignment = unusedReassignments.get(0)
			.toString()
			.trim();
		String expectedRemovedAssignment = "unusedField=(usedField=1);";
		assertEquals(expectedRemovedAssignment, actualRemovedAssignment);
	}

	@Test
	void testRemoveSideEffectsOfFieldAccess_shouldBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		options.put("remove-initializers-side-effects", true);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);

		String originalCode = "" +
				"	private IntWrapper intWrapper = new IntWrapper();\n"
				+ "\n"
				+ "	class IntWrapper {\n"
				+ "		private int unusedIntWrapperField;\n"
				+ "	}\n"
				+ "\n"
				+ "	IntWrapper getIntWrapper() {\n"
				+ "		return intWrapper;\n"
				+ "	}\n"
				+ "\n"
				+ "	void reAssignToFieldAccessWithSideEffect() {\n"
				+ "		getIntWrapper().unusedIntWrapperField = 0;\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, originalCode);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> removedUnusedFields = visitor.getUnusedPrivateFields();
		assertEquals(1, removedUnusedFields.size());
		UnusedFieldWrapper unusedFieldWrapper = removedUnusedFields.get(0);
		String removedUnusedFieldName = unusedFieldWrapper.getClassMemberIdentifier();
		assertEquals("unusedIntWrapperField", removedUnusedFieldName);
		List<ExpressionStatement> unusedReassignments = unusedFieldWrapper.getUnusedReassignments();
		assertEquals(1, unusedReassignments.size());
		String actualRemovedAssignment = unusedReassignments.get(0)
			.toString()
			.trim();
		String expectedRemovedAssignment = "getIntWrapper().unusedIntWrapperField=0;";
		assertEquals(expectedRemovedAssignment, actualRemovedAssignment);
	}
	
	
	@Test
	void testRemoveSideEffectsOfArrayAccessAsLeftHandSide_shouldBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		options.put("remove-initializers-side-effects", true);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);

		String originalCode = "" +
				"	private int[] unusedArray = new int[10];\n"
				+ "	\n"
				+ "	int getValueWithSideEffects() {\n"
				+ "		return 0;\n"
				+ "	}\n"
				+ "	\n"
				+ "	void assignmentWithSideEffect() {\n"
				+ "		unusedArray[getValueWithSideEffects()] = 0;\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, originalCode);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> removedUnusedFields = visitor.getUnusedPrivateFields();
		assertEquals(1, removedUnusedFields.size());
		UnusedFieldWrapper unusedFieldWrapper = removedUnusedFields.get(0);
		String removedUnusedFieldName = unusedFieldWrapper.getClassMemberIdentifier();
		assertEquals("unusedArray", removedUnusedFieldName);
		List<ExpressionStatement> unusedReassignments = unusedFieldWrapper.getUnusedReassignments();
		assertEquals(1, unusedReassignments.size());
		String actualRemovedAssignment = unusedReassignments.get(0)
			.toString()
			.trim();
		String expectedRemovedAssignment = "unusedArray[getValueWithSideEffects()]=0;";
		assertEquals(expectedRemovedAssignment, actualRemovedAssignment);
	}

	@Test
	void testSideEffectsOfReAssignmentNotRemoved_shouldNotBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		options.put("remove-initializers-side-effects", false);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);

		String originalCode = "" +
				"	private int unusedField;\n" +
				"	private int usedField;\n" +
				"	void reAssignmentWithAssignment() {\n" +
				"		unusedField = (usedField = 1);\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, originalCode);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> removedUnusedFields = visitor.getUnusedPrivateFields();
		assertTrue(removedUnusedFields.isEmpty());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"getIntWrapper().unusedIntWrapperField = 0;",
			"++getIntWrapper().unusedIntWrapperField;",
			"getIntWrapper().unusedIntWrapperField++;",
			"--getIntWrapper().unusedIntWrapperField;",
			"getIntWrapper().unusedIntWrapperField--;",
	})
	void testReAssignToFieldAccessWithSideEffect_shouldNotBeRemoved(String statementToKeep) throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		options.put("remove-initializers-side-effects", false);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);

		String originalCode = "" +
				"	private IntWrapper intWrapper = new IntWrapper();\n"
				+ "\n"
				+ "	class IntWrapper {\n"
				+ "		private int unusedIntWrapperField = 0;\n"
				+ "	}\n"
				+ "\n"
				+ "	IntWrapper getIntWrapper() {\n"
				+ "		return intWrapper;\n"
				+ "	}\n"
				+ "\n"
				+ "	void reAssignToFieldAccessWithSideEffect() {\n"
				+ "		" + statementToKeep + "\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, originalCode);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> removedUnusedFields = visitor.getUnusedPrivateFields();
		assertTrue(removedUnusedFields.isEmpty());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"unusedArray[getValueWithSideEffects()]=0;",
			"unusedArray[getValueWithSideEffects()]++;",
			"++unusedArray[getValueWithSideEffects()];",
			"unusedArray[0]=getValueWithSideEffects();",
	})
	void testArrayAccessAndSideEffects_shouldNotBeRemoved(String statementToKeep) throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		options.put("remove-initializers-side-effects", false);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);

		String originalCode = "" +
				"	private int[] unusedArray = new int[10];\n"
				+ "	\n"
				+ "	int getValueWithSideEffects() {\n"
				+ "		return 0;\n"
				+ "	}\n"
				+ "	\n"
				+ "	void assignmentWithSideEffect() {\n"
				+ "		" + statementToKeep + "\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, originalCode);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> removedUnusedFields = visitor.getUnusedPrivateFields();
		assertTrue(removedUnusedFields.isEmpty());
	}

	@Test
	void testReAsssignmentToSuperClassField_shouldBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("protected-fields", true);
		options.put("remove-initializers-side-effects", false);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);

		String code = "" +
				"	class ExampleSuperClass {\n"
				+ "		protected int unusedField;\n"
				+ "	}\n"
				+ "\n"
				+ "	class ExampleReassigningUnusedSuperField extends ExampleSuperClass {\n"
				+ "\n"
				+ "		void reAssignSuperField() {\n"
				+ "			super.unusedField = 0;\n"
				+ "		}\n"
				+ "	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> unusedPrivateFields = visitor.getUnusedPrivateFields();
		assertTrue(unusedPrivateFields.isEmpty());
		List<NonPrivateUnusedFieldCandidate> candidates = visitor.getNonPrivateCandidates();
		assertEquals(1, candidates.size());
		NonPrivateUnusedFieldCandidate candidate = candidates.get(0);
		String unusedFieldName = candidate.getFragment()
			.getName()
			.getIdentifier();
		assertEquals("unusedField", unusedFieldName);

		List<ExpressionStatement> internalReassignments = candidate.getInternalReassignments();
		assertEquals(1, internalReassignments.size());
		String actualRemovedAssignment = internalReassignments.get(0)
			.toString()
			.trim();
		String expectedRemovedAssignment = "super.unusedField=0;";
		assertEquals(expectedRemovedAssignment, actualRemovedAssignment);
	}

	@Test
	void testReAsssignmentWithSideEffectToSuperClassField_shouldNotBeRemoved() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("protected-fields", true);
		options.put("remove-initializers-side-effects", false);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);

		String code = "" +
				"	class ExampleSuperClass {\n"
				+ "		protected int unusedField;\n"
				+ "	}\n"
				+ "\n"
				+ "	class ExampleReassigningUnusedSuperField extends ExampleSuperClass {\n"
				+ "\n"
				+ "		void reAssignSuperField() {\n"
				+ "			super.unusedField = getValueWithSideEffect();\n"
				+ "		}\n"
				+ "	}\n"
				+ "	\n"
				+ "	int getValueWithSideEffect() {\n"
				+ "		return 0;\n"
				+ "	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> unusedPrivateFields = visitor.getUnusedPrivateFields();
		assertTrue(unusedPrivateFields.isEmpty());
		List<NonPrivateUnusedFieldCandidate> candidates = visitor.getNonPrivateCandidates();
		assertTrue(candidates.isEmpty());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"++unusedField;",
			"unusedField++;",
			"--unusedField;",
			"unusedField--;",
	})
	void testIncrementOrDecrementOnUnusedField_shouldBeRemoved(String ecpressionStatementToRemove) throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		options.put("remove-initializers-side-effects", false);

		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);

		String code = "" +
				"	private int unusedField = 0;\n"
				+ "	void methodWithStatementToRemove() {\n"
				+ "		" + ecpressionStatementToRemove + "\n"
				+ "	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> removedUnusedFields = visitor.getUnusedPrivateFields();
		assertEquals(1, removedUnusedFields.size());
		UnusedFieldWrapper unusedFieldWrapper = removedUnusedFields.get(0);
		String removedUnusedFieldName = unusedFieldWrapper.getClassMemberIdentifier();
		assertEquals("unusedField", removedUnusedFieldName);
		List<ExpressionStatement> unusedReassignments = unusedFieldWrapper.getUnusedReassignments();
		assertEquals(1, unusedReassignments.size());
		String actualRemovedAssignment = unusedReassignments.get(0)
			.toString()
			.trim();
		assertEquals(ecpressionStatementToRemove, actualRemovedAssignment);
	}
}

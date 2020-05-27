package eu.jsparrow.core.visitor.make_final;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class FinalInitializerCheckASTVisitorTest extends UsesJDTUnitFixture {

	private FinalInitializerCheckASTVisitor visitor;

	@BeforeEach
	public void setUpVisitor() throws Exception {
//		defaultFixture = fixtureProject.addCompilationUnit(DEFAULT_TYPE_NAME);

		visitor = new FinalInitializerCheckASTVisitor();
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void staticField_initInDeclarationOnly_shouldBeCandidate() throws Exception {
		String typeContent = "private static String a = \"asdf\";" + "static {" + "}" + "public " + DEFAULT_TYPE_DECLARATION_NAME
				+ "() {" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a"));
	}

	@Test
	public void staticField_twoFragments_onlyOneInitializedInDeclaration_shouldNotBeCandidate() throws Exception {
		String typeContent = "private static String a, b = \"asdf\";";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void staticField_initInStaticInitializerOnly_shouldBeCandidate() throws Exception {
		String typeContent = "private static String a;" + "private static String b;" + "static {" + "	a = \"test\";"
				+ "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a"));
	}

	@Test
	public void staticField_initInDeclarationAndStaticInitializer_shouldNotBeCandidate() throws Exception {
		String typeContent = "private static String a = \"asdf\";" + "static {" + "	a = \"jkl\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates, "a"));
	}

	@Test
	public void staticField_twoFragments_firstInitInDecl_secondInitInStaticInitializer_shouldBeCandidate()
			throws Exception {
		String typeContent = "private static String a = \"asdf\", b;" + "static {" + "	b = \"jkl\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a", "b"));
	}

	@Test
	public void staticField_twoFragments_initInDeclarationAndStaticInitializer_shouldBeCandidate() throws Exception {
		String typeContent = "private static String a, b;" + "static {" + "	a = \"asdf\";" + "	b = \"jkl\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a", "b"));
	}

	@Test
	public void nonStaticField_notInitialized_shouldNotBeCandidate() throws Exception {
		String typeContent = "private String a;";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void nonStaticField_initInDeclaration_shouldBeCandidate() throws Exception {
		String typeContent = "public String a = \"asdf\";";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a"));
	}

	@Test
	public void nonStaticField_twoFragments_onlyOneInitializedInDeclaration_shouldNotBeCandidate() throws Exception {
		String typeContent = "public String a, b = \"asdf\";";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void nonStaticField_initInInitializerOnly_shouldBeCandidate() throws Exception {
		String typeContent = "private String a;" + "{" + "	a = \"asdf\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a"));
	}

	@Test
	public void nonStaticField_initInConstructorOnly_shouldBeCandidate() throws Exception {
		String typeContent = "private String a;" + "public " + DEFAULT_TYPE_DECLARATION_NAME + "() {" + "	a = \"asdf\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a"));
	}

	@Test
	public void nonStaticField_initInOneConstructorOnly_shouldNotBeCandidate() throws Exception {
		String typeContent = "private String a;" + "public " + DEFAULT_TYPE_DECLARATION_NAME + "() {" + "	a = \"asdf\";" + "}"
				+ "public " + DEFAULT_TYPE_DECLARATION_NAME + "(String asdf) {" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void nonStaticField_initInAllConstructors_shouldBeCandidate() throws Exception {
		String typeContent = "private String a;" + "public " + DEFAULT_TYPE_DECLARATION_NAME + "() {" + "	a = \"asdf\";" + "}"
				+ "public " + DEFAULT_TYPE_DECLARATION_NAME + "(String asdf) {" + "	a = \"asdf\";" + "}" + "public "
				+ DEFAULT_TYPE_DECLARATION_NAME + "(String asdf, String jkl) {" + "	a = \"asdf\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a"));
	}

	@Test
	public void nonStaticField_initInDeclarationAndInitializer_shouldNotBeCandidate() throws Exception {
		String typeContent = "private String a = \"asdf\";" + "{" + "	a = \"jkl\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void nonStaticField_initInDeclarationConstructorAndInitializer_shouldNotBeCandidate() throws Exception {
		String typeContent = "private String a = \"asdf\";" + "{" + "	a = \"jkl\";" + "}" + "public "
				+ DEFAULT_TYPE_DECLARATION_NAME + "() {" + "	a = \"\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void nonStaticField_accessUsingFieldAccess_initInConstructor_shouldBeCandidate() throws Exception {
		String typeContent = "private String a;" + "public " + DEFAULT_TYPE_DECLARATION_NAME + "() {" + "	this.a = \"asdf\";"
				+ "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a"));
	}

	@Test
	public void nonStaticField_assignedMultipleTimesInInitializer_shouldNotBeCandidate() throws Exception {
		String typeContent = "private String a;" + "{" + "	a = \"asdf\";" + "	a = \"jkl\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void nonStaticField_assignedMultipleTimesInConstructor_shouldNotBeCandidate() throws Exception {
		String typeContent = "private String a;" + "public " + DEFAULT_TYPE_DECLARATION_NAME + "() {" + "	a = \"asdf\";"
				+ "	a = \"jkl\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void nonStaticField_assignedMultipleTimesInMultipleConstructor_shouldNotBeCandidate() throws Exception {
		String typeContent = "private String a;" + "public " + DEFAULT_TYPE_DECLARATION_NAME + "() {" + "	a = \"asdf\";"
				+ "	a = \"jkl\";" + "}" + "public " + DEFAULT_TYPE_DECLARATION_NAME + "(String asdf) {" + "	a = \"asdf\";" + "}"
				+ "public " + DEFAULT_TYPE_DECLARATION_NAME + "(String asdf, String jkl) {" + "	a = \"asdf\";" + "	a = \"jkl\";"
				+ "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);
		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}
	
	@Test
	public void nonStaticField_isAlreadyFinal_shouldNotBeCandidate() throws Exception {
		String typeContent = "private final String a = \"asdf\";"
						   + "public void test() {"
						   + "	System.out.println(a);"
						   + "}";
		
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}
	
	@Test 
	public void nonStaticField_isReassignedInAnonymousClassInConstructor_shouldNotBeCandidate() throws Exception {
		String typeContent = "" +
				"	private boolean reassignedInConstractorInnerClass = false;\n" + 
				"	public " + DEFAULT_TYPE_DECLARATION_NAME +  "() {\n" + 
				"		final Runnable runnable = new Runnable() {\n" + 
				"			@Override\n" + 
				"			public void run() {\n" + 
				"				reassignedInConstractorInnerClass = true;\n" + 
				"			}" + 
				"		};" + 
				"	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		defaultFixture.accept(visitor);
		List<FieldDeclaration> candidates = visitor.getFinalCandidates();
		assertTrue(candidates.isEmpty());
	}
	
	@Test
	public void nonStaticField_initInAllButOneCtor_shouldNotBeCandidate() throws Exception {
		String typeContent = "" +
				"	private boolean value;\n" + 
				"	{" + 
				"		value = true;\n" + 
				"	}" + 
				"	public " + DEFAULT_TYPE_DECLARATION_NAME + "(String value) {" + 
				"	}" + 
				"	public " + DEFAULT_TYPE_DECLARATION_NAME + "(boolean value) {" + 
				"		this.value = value;\n" + 
				"	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		defaultFixture.accept(visitor);
		List<FieldDeclaration> candidates = visitor.getFinalCandidates();
		assertTrue(candidates.isEmpty());
		
	}
	
	@Test
	public void nonStaticField_nestedBlockAssignment_shouldNotBeCandidate() throws Exception {
		String typeContent = "" +
				"	private double doubleValue;\n" + 
				"	public " + DEFAULT_TYPE_DECLARATION_NAME + "(String value) {\n" + 
				"		if(!value.isEmpty()) {\n" + 
				"			doubleValue = value.length();\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public " + DEFAULT_TYPE_DECLARATION_NAME + "(double value) {\n" + 
				"		doubleValue = value;\n" + 
				"	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		defaultFixture.accept(visitor);
		List<FieldDeclaration> candidates = visitor.getFinalCandidates();
		assertTrue(candidates.isEmpty());
	}

	@Test
	public void nonStaticField_nestedBlockAssignmentWithinParentheses_shouldNotBeCandidate() throws Exception {
		String typeContent = "" +
				"	private double doubleValue;\n" + 
				"	public " + DEFAULT_TYPE_DECLARATION_NAME + "(String value) {\n" + 
				"		if(!value.isEmpty()) {\n" + 
				"			double size = (doubleValue = value.length());\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public " + DEFAULT_TYPE_DECLARATION_NAME + "(double value) {\n" + 
				"		doubleValue = value;\n" + 
				"	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		defaultFixture.accept(visitor);
		List<FieldDeclaration> candidates = visitor.getFinalCandidates();
		assertTrue(candidates.isEmpty());
	}

	@Test
	public void staticField_reassigningInConstructor_shouldNotBeCandidate() throws Exception {
		String typeContent = "" + 
				"	private static double DOUBLE_VALUE = 0.0;\n" + 
				"	\n" + 
				"	public " + DEFAULT_TYPE_DECLARATION_NAME + " (double value) {\n" + 
				"		DOUBLE_VALUE = value;\n" + 
				"	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		defaultFixture.accept(visitor);
		List<FieldDeclaration> candidates = visitor.getFinalCandidates();
		assertTrue(candidates.isEmpty());
	}
	
	@Test
	public void nonStaticField_incrementWithPrefixExpression_shouldNotBeCandidate() throws Exception {
		String typeContent = "" + 
				"	private int value = 0;\n" + 
				"	\n" + 
				"	public " + DEFAULT_TYPE_DECLARATION_NAME + " () {\n" + 
				"		value++;\n" + 
				"	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		defaultFixture.accept(visitor);
		List<FieldDeclaration> candidates = visitor.getFinalCandidates();
		assertTrue(candidates.isEmpty());
	}

	private boolean isValidCandidates(List<FieldDeclaration> candidates, String... correctFieldNames) {
		if (candidates.isEmpty()) {
			return false;
		}

		List<String> correctFieldNamesList = new LinkedList<>(Arrays.asList(correctFieldNames));

		int variableDeclarationFragmentCount = candidates.stream()
			.mapToInt(field -> field.fragments()
				.size())
			.sum();

		if (variableDeclarationFragmentCount != correctFieldNames.length) {
			return false;
		}

		for (FieldDeclaration candidate : candidates) {
			List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(candidate.fragments(),
					VariableDeclarationFragment.class);

			for (VariableDeclarationFragment fragment : fragments) {
				String name = fragment.getName()
					.getIdentifier();

				correctFieldNamesList.remove(name);
			}
		}

		return correctFieldNamesList.isEmpty();
	}
}

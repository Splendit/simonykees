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
import eu.jsparrow.jdtunit.JdtUnitFixtureClass;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

@SuppressWarnings("nls")
public class FinalInitializerCheckASTVisitorTest extends UsesJDTUnitFixture {

	private static final String DEFAULT_TYPE_NAME = "TestCU";

	private FinalInitializerCheckASTVisitor visitor;
	private JdtUnitFixtureClass defaultFixture;

	@BeforeEach
	public void setUp() throws Exception {
		defaultFixture = fixtureProject.addCompilationUnit(DEFAULT_TYPE_NAME);

		visitor = new FinalInitializerCheckASTVisitor();
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void staticField_initInDeclarationOnly_shouldBeCandidate() throws Exception {
		String typeContent = "private static String a = \"asdf\";" + "static {" + "}" + "public " + DEFAULT_TYPE_NAME
				+ "() {" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a"));
	}

	@Test
	public void staticField_twoFragments_onlyOneInitializedInDeclaration_shouldNotBeCandidate() throws Exception {
		String typeContent = "private static String a, b = \"asdf\";";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void staticField_initInStaticInitializerOnly_shouldBeCandidate() throws Exception {
		String typeContent = "private static String a;" + "private static String b;" + "static {" + "	a = \"test\";"
				+ "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a"));
	}

	@Test
	public void staticField_initInDeclarationAndStaticInitializer_shouldNotBeCandidate() throws Exception {
		String typeContent = "private static String a = \"asdf\";" + "static {" + "	a = \"jkl\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates, "a"));
	}

	@Test
	public void staticField_twoFragments_firstInitInDecl_secondInitInStaticInitializer_shouldBeCandidate()
			throws Exception {
		String typeContent = "private static String a = \"asdf\", b;" + "static {" + "	b = \"jkl\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a", "b"));
	}

	@Test
	public void staticField_twoFragments_initInDeclarationAndStaticInitializer_shouldBeCandidate() throws Exception {
		String typeContent = "private static String a, b;" + "static {" + "	a = \"asdf\";" + "	b = \"jkl\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a", "b"));
	}

	@Test
	public void nonStaticField_notInitialized_shouldNotBeCandidate() throws Exception {
		String typeContent = "private String a;";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void nonStaticField_initInDeclaration_shouldBeCandidate() throws Exception {
		String typeContent = "public String a = \"asdf\";";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a"));
	}

	@Test
	public void nonStaticField_twoFragments_onlyOneInitializedInDeclaration_shouldNotBeCandidate() throws Exception {
		String typeContent = "public String a, b = \"asdf\";";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void nonStaticField_initInInitializerOnly_shouldBeCandidate() throws Exception {
		String typeContent = "private String a;" + "{" + "	a = \"asdf\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a"));
	}

	@Test
	public void nonStaticField_initInConstructorOnly_shouldBeCandidate() throws Exception {
		String typeContent = "private String a;" + "public " + DEFAULT_TYPE_NAME + "() {" + "	a = \"asdf\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a"));
	}

	@Test
	public void nonStaticField_initInOneConstructorOnly_shouldNotBeCandidate() throws Exception {
		String typeContent = "private String a;" + "public " + DEFAULT_TYPE_NAME + "() {" + "	a = \"asdf\";" + "}"
				+ "public " + DEFAULT_TYPE_NAME + "(String asdf) {" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void nonStaticField_initInAllConstructors_shouldBeCandidate() throws Exception {
		String typeContent = "private String a;" + "public " + DEFAULT_TYPE_NAME + "() {" + "	a = \"asdf\";" + "}"
				+ "public " + DEFAULT_TYPE_NAME + "(String asdf) {" + "	a = \"asdf\";" + "}" + "public "
				+ DEFAULT_TYPE_NAME + "(String asdf, String jkl) {" + "	a = \"asdf\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a"));
	}

	@Test
	public void nonStaticField_initInDeclarationAndInitializer_shouldNotBeCandidate() throws Exception {
		String typeContent = "private String a = \"asdf\";" + "{" + "	a = \"jkl\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void nonStaticField_initInDeclarationConstructorAndInitializer_shouldNotBeCandidate() throws Exception {
		String typeContent = "private String a = \"asdf\";" + "{" + "	a = \"jkl\";" + "}" + "public "
				+ DEFAULT_TYPE_NAME + "() {" + "	a = \"\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void nonStaticField_accessUsingFieldAccess_initInConstructor_shouldBeCandidate() throws Exception {
		String typeContent = "private String a;" + "public " + DEFAULT_TYPE_NAME + "() {" + "	this.a = \"asdf\";"
				+ "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertTrue(isValidCandidates(candidates, "a"));
	}

	@Test
	public void nonStaticField_assignedMultipleTimesInInitializer_shouldNotBeCandidate() throws Exception {
		String typeContent = "private String a;" + "{" + "	a = \"asdf\";" + "	a = \"jkl\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void nonStaticField_assignedMultipleTimesInConstructor_shouldNotBeCandidate() throws Exception {
		String typeContent = "private String a;" + "public " + DEFAULT_TYPE_NAME + "() {" + "	a = \"asdf\";"
				+ "	a = \"jkl\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
	}

	@Test
	public void nonStaticField_assignedMultipleTimesInMultipleConstructor_shouldNotBeCandidate() throws Exception {
		String typeContent = "private String a;" + "public " + DEFAULT_TYPE_NAME + "() {" + "	a = \"asdf\";"
				+ "	a = \"jkl\";" + "}" + "public " + DEFAULT_TYPE_NAME + "(String asdf) {" + "	a = \"asdf\";" + "}"
				+ "public " + DEFAULT_TYPE_NAME + "(String asdf, String jkl) {" + "	a = \"asdf\";" + "	a = \"jkl\";"
				+ "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

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
		
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<FieldDeclaration> candidates = visitor.getFinalCandidates();

		assertFalse(isValidCandidates(candidates));
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

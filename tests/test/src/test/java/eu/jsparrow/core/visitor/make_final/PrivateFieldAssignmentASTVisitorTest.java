package eu.jsparrow.core.visitor.make_final;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

public class PrivateFieldAssignmentASTVisitorTest extends UsesJDTUnitFixture {

	private PrivateFieldAssignmentASTVisitor visitor;

	@BeforeEach
	public void setUp() throws Exception {
		visitor = new PrivateFieldAssignmentASTVisitor(defaultFixture.getTypeDeclaration());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void test_assignment_1() throws Exception {
		String typeContent = "private String a, b, c, d;" + "public void test1() {" + "	a = \"asdf\";"
				+ "	b = \"jkl\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<VariableDeclarationFragment> assignedFragments = visitor.getAssignedVariableDeclarationFragments();

		assertTrue(checkAssignedFragments(assignedFragments, "a", "b"));
	}

	@Test
	public void test_assignment_2() throws Exception {
		String typeContent = "private String a;" + "public void test1() {" + "	a = \"asdf\";" + "}"
				+ "public void test2() {" + "	a = \"jkl\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<VariableDeclarationFragment> assignedFragments = visitor.getAssignedVariableDeclarationFragments();

		assertTrue(checkAssignedFragments(assignedFragments, "a"));
	}

	@Test
	public void test_assignment_3() throws Exception {
		String typeContent = "private String a, b, c;" + "public void test1() {" + "	System.out.println(a);" + "}"
				+ "public void test2() {" + "	System.out.println(b);" + "}" + "public void test3() {"
				+ "	System.out.println(c);" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<VariableDeclarationFragment> assignedFragments = visitor.getAssignedVariableDeclarationFragments();

		assertTrue(checkAssignedFragments(assignedFragments));
	}

	@Test
	public void test_prefix_1() throws Exception {
		String typeContent = "private int a, b, c;" + "public void test1() {" + "	++a;" + "	--b;" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<VariableDeclarationFragment> assignedFragments = visitor.getAssignedVariableDeclarationFragments();

		assertTrue(checkAssignedFragments(assignedFragments, "a", "b"));
	}

	@Test
	public void test_prefix_2() throws Exception {
		String typeContent = "private int a, b, c, d, e;" + "public void test1() {" + "	System.out.println(+a);"
				+ "	System.out.println(~b);" + "	System.out.println(-c);" + "	System.out.println(!d);"
				+ "	System.out.println(++e);" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<VariableDeclarationFragment> assignedFragments = visitor.getAssignedVariableDeclarationFragments();

		assertTrue(checkAssignedFragments(assignedFragments, "e"));
	}

	@Test
	public void test_postfix_1() throws Exception {
		String typeContent = "private int a, b, c;" + "public void test1() {" + "	a++;" + "	b--;"
				+ "	System.out.println(c);" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<VariableDeclarationFragment> assignedFragments = visitor.getAssignedVariableDeclarationFragments();

		assertTrue(checkAssignedFragments(assignedFragments, "a", "b"));
	}
	
	@Test
	public void test_initializerInMethodBody_shouldFindAssignment() throws Exception {
		String typeContent = "" +
				"	private int intValue = 0;\n" + 
				"	\n" + 
				"	private void sampleMethod() {\n" + 
				"		final Runnable updateIntValue = new Runnable() {\n" + 
				"			public void run() {\n" + 
				"				final Runnable r = new Runnable() {\n" + 
				"					\n" + 
				"					{\n" + 
				"						intValue = 1;\n" + 
				"					}\n" + 
				"					\n" + 
				"					public void run() {}\n" + 
				"				};\n" + 
				"			}\n" + 
				"		};\n" + 
				"	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<VariableDeclarationFragment> assignedFragments = visitor.getAssignedVariableDeclarationFragments();
		assertTrue(checkAssignedFragments(assignedFragments, "intValue"));
	}

	@Test
	public void test_reassignInnerInnerFieldInOuterConstructor_shouldFindAssignment() throws Exception {
		String typeContent = "" +
				"public static class InnerClassWithConstructor {\n" + 
				"	InnerClassWithConstructor(){\n" + 
				"		InnerClassWithConstructor.InnerInnerClass xInnerInnerClass = new InnerClassWithConstructor.InnerInnerClass();\n" + 
				"		xInnerInnerClass.intValue = 1;\n" + 
				"	}\n" + 
				"	public static class InnerInnerClass {\n" + 
				"		private int intValue = 0;\n" + 
				"	}\n" + 
				"}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<VariableDeclarationFragment> assignedFragments = visitor.getAssignedVariableDeclarationFragments();
		assertTrue(checkAssignedFragments(assignedFragments, "intValue"));
	}

	@Test
	public void test_reassignInnerInnerFieldInRootClassMethod_shouldFindAssignment() throws Exception {
		
		String typeContent = "" +
				"public static class InnerClassWithConstructor {\n" + 
				"	public static class InnerInnerClass {\n" + 
				"		private int intValue = 0;\n" + 
				"	}\n" + 
				"}\n" + 
				"private void sampleMethod() {\n" + 
				"	final InnerClassWithConstructor.InnerInnerClass xInnerInnerClass = new InnerClassWithConstructor.InnerInnerClass();\n" + 
				"	xInnerInnerClass.intValue = 1;\n" + 
				"}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		defaultFixture.accept(visitor);

		List<VariableDeclarationFragment> assignedFragments = visitor.getAssignedVariableDeclarationFragments();
		assertTrue(checkAssignedFragments(assignedFragments, "intValue"));
	}


	private boolean checkAssignedFragments(List<VariableDeclarationFragment> assignedFragments,
			String... correctFragmentNames) {
		if (assignedFragments.size() != correctFragmentNames.length) {
			return false;
		}

		List<String> correctFragmentNamesList = new ArrayList<>(Arrays.asList(correctFragmentNames));

		return assignedFragments.stream()
			.map(VariableDeclarationFragment::getName)
			.map(SimpleName::getIdentifier)
			.allMatch(correctFragmentNamesList::contains);
	}
}

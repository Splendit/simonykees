package eu.jsparrow.core.visitor.functionalinterface;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.JdtUnitFixtureClass;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings("nls")
public class FunctionalInterfaceASTVisitorTest extends UsesJDTUnitFixture {

	private static final String DEFAULT_TYPE_NAME = "TestForSim1709";

	private JdtUnitFixtureClass defaultFixture;

	private FunctionalInterfaceASTVisitor functionalInterfaceASTVisitor;

	@BeforeEach
	public void setUp() throws Exception {
		defaultFixture = fixtureProject.addCompilationUnit(DEFAULT_TYPE_NAME);
		functionalInterfaceASTVisitor = new FunctionalInterfaceASTVisitor();
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();

	}
	
	
	/*
	 * needed for coverage -- transformed although not compiler clean
	 */
	@Test
	public void visit_AnonymousWithNonBoundSimpleName_ShouldTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		InterfaceForSim1709 anonymous = new InterfaceForSim1709() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return INTERFACE_CONSTANT;\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		InterfaceForSim1709 anonymous = () -> {\n" +
				"			return INTERFACE_CONSTANT;" +
				"		};\n" +
				"	}";

		assertCodeChanged(original, expected);

	}


	/*
	 * needed for coverage -- transformed although not compiler clean
	 */
	@Test
	public void visit_AnonymousWithQualifiedInterfaceName_ShouldTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"   	static final int INTERFACE_CONSTANT = -20;\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		fixturepackage.TestForSim1709.InterfaceForSim1709 anonymous = " +
				"		new fixturepackage.TestForSim1709.InterfaceForSim1709() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return INTERFACE_CONSTANT;\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"   	static final int INTERFACE_CONSTANT = -20;\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		fixturepackage.TestForSim1709.InterfaceForSim1709 anonymous = () -> {\n" +
				"			return fixturepackage.TestForSim1709.InterfaceForSim1709.INTERFACE_CONSTANT;" +
				"		};\n" +
				"	}";

		assertCodeChanged(original, expected);

	}

	@Test
	public void visit_AnonymousWithStaticMethodInvocation_ShouldTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	static int exampleStaticMethod() {\n" +
				"		return 0;\n" +
				"	}" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		InterfaceForSim1709 anonymous = new InterfaceForSim1709() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return exampleStaticMethod();\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	static int exampleStaticMethod() {\n" +
				"		return 0;\n" +
				"	}" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		InterfaceForSim1709 anonymous = () -> {\n" +
				"			return exampleStaticMethod();" +
				"		};\n" +
				"	}";

		assertCodeChanged(original, expected);

	}

	@Test
	public void visit_AnonymousWithLocalVariable_ShouldTransform() throws Exception {
		String original = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_WithLocalVariables() {\n" +
				"		int x = 1;\n" +
				"		InterfaceForSim1709 anonymous = new InterfaceForSim1709() {\n" +
				"\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return x;\n" +
				"			}\n" +
				"\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_WithLocalVariables() {\n" +
				"		int x = 1;\n" +
				"		InterfaceForSim1709 anonymous = () -> {\n" +
				"			return x;\n" +
				"		};\n" +
				"	}";

		assertCodeChanged(original, expected);

	}

	@Test
	public void visit_AnonymousWithQualifiedConstant_ShouldTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		static final int INTERFACE_CONSTANT = -20;\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		InterfaceForSim1709 anonymous = new InterfaceForSim1709() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return InterfaceForSim1709.INTERFACE_CONSTANT;\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		static final int INTERFACE_CONSTANT = -20;\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		InterfaceForSim1709 anonymous = () -> {\n" +
				"			return InterfaceForSim1709.INTERFACE_CONSTANT;" +
				"		};\n" +
				"	}";

		assertCodeChanged(original, expected);

	}

	@Test
	public void visit_AnonymousWithInterfaceConstant_ShouldTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		static final int INTERFACE_CONSTANT = -20;\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		InterfaceForSim1709 anonymous = new InterfaceForSim1709() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return INTERFACE_CONSTANT;\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		static final int INTERFACE_CONSTANT = -20;\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		InterfaceForSim1709 anonymous = () -> {\n" +
				"			return InterfaceForSim1709.INTERFACE_CONSTANT;" +
				"		};\n" +
				"	}";

		assertCodeChanged(original, expected);

	}

	@Test
	public void visit_AnonymousWithSuperInterfaceConstant_ShouldTransform() throws Exception {

		String original = "" +
				"	static interface SuperInterfaceForSim1709 {\n" +
				"		static final int SUPER_INTERFACE_CONSTANT = -20;\n" +
				"	}\n" +
				"	static interface InterfaceForSim1709 extends SuperInterfaceForSim1709 {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		InterfaceForSim1709 anonymous = new InterfaceForSim1709() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return SUPER_INTERFACE_CONSTANT;\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	static interface SuperInterfaceForSim1709 {\n" +
				"		static final int SUPER_INTERFACE_CONSTANT=-20;\n" +
				"	}\n" +
				"	static interface InterfaceForSim1709 extends SuperInterfaceForSim1709 {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant(){\n" +
				"		InterfaceForSim1709 anonymous=() -> {\n" +
				"			return InterfaceForSim1709.SUPER_INTERFACE_CONSTANT;\n" +
				"		};\n" +
				"	}";

		assertCodeChanged(original, expected);

	}

	@Test
	public void visit_AnonymousWithAmbiguousConstant_ShouldTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		static final int AMBIGUOUS_CONSTANT = 20;\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public static final int AMBIGUOUS_CONSTANT = -20;\n" +
				"	public void test_NonQualified_AmbiguousConstant() {\n" +
				"		InterfaceForSim1709 anonymous = new InterfaceForSim1709() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return AMBIGUOUS_CONSTANT;\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		static final int AMBIGUOUS_CONSTANT = 20;\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public static final int AMBIGUOUS_CONSTANT = -20;\n" +
				"	public void test_NonQualified_AmbiguousConstant() {\n" +
				"		InterfaceForSim1709 anonymous = () -> {\n" +
				"			return InterfaceForSim1709.AMBIGUOUS_CONSTANT;" +
				"		};\n" +
				"	}";

		assertCodeChanged(original, expected);

	}

	@Test
	public void visit_AnonymousWithClassConstant_ShouldTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public static final int CLASS_CONSTANT = -30;" +
				"	public void test_NonQualified_ClassConstant() {\n" +
				"		InterfaceForSim1709 anonymous = new InterfaceForSim1709() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return CLASS_CONSTANT;\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public static final int CLASS_CONSTANT = -30;" +
				"	public void test_NonQualified_ClassConstant() {\n" +
				"		InterfaceForSim1709 anonymous = () -> {\n" +
				"			return CLASS_CONSTANT;" +
				"		};\n" +
				"	}";

		assertCodeChanged(original, expected);

	}

	@Test
	public void visit_AnonymousWithInstanceField_ShouldNotTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_WithInstanceField() {\n" +
				"\n" +
				"		InterfaceForSim1709 anonymous = new InterfaceForSim1709() {\n" +
				"			int x = 1;\n" +
				"\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return x;\n" +
				"			}\n" +
				"\n" +
				"		};\n" +
				"	}";

		assertCodeNotChanged(original);
	}

	private void assertCodeChanged(String original, String expected) throws Exception {
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, original);

		functionalInterfaceASTVisitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(functionalInterfaceASTVisitor);

		assertMatch(ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_NAME, expected),
				defaultFixture.getTypeDeclaration());
	}

	private void assertCodeNotChanged(String original) throws Exception {
		assertCodeChanged(original, original);
	}

}

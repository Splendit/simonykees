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

		checkMatch(original, expected);

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
				"			return SuperInterfaceForSim1709.SUPER_INTERFACE_CONSTANT;\n" + 
				"		};\n" + 
				"	}";

		checkMatch(original, expected);

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

		checkMatch(original, expected);

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

		checkMatch(original, expected);

	}


	private void checkMatch(String actual, String expected) throws Exception {
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_NAME, actual);

		functionalInterfaceASTVisitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(functionalInterfaceASTVisitor);

		assertMatch(ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_NAME, expected),
				defaultFixture.getTypeDeclaration());
	}

}

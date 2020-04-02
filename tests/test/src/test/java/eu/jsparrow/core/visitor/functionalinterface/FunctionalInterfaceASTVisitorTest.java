package eu.jsparrow.core.visitor.functionalinterface;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

@SuppressWarnings("nls")
public class FunctionalInterfaceASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new FunctionalInterfaceASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

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

		assertChange(original, expected);
	}

	@Test
	public void visit_AnonymousWithFullyQualifiedInterfaceName_ShouldTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"   	static final int INTERFACE_CONSTANT = -20;\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		fixturepackage.TestCU.InterfaceForSim1709 anonymous = " +
				"		new fixturepackage.TestCU.InterfaceForSim1709() {\n" +
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
				"		fixturepackage.TestCU.InterfaceForSim1709 anonymous = () -> {\n" +
				"			return fixturepackage.TestCU.InterfaceForSim1709.INTERFACE_CONSTANT;" +
				"		};\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AnonymousWithPartlyQualifiedInterfaceName_ShouldTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"   	static final int INTERFACE_CONSTANT = -20;\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		TestCU.InterfaceForSim1709 anonymous = " +
				"		new TestCU.InterfaceForSim1709() {\n" +
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
				"		TestCU.InterfaceForSim1709 anonymous = () -> {\n" +
				"			return TestCU.InterfaceForSim1709.INTERFACE_CONSTANT;" +
				"		};\n" +
				"	}";

		assertChange(original, expected);
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

		assertChange(original, expected);
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

		assertChange(original, expected);
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

		assertChange(original, expected);
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

		assertChange(original, expected);
	}

	@Test
	public void visit_AnonymousWithThisInterfaceConstant_ShouldTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		static final int INTERFACE_CONSTANT = -20;\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		InterfaceForSim1709 anonymous = new InterfaceForSim1709() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return this.INTERFACE_CONSTANT;\n" +
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

		assertChange(original, expected);
	}

	@Test
	public void visit_AnonymousWithIntegerMaxValue_ShouldTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		InterfaceForSim1709 anonymous = new InterfaceForSim1709() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return Integer.valueOf(1).MAX_VALUE;\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	static interface InterfaceForSim1709 {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_InnerInterfaceConstant() {\n" +
				"		InterfaceForSim1709 anonymous = () -> {\n" +
				"			return Integer.valueOf(1).MAX_VALUE;" +
				"		};\n" +
				"	}";

		assertChange(original, expected);
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

		assertChange(original, expected);
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

		assertChange(original, expected);
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

		assertChange(original, expected);
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

		assertNoChange(original);
	}

	@Test
	public void visit_AnonymousWithTypeArguments_ShouldTransform() throws Exception {

		String original = "" +
				"	static interface GenericInterface<T> {\n" +
				"		String INTERFACECONSTANT = \"interface-constant\";" +
				"		T getValue();\n" +
				"	}" +
				"	public void test_GenericInterface() {\n" +
				"		GenericInterface<String> genericInterface = new GenericInterface<String>() {\n" +
				"			@Override\n" +
				"			public String getValue() {\n" +
				"				return INTERFACECONSTANT;" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	static interface GenericInterface<T> {\n" +
				"		String INTERFACECONSTANT = \"interface-constant\";" +				
				"		T getValue();\n" +
				"	}" +
				"	public void test_GenericInterface() {\n" +
				"		GenericInterface<String> genericInterface = () -> {\n" +
				"			return GenericInterface.INTERFACECONSTANT;" +
				"		};\n" +
				"	}";

		assertChange(original, expected);
	}
}

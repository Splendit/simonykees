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
	public void visit_AnonymousCallingNonQualifiedDefaultMethod_ShouldNotTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceWithDefaultMethod {\n" +
				"		default int getIntOne() {\n" +
				"			return 1;\n" +
				"		}\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_DefaultMethodInvocation() {\n" +
				"		InterfaceWithDefaultMethod anonymous = new InterfaceWithDefaultMethod() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return getIntOne();\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AnonymousCallingThisDefaultMethod_ShouldNotTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceWithDefaultMethod {\n" +
				"		default int getIntOne() {\n" +
				"			return 1;\n" +
				"		}\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test_NonQualified_DefaultMethodInvocation() {\n" +
				"		InterfaceWithDefaultMethod anonymous = new InterfaceWithDefaultMethod() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return this.getIntOne();\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AnonymousCallingNonQualifiedHashCode_ShouldTransform() throws Exception {

		String original = "" +
				"	public interface InterfaceWithGetIntMethod {\n" +
				"		int getInt();\n" +
				"	}\n" +
				"	public void test_NonQualified_HashCodeInvocation() {\n" +
				"		InterfaceWithGetIntMethod anonymous = new InterfaceWithGetIntMethod() {\n" +
				"			@Override\n" +
				"			public int getInt() {\n" +
				"				return hashCode();\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	public interface InterfaceWithGetIntMethod {\n" +
				"		int getInt();\n" +
				"	}\n" +
				"	public void test_NonQualified_HashCodeInvocation() {\n" +
				"		InterfaceWithGetIntMethod anonymous = () -> {\n" +
				"				return hashCode();\n" +
				"		};\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AnonymousCallingThisHashCode_ShouldTransform() throws Exception {

		String original = "" +
				"	public interface InterfaceWithGetIntMethod {\n" +
				"		int getInt();\n" +
				"	}\n" +
				"	public void test_NonQualified_HashCodeInvocation() {\n" +
				"		InterfaceWithGetIntMethod anonymous = new InterfaceWithGetIntMethod() {\n" +
				"			@Override\n" +
				"			public int getInt() {\n" +
				"				return this.hashCode();\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	public interface InterfaceWithGetIntMethod {\n" +
				"		int getInt();\n" +
				"	}\n" +
				"	public void test_NonQualified_HashCodeInvocation() {\n" +
				"		InterfaceWithGetIntMethod anonymous = () -> {\n" +
				"				return this.hashCode();\n" +
				"		};\n" +
				"	}";

		assertChange(original, expected);
	}
	
	@Test
	public void visit_AnonymousCallingObjectHashCode_ShouldTransform() throws Exception {

		String original = "" +
				"	public interface InterfaceWithGetIntMethod {\n" +
				"		int getInt();\n" +
				"	}\n" +
				"	public void test_NonQualified_HashCodeInvocation() {\n" +
				"		InterfaceWithGetIntMethod anonymous = new InterfaceWithGetIntMethod() {\n" +
				"			@Override\n" +
				"			public int getInt() {\n" +
				"               Object o = new Object();\n" +
				"				return o.hashCode();\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	public interface InterfaceWithGetIntMethod {\n" +
				"		int getInt();\n" +
				"	}\n" +
				"	public void test_NonQualified_HashCodeInvocation() {\n" +
				"		InterfaceWithGetIntMethod anonymous = () -> {\n" +
				"               Object o = new Object();\n" +
				"				return o.hashCode();\n" +
				"		};\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AnonymousCallingDefaultMethodWithQualifiedThis_ShouldTransform() throws Exception {

		String original = "" +
				"	public interface InterfaceWithGetIntMethod {\n" +
				"		int getInt();\n" +
				"	}\n" +
				"   public int getAnotherInt() {\n"	+ 
				"		return 10;\n" +
				"   }\n" +
				"	public void test() {\n" +
				"		InterfaceWithGetIntMethod anonymous = new InterfaceWithGetIntMethod() {\n" +
				"			@Override\n" +
				"			public int getInt() {\n" +
				"				return TestForSim1709.this.getAnotherInt();\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	public interface InterfaceWithGetIntMethod {\n" +
				"		int getInt();\n" +
				"	}\n" +
				"   public int getAnotherInt() {\n"	+ 
				"		return 10;\n" +
				"   }\n" +				
				"	public void test() {\n" +
				"		InterfaceWithGetIntMethod anonymous = () -> {\n" +
				"				return TestForSim1709.this.getAnotherInt();\n" +
				"		};\n" +
				"	}";

		assertChange(original, expected);
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

	@Test
	public void visit_AnonymousWithNameQualifiedType_ShouldTransform() throws Exception {
		defaultFixture.addImport(java.lang.annotation.Target.class.getName());
		String original = "" +
				"	@Target(value = { java.lang.annotation.ElementType.TYPE_USE })\n" + 
				"	@interface ExampleAnnotation {\n" + 
				"	}\n" + 
				"	interface EnclosingInterface {\n" + 
				"		interface InnerInterface {\n" + 
				"			String INNER_INTERFACE_CONSTANT = \"inner-interface-constant\";\n" + 
				"			void exampleMethod();\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public void test_NameQualifiedType() {\n" + 
				"		EnclosingInterface.@ExampleAnnotation InnerInterface xInnerInterface = new EnclosingInterface.@ExampleAnnotation InnerInterface() {\n" + 
				"			@Override\n" + 
				"			public void exampleMethod() {\n" + 
				"				System.out.println(INNER_INTERFACE_CONSTANT);\n" + 
				"			}\n" + 
				"		};\n" + 
				"	}";
		String expected = "" +
				"	@Target(value = { java.lang.annotation.ElementType.TYPE_USE })\n" + 
				"	@interface ExampleAnnotation {\n" + 
				"	}\n" + 
				"	interface EnclosingInterface {\n" + 
				"		interface InnerInterface {\n" + 
				"			String INNER_INTERFACE_CONSTANT = \"inner-interface-constant\";\n" + 
				"			void exampleMethod();\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public void test_NameQualifiedType(){\n" + 
				"		EnclosingInterface.@ExampleAnnotation InnerInterface xInnerInterface=() -> {\n" + 
				"			System.out.println(EnclosingInterface.InnerInterface.INNER_INTERFACE_CONSTANT);\n" + 
				"		};\n" + 
				"	}";
		assertChange(original, expected);
	}
	
	@Test
	public void visit_innerClassFieldAccess_shouldTransform() throws Exception {
		String original = "" +
				"interface Foo {\n" + 
				"	Runnable sampleMethod();\n" + 
				"}\n" + 
				"public void foo() {\n" + 
				"	Foo foo = new Foo () {\n" + 
				"		public Runnable sampleMethod() {\n" + 
				"			return new Runnable() {\n" + 
				"				String string = \"\";\n" + 
				"				@Override\n" + 
				"				public void run() {\n" + 
				"					System.out.println(this.string);\n" + 
				"					\n" + 
				"				}\n" + 
				"			};\n" + 
				"		}\n" + 
				"	};\n" + 
				"}";
		String expected = "" +
				"interface Foo {\n" + 
				"	Runnable sampleMethod();\n" + 
				"}\n" + 
				"public void foo() {\n" + 
				"	Foo foo = () -> {\n" + 
				"		return new Runnable() {\n" + 
				"			String string = \"\";\n" + 
				"			@Override\n" + 
				"			public void run() {\n" + 
				"				System.out.println(this.string);\n" + 
				"			}\n" + 
				"		};\n" + 
				"	};\n" + 
				"}";
		assertChange(original, expected);
		
	}
}

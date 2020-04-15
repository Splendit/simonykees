package eu.jsparrow.core.visitor.functionalinterface;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

@SuppressWarnings("nls")
public class FunctionalInterfaceASTVisitorMethodsTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new FunctionalInterfaceASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_NonQualifiedDefaultMethodInvocation_ShouldNotTransform() throws Exception {

		String original = "" +
				"	static interface InterfaceWithDefaultMethod {\n" +
				"		default int getIntOne() {\n" +
				"			return 1;\n" +
				"		}\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test() {\n" +
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
	public void visit_ThisDefaultMethodInvocation_ShouldNotTransform() throws Exception {

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
	public void visit_NonQualifiedHashCodeInvocation_ShouldTransform() throws Exception {

		String original = "" +
				"	public interface InterfaceWithGetIntMethod {\n" +
				"		int getInt();\n" +
				"	}\n" +
				"	public void test() {\n" +
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
				"	public void test() {\n" +
				"		InterfaceWithGetIntMethod anonymous = () -> {\n" +
				"				return hashCode();\n" +
				"		};\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_ThisHashCodeInvocation_ShouldTransform() throws Exception {

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
	public void visit_ObjectHashCodeInvocation_ShouldTransform() throws Exception {

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
	public void visit_InvocationWithQualifiedThis_ShouldTransform() throws Exception {

		String original = "" +
				"	public interface InterfaceWithGetIntMethod {\n" +
				"		int getInt();\n" +
				"	}\n" +
				"   public int getInt() {\n" +
				"		return 10;\n" +
				"   }\n" +
				"	public void test() {\n" +
				"		InterfaceWithGetIntMethod anonymous = new InterfaceWithGetIntMethod() {\n" +
				"			@Override\n" +
				"			public int getInt() {\n" +
				"				return TestForSim1709.this.getInt();\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
				"	public interface InterfaceWithGetIntMethod {\n" +
				"		int getInt();\n" +
				"	}\n" +
				"   public int getInt() {\n" +
				"		return 10;\n" +
				"   }\n" +
				"	public void test() {\n" +
				"		InterfaceWithGetIntMethod anonymous = () -> {\n" +
				"				return TestForSim1709.this.getInt();\n" +
				"		};\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_UnqualifiedMethodOfOtherClass_ShouldTransform() throws Exception {
		String original = "" + //
				"	interface InterfaceWithExampleMethod {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	private int exampleMethodOfTestCU() {\n" +
				"		return 1;\n" +
				"	}\n" +
				"	public void test() {\n" +
				"		InterfaceWithExampleMethod anonymous = new InterfaceWithExampleMethod() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return exampleMethodOfTestCU();\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" + //
				"	interface InterfaceWithExampleMethod {\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	private int exampleMethodOfTestCU() {\n" +
				"		return 1;\n" +
				"	}\n" +
				"	public void test() {\n" +
				"		InterfaceWithExampleMethod anonymous = () -> {\n" +
				"			return exampleMethodOfTestCU();\n" +
				"		};\n" +
				"	}";
		assertChange(original, expected);
	}

	@Test
	public void visit_CallDefaultMethodInNestedAnonymousClass_ShouldTransform() throws Exception {
		String original = "" + //
				"	interface InterfaceWithDefaultGetIntMethod {\n" +
				"		default int getInt() {\n" +
				"			return 1;\n" +
				"		}\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test() {\n" +
				"		InterfaceWithDefaultGetIntMethod anonymous = new InterfaceWithDefaultGetIntMethod() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				int x = new InterfaceWithDefaultGetIntMethod() {\n" +
				"					@Override\n" +
				"					public int exampleMethod() {\n" +
				"						return getInt();\n" +
				"					}\n" +
				"				}.exampleMethod();\n" +
				"				return x;\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" + //
				"	interface InterfaceWithDefaultGetIntMethod {\n" +
				"		default int getInt() {\n" +
				"			return 1;\n" +
				"		}\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test() {\n" +
				"		InterfaceWithDefaultGetIntMethod anonymous = () -> {\n" +
				"			int x = new InterfaceWithDefaultGetIntMethod() {\n" +
				"				@Override\n" +
				"				public int exampleMethod() {\n" +
				"					return getInt();\n" +
				"				}\n" +
				"			}.exampleMethod();\n" +
				"			return x;\n" +
				"		};\n" +
				"	}";
		assertChange(original, expected);
	}

	@Test
	public void visit_CallDefaultMethodInGenericAnonymousClass_ShouldNotTransform() throws Exception {
		String original = "" + //
				"	interface GenericInterfaceWithDefaultMethod<T> {\n" +
				"		default String getString() {\n" +
				"			return \"X\";\n" +
				"		}\n" +
				"		T exampleMethod();\n" +
				"	}\n" +
				"	public void test_QualifierThis_GenericInterfaceDefaultMethodInvocation() {\n" +
				"		GenericInterfaceWithDefaultMethod<String> anonymous = new GenericInterfaceWithDefaultMethod<String>() {\n"
				+
				"			@Override\n" +
				"			public String exampleMethod() {\n" +
				"				return this.getString();\n" +
				"			}\n" +
				"		};\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	public void visit_RecursiveExampleMethodInvocation_shouldNotTransform() throws Exception {
		String original = "" + //
				"	interface InterfaceWithExampleMethod{\n" + 
				"		int exampleMethod();\n" + 
				"	}\n" + 
				"	public void test() {\n" + 
				"		InterfaceWithExampleMethod interfaceWithExampleMethod = new InterfaceWithExampleMethod() {\n" + 
				"			@Override\n" + 
				"			public int exampleMethod() {\n" + 
				"				return exampleMethod();\n" + 
				"			}\n" + 
				"		};\n" + 
				"	}";

		assertNoChange(original);
	}

}

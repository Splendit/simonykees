package eu.jsparrow.core.visitor.functionalinterface;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

@SuppressWarnings("nls")
public class FunctionalInterfaceDefaultMethodsASTVisitorTest extends UsesJDTUnitFixture {

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
	public void visit_CallDefaultMethodInNestedRunnable_ShouldTransformOnlyInner() throws Exception {
		String original = "" +
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
				"				Runnable runnable = new Runnable() {\n" +
				"					@Override\n" +
				"					public void run() {\n" +
				"						int x = getInt();\n" +
				"					}\n" +
				"				};\n" +
				"				return 1;\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" +
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
				"				Runnable runnable = () -> {\n" +
				"					int x = getInt();\n" +
				"				};\n" +
				"				return 1;\n" +
				"			}\n" +
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
	public void visit_HashCodeInvocation_shouldTransform() throws Exception {
		String original = "" + //
				"	interface InterfaceWithExampleMethod{\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test() {\n" +
				"		InterfaceWithExampleMethod interfaceWithExampleMethod = new InterfaceWithExampleMethod() {\n" +
				"			@Override\n" +
				"			public int exampleMethod() {\n" +
				"				return hashCode();\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		String expected = "" + //
				"	interface InterfaceWithExampleMethod{\n" +
				"		int exampleMethod();\n" +
				"	}\n" +
				"	public void test() {\n" +
				"		InterfaceWithExampleMethod interfaceWithExampleMethod = () -> {\n" +
				"			return hashCode();\n" +
				"		};\n" +
				"	}";

		assertChange(original, expected);
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

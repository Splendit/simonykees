package eu.jsparrow.core.visitor.make_final;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class MakeFieldsAndVariablesFinalASTVisitorTest extends UsesJDTUnitFixture {

	private static final String DEFAULT_METHOD_NAME = "FixtureMethod";


	@BeforeEach
	public void setUpDefaultVisitor() throws Exception {
		setDefaultVisitor(new MakeFieldsAndVariablesFinalASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void test_() throws Exception {
		String actual = "private String nonStaticField =\"nonStaticField\";"
				+ "private String nonStaticFieldEffectivelyFinal =\"nonStaticFieldEffectivelyFinal\";"
				+ "private static String staticField = \"staticField\";"
				+ "private static String staticFieldEffectivelyFinal = \"staticFieldEffectivelyFinal\";"
				+ ""
				+ "public void c() {"
				+ "	int i = 0;"
				+ "	int j = 0;"
				+ "	System.out.println(i);"
				+ "	System.out.println(j++);"
				+ "}"
				+ ""
				+ "public void d() {"
				+ "	nonStaticField += \"Altered\";"
				+ "	System.out.println(nonStaticField);"
				+ "	System.out.println(nonStaticFieldEffectivelyFinal);"
				+ "}"
				+ ""
				+ "public static void e() {"
				+ "	staticField = staticField + \"Altered\";"
				+ "	System.out.println(staticField);"
				+ "	System.out.println(staticFieldEffectivelyFinal);"
				+ "}";

		String expected = "private String nonStaticField =\"nonStaticField\";"
				+ "private final String nonStaticFieldEffectivelyFinal =\"nonStaticFieldEffectivelyFinal\";"
				+ "private static String staticField = \"staticField\";"
				+ "private static final String staticFieldEffectivelyFinal = \"staticFieldEffectivelyFinal\";"
				+ ""
				+ "public void c() {"
				+ "	final int i = 0;"
				+ "	int j = 0;"
				+ "	System.out.println(i);"
				+ "	System.out.println(j++);"
				+ "}"
				+ ""
				+ "public void d() {"
				+ "	nonStaticField += \"Altered\";"
				+ "	System.out.println(nonStaticField);"
				+ "	System.out.println(nonStaticFieldEffectivelyFinal);"
				+ "}"
				+ ""
				+ "public static void e() {"
				+ "	staticField = staticField + \"Altered\";"
				+ "	System.out.println(staticField);"
				+ "	System.out.println(staticFieldEffectivelyFinal);"
				+ "}";

		assertChange(actual, expected);
	}

	@Test
	public void localVariable_initInDeclaration_isEffectivelyFinal_shouldTransform() throws Exception {
		String actual = "int i = 0;"
				+ "System.out.println(i);";

		String expected = "final int i = 0;"
				+ "System.out.println(i);";
		
		MethodDeclaration method = defaultFixture.addMethod(DEFAULT_METHOD_NAME, actual);

		AbstractASTRewriteASTVisitor visitor = getDefaultVisitor();
		visitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(expected), defaultFixture.getMethodBlock(method));
	}

	@Test
	public void localVariable_initInDeclaration_isNotEffectivelyFinal_shouldNotTransform() throws Exception {
		String actualAndExpected = "int i = 0;"
				+ "System.out.println(i++);";

		AbstractASTRewriteASTVisitor visitor = getDefaultVisitor();
		MethodDeclaration method = defaultFixture.addMethod(DEFAULT_METHOD_NAME, actualAndExpected);
		visitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(actualAndExpected), defaultFixture.getMethodBlock(method));
	}

	@Test
	public void privateField_initInDeclaration_isEffectivelyFinal_shouldTransform() throws Exception {
		String actual = "private String a = \"a\";"
				+ ""
				+ "public void b() {"
				+ "	System.out.println(a);"
				+ "}";

		String expected = "private final String a = \"a\";"
				+ ""
				+ "public void b() {"
				+ "	System.out.println(a);"
				+ "}";

		assertChange(actual, expected);
	}

	@Test
	public void privateField_initInDeclaration_isNotEffectivelyFinal_shouldNotTransform() throws Exception {
		String actualAndExpected = "private String a = \"a\";"
				+ ""
				+ "public void b() {"
				+ "	a += \"Altered\";"
				+ "	System.out.println(a);"
				+ "}";

		assertNoChange(actualAndExpected);
	}

	@Test
	public void privateStaticField_initInDeclaration_isEffectivelyFinal_shouldTransform() throws Exception {
		String actual = "private static String a = \"a\";"
				+ ""
				+ "public static void b() {"
				+ "	System.out.println(a);"
				+ "}";

		String expected = "private static final String a = \"a\";"
				+ ""
				+ "public static void b() {"
				+ "	System.out.println(a);"
				+ "}";

		assertChange(actual, expected);
	}

	@Test
	public void privateStaticField_initInDeclaration_isNotEffectivelyFinal_shouldNotTransform() throws Exception {
		String actualAndExpected = "private static String a = \"a\";"
				+ ""
				+ "public static void b() {"
				+ "	a = a + \"Altered\";"
				+ "	System.out.println(a);"
				+ "}";

		assertNoChange(actualAndExpected);
	}

	@Test
	public void privateField_initInConstructor_accessWithName_isEffectivelyFinal_shouldTransform() throws Exception {
		String actual = "private String test;"
				+ ""
				+ "public " + DEFAULT_TYPE_DECLARATION_NAME + "() {"
				+ "	test = \"asdf\";"
				+ "}"
				+ ""
				+ "public void testMethod() {"
				+ "	System.out.println(test);"
				+ "}";

		String expected = "private final String test;"
				+ ""
				+ "public " + DEFAULT_TYPE_DECLARATION_NAME + "() {"
				+ "	test = \"asdf\";"
				+ "}"
				+ ""
				+ "public void testMethod() {"
				+ "	System.out.println(test);"
				+ "}";

		assertChange(actual, expected);
	}

	@Test
	public void privateFiled_initInConstructor_accessWithFieldAccess_shouldTransform() throws Exception {
		String actual = "private String test;"
				+ ""
				+ "public " + DEFAULT_TYPE_DECLARATION_NAME + "() {"
				+ "	this.test = \"asdf\";"
				+ "}"
				+ ""
				+ "public void testMethod() {"
				+ "	System.out.println(this.test);"
				+ "}";

		String expected = "private final String test;"
				+ ""
				+ "public " + DEFAULT_TYPE_DECLARATION_NAME + "() {"
				+ "	this.test = \"asdf\";"
				+ "}"
				+ ""
				+ "public void testMethod() {"
				+ "	System.out.println(this.test);"
				+ "}";

		assertChange(actual, expected);
	}

	@Test
	public void privateField_initInOneConstructorOnly_shouldNotTransform() throws Exception {
		String actualAndExpected = "private String a;"
				+ "public " + DEFAULT_TYPE_DECLARATION_NAME + "() {"
				+ "	a = \"asdf\";"
				+ "}"
				+ "public " + DEFAULT_TYPE_DECLARATION_NAME + "(String b) {"
				+ "	System.out.println(a);"
				+ "}";

		assertNoChange(actualAndExpected);
	}

	@Test
	public void privateField_initInMultipleInitializers_isNotEffectivelyFinal_shouldNotTransform() throws Exception {
		String actualAndExpected = "private String a;"
				+ "{"
				+ "	a = \"asdf\";"
				+ "}"
				+ "{"
				+ "	a = \"jkl\";"
				+ "}";

		assertNoChange(actualAndExpected);
	}

	@Test
	public void privateField_initInInitializer_isEffectivelyFinal_shouldTransform() throws Exception {
		String actual = "private String a;"
				+ "{"
				+ "	a = \"asdf\";"
				+ "}"
				+ "public void test() {"
				+ "	System.out.println(a);"
				+ "}";

		String expected = "private final String a;"
				+ "{"
				+ "	a = \"asdf\";"
				+ "}"
				+ "public void test() {"
				+ "	System.out.println(a);"
				+ "}";

		assertChange(actual, expected);
	}

	@Test
	public void privateStaticField_initInInitializer_isEffectivelyFinal_shouldTransform() throws Exception {
		String actual = "private static String a;"
				+ "static {"
				+ "	a = \"asdf\";"
				+ "}"
				+ "public void test() {"
				+ "	System.out.println(a);"
				+ "}";

		String expected = "private static final String a;"
				+ "static {"
				+ "	a = \"asdf\";"
				+ "}"
				+ "public void test() {"
				+ "	System.out.println(a);"
				+ "}";

		assertChange(actual, expected);
	}

	@Test
	public void privateStaticField_initInMultipleInitializers_isNotEffectivelyFinal_shouldNotTransform()
			throws Exception {
		String actualAndExpected = "private static String a;"
				+ "static {"
				+ "	a = \"asdf\";"
				+ "}"
				+ "static {"
				+ "	a = \"jkl\";"
				+ "}"
				+ "public void test() {"
				+ "	System.out.println(a);"
				+ "}";

		assertNoChange(actualAndExpected);
	}

	@Test
	public void privateField_initInConstructors_alteredByPrefixExpression_shouldNotTransform() throws Exception {
		String actualAndExpected = "private int a;"
				+ "public " + DEFAULT_TYPE_DECLARATION_NAME + "() {"
				+ "	a = 1;"
				+ "}"
				+ "private void increase() {"
				+ "	++a;"
				+ "}";

		assertNoChange(actualAndExpected);
	}

	@Test
	public void privateField_initInDeclaration_nonAssigningPrefixExpression_shouldTransform() throws Exception {
		String actual = "private int a = 1;"
				+ "public void test() {"
				+ "	System.out.println(-a);"
				+ "}";

		String expected = "private final int a = 1;"
				+ "public void test() {"
				+ "	System.out.println(-a);"
				+ "}";

		assertChange(actual, expected);
	}
	
	@Test
	public void privateField_reassignInnerInnerFieldInRootClassMethod_shouldNotTransform() throws Exception {
		String actual = "" +
				"public static class InnerClassWithConstructor {\n" + 
				"	public static class InnerInnerClass {\n" + 
				"		private int intValue = 0;\n" + 
				"	}\n" + 
				"}\n" + 
				"private void sampleMethod() {\n" + 
				"	final InnerClassWithConstructor.InnerInnerClass xInnerInnerClass = new InnerClassWithConstructor.InnerInnerClass();\n" + 
				"	xInnerInnerClass.intValue = 1;\n" + 
				"}";
		assertNoChange(actual);
	}
	
	@Test
	public void privateField_volatile_shouldNotTransform() throws Exception {
		String actual = "private volatile int a = 1;";
		assertNoChange(actual);
	}
	
	@Test
	public void privateField_initializerInAnonymousClass_shouldNotTransform() throws Exception {
		
		String actual = "" +
				"public Runnable runnable = new Runnable() {\n" + 
				"	private String finalField = \"\";\n" + 
				"	@Override\n" + 
				"	public void run() {}\n" + 
				"};";
		assertNoChange(actual);
	}
	
	@Test
	public void privateField_reassignOuterFieldInInnerConstructor_shouldNotTransform() throws Exception {
		
		String original = "" +
				"private String value = \"\";\n" + 
				"public class InnerClass {\n" + 
				"	private final String value2;\n" + 
				"	public InnerClass() {\n" + 
				"		value = \"2\";\n" + 
				"		value2 = \"3\";\n" + 
				"	}\n" + 
				"}";
		assertNoChange(original);
	}
}

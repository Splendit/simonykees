package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.security.random.ReuseRandomObjectsASTVisitor;

public class ReuseRandomObjectsASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUpDefaultVisitor() throws Exception {
		setDefaultVisitor(new ReuseRandomObjectsASTVisitor());
		defaultFixture.addImport(java.util.Random.class.getName());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void test_localRandomVariable_shouldTransform() throws Exception {
		String actual = "" +
				"private void sampleMethod() {\n" +
				"	Random random = new Random();\n" +
				"	int iRandom = random.nextInt();\n" +
				"	System.out.println(\"\");\n" +
				"}\n" +
				"";
		String expected = "" +
				"private Random random = new Random();\n" +
				"private void sampleMethod() {\n" +
				"	int iRandom = random.nextInt();\n" +
				"	System.out.println(\"\");\n" +
				"}";
		assertChange(actual, expected);
	}

	@Test
	public void test_multipleLocalVariables_shouldTransform() throws Exception {
		String actual = "" +
				"private void sampleMethod(String value) {\n" +
				"	if(value.isEmpty()) {\n" +
				"		Random random = new Random();\n" +
				"		System.out.println(random);\n" +
				"	}\n" +
				"	Random random = new Random();\n" +
				"	System.out.println(value + random);\n" +
				"}";
		String expected = "" +
				"private Random random = new Random();" +
				"private void sampleMethod(String value) {\n" +
				"	if(value.isEmpty()) {\n" +
				"		System.out.println(random);\n" +
				"	}\n" +
				"	System.out.println(value + random);\n" +
				"}";
		assertChange(actual, expected);
	}
	
	@Test
	public void test_usingStaticFinalSeed_shouldTransform() throws Exception {
		String actual = "" +
				"private static final int seed = 10;\n" + 
				"public void sampleMethod() {\n" + 
				"	Random withStaticFinalSeed = new Random(seed);\n" + 
				"}";
		String expected = "" +
				"private static final int seed = 10;\n" + 
				"private Random withStaticFinalSeed = new Random(seed);" +
				"public void sampleMethod() {\n" + 
				"}\n";
		assertChange(actual, expected);
	}

	@Test
	public void test_localRandomObjectShadowingAField_shouldTransform() throws Exception {
		String actual = "" +
				"private Random random = new Random();" +
				"private void sampleMethod(String value) {\n" +
				"	Random random = new Random();\n" +
				"	System.out.println(value + random.nextInt());\n" +
				"}";
		String expected = "" +
				"private Random random = new Random();" +
				"private void sampleMethod(String value) {\n" +
				"	System.out.println(value + random.nextInt());\n" +
				"}";
		assertChange(actual, expected);
	}
	
	@Test
	public void test_staticMethod_shouldTransform() throws Exception {
		String actual = "" +
				"private static void sampleMethod(String value) {\n" + 
				"	Random random = new Random();\n" + 
				"	System.out.println(value + random.nextInt());\n" + 
				"}";
		String expected = "" +
				"private static Random random = new Random();" +
				"private static void sampleMethod(String value) {\n" + 
				"	System.out.println(value + random.nextInt());\n" + 
				"}\n" + 
				"";
		assertChange(actual, expected);
	}
	
	@Test
	public void test_finalVariable_shouldTransform() throws Exception {
		String actual = "" + 
				"private void sampleMethod(String value) {\n" + 
				"	final Random random = new Random();\n" + 
				"	System.out.println(value + random.nextInt());\n" + 
				"}";
		String expected = "" + 
				"private final Random random = new Random();" +
				"private void sampleMethod(String value) {\n" + 
				"	System.out.println(value + random.nextInt());\n" + 
				"}\n" + 
				"";
		assertChange(actual, expected);
	}
	
	@Test
	public void test_randomInLambdaInitializer_shouldTransform() throws Exception {
		String actual = "" + 
				"private Runnable runnable = () -> {\n" + 
				"	Random random = new Random();\n" + 
				"	int i = random.nextInt();\n" + 
				"};";
		String expected = "" + 
				"private Random random = new Random();\n" + 
				"private Runnable runnable = () -> {\n" + 
				"	int i = random.nextInt();\n" + 
				"};";
		assertChange(actual, expected);
	}
	
	@Test
	public void test_findingPositionOfExtractedField_shouldTransform() throws Exception {
		String actual = "" + 
				"private Runnable runnable = () -> {\n" + 
				"	Random random = new Random();\n" + 
				"	int i = random.nextInt();\n" + 
				"};" +
				"private void sampleMethod() {}\n";
		String expected = "" + 
				"private Random random = new Random();\n" +
				"private Runnable runnable = () -> {\n" +
				"	int i = random.nextInt();\n" +
				"};" +
				"private void sampleMethod() {}\n";
		assertChange(actual, expected);
	}
	
	/*
	 * Negative Test Cases
	 */
	
	@Test
	public void test_shadowingNonPrivateField_shouldNotTransform() throws Exception {
		String actual = "" + 
				"Random random = new Random();\n" + 
				"private void sampleMethod(String value) {\n" + 
				"	Random random = new Random();\n" + 
				"	System.out.println(value + random.nextInt());\n" + 
				"}";
		assertNoChange(actual);
	}
	
	@Test
	public void test_shadowingNonRandomField_shouldNotTransform() throws Exception {
		String actual = "" + 
				"int random = 123;\n" + 
				"private void sampleMethod(String value) {\n" + 
				"	Random random = new Random();\n" + 
				"	System.out.println(value + random.nextInt());\n" + 
				"}";
		assertNoChange(actual);
	}
	
	@Test
	public void test_shadowingNonStaticField_shouldNotTransform() throws Exception {
		String actual = "" + 
				"private Random random = new Random();\n" + 
				"private static void sampleMethod(String value) {\n" + 
				"	Random random = new Random();\n" + 
				"	System.out.println(value + random.nextInt());\n" + 
				"}";
		assertNoChange(actual);
	}
	
	@Test
	public void test_shadowingNotInitializedField_shouldNotTransform() throws Exception {
		String actual = "" + 
				"private Random random;\n" + 
				"private void sampleMethod(String value) {\n" + 
				"	Random random = new Random();\n" + 
				"	System.out.println(value + random.nextInt());\n" + 
				"}";
		assertNoChange(actual);
	}

	@Test
	public void test_shadowingNullInitializedField_shouldNotTransform() throws Exception {
		String actual = "" + 
				"private Random random = null;\n" + 
				"private void sampleMethod(String value) {\n" + 
				"	Random random = new Random();\n" + 
				"	System.out.println(value + random.nextInt());\n" + 
				"}";
		assertNoChange(actual);
	}

	@Test
	public void test_shadowingNullInitializedLocalVariable_shouldNotTransform() throws Exception {
		String actual = "" +
				"private void sampleMethod(String value) {\n" + 
				"	Random random = null;\n" + 
				"	System.out.println(value + random.nextInt());\n" + 
				"}";
		assertNoChange(actual);
	}

	@Test
	public void test_randomVariableInAnonymousClass_shouldNotTransform() throws Exception {
		String actual = "" + 
				"private void sampleMethod() {\n" + 
				"	Runnable r = new Runnable() {\n" + 
				"		@Override\n" + 
				"		public void run() {\n" + 
				"			Random random = new Random();\n" + 
				"			int i = random.nextInt();\n" + 
				"		}\n" + 
				"	};\n" + 
				"}"; 
		assertNoChange(actual);
	}
	
	@Test
	public void test_innerClass_shouldNotTransform() throws Exception {
		String actual = "" + 
				"private void sampleMethod() {\n" + 
				"	class LocalClass {\n" + 
				"		private void innerMethod() {\n" + 
				"			Random random = new Random();\n" + 
				"			int i = random.nextInt();\n" + 
				"		}\n" + 
				"	}\n" + 
				"}"; 
		assertNoChange(actual);
	}

	@Test
	public void test_usingLocalVariableAsSeed_shouldNotTransform() throws Exception {
		String actual = "" + 
				"private void sampleMethod() {\n" + 
				"	int seed = 10;\n" + 
				"	Random random = new Random(seed);\n" + 
				"	int i = random.nextInt();\n" + 
				"}"; 
		assertNoChange(actual);
	}

	@Test
	public void test_matchingFieldWithDifferentInitializer_shouldNotTransform() throws Exception {
		String actual = "" + 
				"private Random random = new Random();\n" + 
				"private void sampleMethod() {\n" + 
				"	Random random = new Random(10);\n" + 
				"	int i = random.nextInt();\n" + 
				"}"; 
		assertNoChange(actual);
	}

	@Test
	public void test_reuseObjectWithIntLiteralAsSeed_shouldTransform() throws Exception {
		String actual = "" + 
				"private Random random = new Random(10);\n" + 
				"private void sampleMethod() {\n" + 
				"	Random random = new Random(10);\n" + 
				"	int i = random.nextInt();\n" + 
				"}";
		String expected = "" + 
				"private Random random = new Random(10);\n" + 
				"private void sampleMethod() {\n" + 
				"	int i = random.nextInt();\n" + 
				"}";
		assertChange(actual, expected);
	}

	@Test
	public void test_extractObjectWithIntLiteralAsSeed_shouldTransform() throws Exception {
		String actual = "" + 
				"private void sampleMethod() {\n" + 
				"	Random random = new Random(10);\n" + 
				"	int i = random.nextInt();\n" + 
				"}";
		String expected = "" + 
				"private Random random = new Random(10);\n" + 
				"private void sampleMethod() {\n" + 
				"	int i = random.nextInt();\n" + 
				"}";
		assertChange(actual, expected);
	}

	@Test
	public void test_usingLocalVarTypeInference_shouldTransform() throws Exception {
		setJavaVersion(JavaCore.VERSION_11);
		String actual = "" + 
				"private void sampleMethod() {\n" + 
				"	var random = new Random();\n" + 
				"	int i = random.nextInt();\n" + 
				"}";
		String expected = "" + 
				"private Random random = new Random();\n" + 
				"private void sampleMethod() {\n" + 
				"	int i = random.nextInt();\n" + 
				"}";
		assertChange(actual, expected);
	}
}

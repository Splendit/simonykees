package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
				"	System.out.println(value + random7.nextInt());\n" + 
				"}";
		assertNoChange(actual);
	}
	
	@Test
	public void test_shadowingNonRandomField_shouldNotTransform() throws Exception {
		String actual = "" + 
				"Random random = new Random();\n" + 
				"private void sampleMethod(String value) {\n" + 
				"	Random random = new Random();\n" + 
				"	System.out.println(value + random7.nextInt());\n" + 
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

	
	
	@Disabled("only as a framework")
	@Test
	public void test_() throws Exception {
		String actual = "" + 
				"";
		String expected = "" + 
				"";
		assertChange(actual, expected);
	}

}

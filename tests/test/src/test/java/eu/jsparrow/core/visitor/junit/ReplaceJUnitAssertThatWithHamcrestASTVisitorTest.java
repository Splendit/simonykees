package eu.jsparrow.core.visitor.junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

class ReplaceJUnitAssertThatWithHamcrestASTVisitorTest extends UsesJDTUnitFixture {
	
	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("org.hamcrest", "hamcrest-core", "1.3");
		addDependency("org.hamcrest", "hamcrest-library", "1.3");
		addDependency("junit", "junit", "4.13");

		defaultFixture.addImport("org.junit.Test");
		defaultFixture.addImport("org.hamcrest.Matcher");
		defaultFixture.addImport("org.junit.Assert");
		defaultFixture.addImport("org.hamcrest.Matchers.equalToIgnoringCase", true, false);
		setDefaultVisitor(new ReplaceJUnitAssertThatWithHamcrestASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_junitAssertThat_shouldTransform() throws Exception {
		String orginal = ""
				+ "	@Test\n"
				+ "	public void replacingAssertThat_noQualifier() throws Exception {\n"
				+ "		Assert.assertThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "	}";
		String expected = ""
				+ "	@Test\n"
				+ "	public void replacingAssertThat_noQualifier() throws Exception {\n"
				+ "		MatcherAssert.assertThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "	}";
		assertChange(orginal, expected);
	}
	
	@Test
	void visit_fullyQualifiedNameAssertThat_shouldTransform() throws Exception {	
		String original = ""
				+ "@Test\n"
				+ "public void localMatcherAssertClass() throws Exception {\n"
				+ "	org.junit.Assert.assertThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "}\n"
				+ "\n"
				+ "static class Assert {\n"
				+ "	public static void assertThat(String value, Matcher<String> object) {}\n"
				+ "}";
		String expected = ""
				+ "@Test\n"
				+ "public void localMatcherAssertClass() throws Exception {\n"
				+ "	MatcherAssert.assertThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "}\n"
				+ "\n"
				+ "static class Assert {\n"
				+ "	public static void assertThat(String value, Matcher<String> object) {}\n"
				+ "}";
		assertChange(original, expected);
	}
	
	@Test
	void visit_namingConflicts_shouldTransform() throws Exception {	
		String original = ""
				+ "@Test\n"
				+ "public void localMatcherAssertClass() throws Exception {\n"
				+ "	org.junit.Assert.assertThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "}\n"
				+ "\n"
				+ "static class Assert {\n"
				+ "	public static void assertThat(String value, Matcher<String> object) {}\n"
				+ "}"
				+ "class MatcherAssert {}";
		String expected = ""
				+ "@Test\n"
				+ "public void localMatcherAssertClass() throws Exception {\n"
				+ "	org.hamcrest.MatcherAssert.assertThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "}\n"
				+ "\n"
				+ "static class Assert {\n"
				+ "	public static void assertThat(String value, Matcher<String> object) {}\n"
				+ "}"
				+ "class MatcherAssert {}";
		assertChange(original, expected);
	}
	@Test
	void visit_localAssertThatMethod_shouldNotTransform() throws Exception {
		String original = ""
				+ "@Test"
				+ "public void replacingAssertThat_noQualifier() throws Exception {\n"
				+ "	this.assertThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "}"
				+ "private void assertThat(String value, Matcher<String> object) {}";
		assertNoChange(original);
	}
	
	@Test
	void visit_localMatcherAssertClass_shouldNotTransform() throws Exception {	
		String original = ""
				+ "@Test\n"
				+ "public void localMatcherAssertClass() throws Exception {\n"
				+ "	Assert.assertThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "}\n"
				+ "\n"
				+ "static class Assert {\n"
				+ "	public static void assertThat(String value, Matcher<String> object) {}\n"
				+ "}";
		assertNoChange(original);
	}
}

package eu.jsparrow.core.markers.visitor.junit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class ReplaceJUnitAssertThatWithHamcrestResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
		addDependency("org.hamcrest", "hamcrest-core", "1.3");
		addDependency("org.hamcrest", "hamcrest-library", "1.3");
		addDependency("junit", "junit", "4.13");

		defaultFixture.addImport("org.junit.Test");
		defaultFixture.addImport("org.hamcrest.Matcher");
		defaultFixture.addImport("org.junit.Assert");
		defaultFixture.addImport("org.hamcrest.Matchers.equalToIgnoringCase", true, false);
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		ReplaceJUnitAssertThatWithHamcrestResolver visitor = new ReplaceJUnitAssertThatWithHamcrestResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceJUnitAssertThatWithHamcrestResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	@Test\n"
				+ "	public void replacingAssertThat_noQualifier() throws Exception {\n"
				+ "		Assert.assertThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "	}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		ReplaceJUnitAssertThatWithHamcrestResolver visitor = new ReplaceJUnitAssertThatWithHamcrestResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceJUnitAssertThatWithHamcrestResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	@Test\n"
				+ "	public void replacingAssertThat_noQualifier() throws Exception {\n"
				+ "		Assert.assertThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "	}";
		String expected = ""
				+ "	@Test\n"
				+ "	public void replacingAssertThat_noQualifier() throws Exception {\n"
				+ "		MatcherAssert.assertThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "	}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "The JUnit Assert.assertThat method is deprecated. Its sole purpose is to "
				+ "forward the call to the MatcherAssert.assertThat method defined in Hamcrest 1.3. "
				+ "Therefore, it is recommended to directly use the equivalent assertion defined in "
				+ "the third party Hamcrest library.";

		assertAll(
				() -> assertEquals("Replace JUnit assertThat with Hamcrest", event.getName()),
				() -> assertEquals(description, event.getMessage()),
				() -> assertEquals("ReplaceJUnitAssertThatWithHamcrestResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(263, event.getOffset()),
				() -> assertEquals(56, event.getLength()),
				() -> assertEquals(12, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		ReplaceJUnitAssertThatWithHamcrestResolver visitor = new ReplaceJUnitAssertThatWithHamcrestResolver(node -> node.getStartPosition() == 263);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReplaceJUnitAssertThatWithHamcrestResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "	@Test\n"
				+ "	public void replacingAssertThat_noQualifier() throws Exception {\n"
				+ "		Assert.assertThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "	}";
		String expected = ""
				+ "	@Test\n"
				+ "	public void replacingAssertThat_noQualifier() throws Exception {\n"
				+ "		MatcherAssert.assertThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "	}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

}
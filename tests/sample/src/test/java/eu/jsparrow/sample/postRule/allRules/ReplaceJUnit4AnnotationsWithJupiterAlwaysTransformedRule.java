package eu.jsparrow.sample.postRule.allRules;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AnnotationsWithJupiterAlwaysTransformedRule {

	class TestWithAllTestMethodAnnotations {

		@BeforeEach
		public void beforeEach() throws Exception {
		}

		@AfterEach
		public void afterEach() throws Exception {
		}

		@BeforeAll
		public void beforeAll() throws Exception {
		}

		@AfterAll
		public void afterAll() throws Exception {
		}

		@Test
		public void testWithTestMarkerAnnotation() throws Exception {
		}

		@Test()
		public void testWithTestNormalAnnotation() throws Exception {
		}

		@Disabled
		@Test
		public void testWithIgnoreMarkerAnnotation() throws Exception {
		}

		@Disabled(value = "This test is ignored")
		@Test
		public void testWithIgnoreNormalAnnotation() throws Exception {
		}

		@Disabled("This test is ignored")
		@Test
		public void testWithIgnoreSingleMemberAnnotation() throws Exception {
		}
	};

}
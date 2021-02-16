package eu.jsparrow.sample.preRule;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ReplaceJUnit4AnnotationsWithJupiterAlwaysTransformedRule {

	class TestWithAllTestMethodAnnotations {

		@Before
		public void beforeEach() throws Exception {
		}

		@After
		public void afterEach() throws Exception {
		}

		@BeforeClass
		public void beforeAll() throws Exception {
		}

		@AfterClass
		public void afterAll() throws Exception {
		}

		@Test
		public void testWithTestMarkerAnnotation() throws Exception {
		}

		@Test()
		public void testWithTestNormalAnnotation() throws Exception {
		}

		@Ignore
		@Test
		public void testWithIgnoreMarkerAnnotation() throws Exception {
		}

		@Ignore(value = "This test is ignored")
		@Test
		public void testWithIgnoreNormalAnnotation() throws Exception {
		}

		@Ignore("This test is ignored")
		@Test
		public void testWithIgnoreSingleMemberAnnotation() throws Exception {
		}
	};

}
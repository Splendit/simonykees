package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.function.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegexPredicateFactoryTest {

	@SuppressWarnings("restriction")
	@Test
	public void testCreateJUnitPackagePredicate() throws Exception {

		Predicate<String> jUnit4PackagePredicate = RegexPredicateFactory.createjUnit4PackagePredicate();

		Assertions.assertTrue(jUnit4PackagePredicate.test(junit.extensions.ActiveTestSuite.class.getPackageName()));
		Assertions.assertTrue(jUnit4PackagePredicate.test(junit.framework.TestResult.class.getPackageName()));
		Assertions.assertTrue(jUnit4PackagePredicate.test(junit.runner.BaseTestRunner.class.getPackageName()));
		Assertions.assertTrue(jUnit4PackagePredicate.test(junit.textui.TestRunner.class.getPackageName()));

		Assertions.assertTrue(jUnit4PackagePredicate.test("org.junit"));
		Assertions.assertFalse(jUnit4PackagePredicate.test("org.junit2"));
		Assertions.assertTrue(jUnit4PackagePredicate.test(org.junit.Assert.class.getPackageName()));
		Assertions.assertTrue(jUnit4PackagePredicate.test("org.junit.experimental"));
		Assertions.assertFalse(jUnit4PackagePredicate.test("org.junit.experimental2"));
		Assertions.assertTrue(jUnit4PackagePredicate.test("org.junit.experimental.categories"));
		Assertions.assertTrue(jUnit4PackagePredicate.test("org.junit.experimental.categories2"));
		Assertions.assertTrue(jUnit4PackagePredicate.test(org.junit.experimental.categories.Category.class.getName()));
		Assertions.assertTrue(jUnit4PackagePredicate.test(org.junit.experimental.ParallelComputer.class.getName()));
		Assertions.assertTrue(jUnit4PackagePredicate.test("org.junit.function"));
		Assertions.assertTrue(jUnit4PackagePredicate.test(org.junit.function.ThrowingRunnable.class.getName()));
		Assertions.assertTrue(jUnit4PackagePredicate.test("org.junit.internal"));
		Assertions.assertTrue(jUnit4PackagePredicate.test(org.junit.internal.TextListener.class.getName()));
		Assertions.assertTrue(jUnit4PackagePredicate.test(org.junit.matchers.JUnitMatchers.class.getName()));
		Assertions.assertTrue(jUnit4PackagePredicate.test(org.junit.rules.DisableOnDebug.class.getName()));
		Assertions.assertTrue(jUnit4PackagePredicate.test(org.junit.runner.Computer.class.getName()));
		Assertions.assertTrue(jUnit4PackagePredicate.test(org.junit.runners.AllTests.class.getName()));
		Assertions.assertTrue(jUnit4PackagePredicate.test(org.junit.validator.AnnotationsValidator.class.getName()));
	}
	
	@Test
	public void testCreateSupportedAnnotationPredicate() throws Exception {
		Predicate<String> supportedAnnotationPredicate = RegexPredicateFactory.createSupportedAnnotationPredicate();
		Assertions.assertTrue(supportedAnnotationPredicate.test(org.junit.Ignore.class.getName()));
		Assertions.assertTrue(supportedAnnotationPredicate.test(org.junit.Test.class.getName()));
		Assertions.assertTrue(supportedAnnotationPredicate.test(org.junit.After.class.getName()));
		Assertions.assertTrue(supportedAnnotationPredicate.test(org.junit.AfterClass.class.getName()));
		Assertions.assertTrue(supportedAnnotationPredicate.test(org.junit.Before.class.getName()));
		Assertions.assertTrue(supportedAnnotationPredicate.test(org.junit.BeforeClass.class.getName()));
		
		Assertions.assertFalse(supportedAnnotationPredicate.test(org.junit.Assert.class.getName()));
		Assertions.assertFalse(supportedAnnotationPredicate.test(org.junit.runner.RunWith.class.getName()));


	}

}

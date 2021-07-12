package eu.jsparrow.core.visitor.junit.junit3;

/**
 * Stores all data which describe the migration of JUnit 3 to either JUnit 4 or
 * JUnit Jupiter.
 *
 */
public class Junit3MigrationConfiguration {

	private final String assertionClassQualifiedName;
	private final String testAnnotationQualifiedName;
	private final String setupAnnotationQualifiedName;
	private final String teardownAnnotationQualifiedName;

	Junit3MigrationConfiguration(Junit3MigrationConfigurationFactory configFactory) {
		assertionClassQualifiedName = configFactory.getAssertionClassQualifiedName();
		testAnnotationQualifiedName = configFactory.getTestAnnotationQualifiedName();
		setupAnnotationQualifiedName = configFactory.getSetupAnnotationQualifiedName();
		teardownAnnotationQualifiedName = configFactory.getTeardownAnnotationQualifiedName();
	}

	public String getAssertionClassQualifiedName() {
		return assertionClassQualifiedName;
	}

	public String getTestAnnotationQualifiedName() {
		return testAnnotationQualifiedName;
	}

	public String getSetUpAnnotationQualifiedName() {
		return setupAnnotationQualifiedName;
	}

	public String getTearDownAnnotationQualifiedName() {
		return teardownAnnotationQualifiedName;
	}
}
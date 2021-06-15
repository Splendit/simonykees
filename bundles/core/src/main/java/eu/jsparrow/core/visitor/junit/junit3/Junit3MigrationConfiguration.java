package eu.jsparrow.core.visitor.junit.junit3;

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

	public String getSetupAnnotationQualifiedName() {
		return setupAnnotationQualifiedName;
	}

	public String getTeardownAnnotationQualifiedName() {
		return teardownAnnotationQualifiedName;
	}
}
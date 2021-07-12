package eu.jsparrow.core.visitor.junit.junit3;

/**
 * Factory class producing configurations for two purposes:
 * <ul>
 * <li>Migration of >JUnit 3 to JUnit 4</li>
 * <li>Migration of >JUnit 3 to JUnit Jupiter</li>
 * </ul>
 * 
 * @since 4.1.0
 *
 */
public class Junit3MigrationConfigurationFactory {

	private String assertionClassQualifiedName;
	private String testAnnotationQualifiedName;
	private String setupAnnotationQualifiedName;
	private String teardownAnnotationQualifiedName;

	public Junit3MigrationConfiguration createJUnit4ConfigurationValues() {
		assertionClassQualifiedName = "org.junit.Assert"; //$NON-NLS-1$
		testAnnotationQualifiedName = "org.junit.Test"; //$NON-NLS-1$
		setupAnnotationQualifiedName = "org.junit.Before"; //$NON-NLS-1$
		teardownAnnotationQualifiedName = "org.junit.After"; //$NON-NLS-1$

		return new Junit3MigrationConfiguration(this);
	}

	public Junit3MigrationConfiguration createJUnitJupiterConfigurationValues() {
		assertionClassQualifiedName = "org.junit.jupiter.api.Assertions"; //$NON-NLS-1$
		testAnnotationQualifiedName = "org.junit.jupiter.api.Test"; //$NON-NLS-1$
		setupAnnotationQualifiedName = "org.junit.jupiter.api.BeforeEach"; //$NON-NLS-1$
		teardownAnnotationQualifiedName = "org.junit.jupiter.api.AfterEach"; //$NON-NLS-1$

		return new Junit3MigrationConfiguration(this);
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
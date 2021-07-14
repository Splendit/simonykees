package eu.jsparrow.core.visitor.junit.junit3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Junit3MigrationConfigurationTest {

	private Junit3MigrationConfiguration junit3MigrationConfiguration;

	@Test
	public void testCreateJUnit4ConfigurationValues() {
		junit3MigrationConfiguration = new Junit3MigrationConfigurationFactory().createJUnit4ConfigurationValues();
		assertEquals("org.junit.Assert", junit3MigrationConfiguration.getAssertionClassQualifiedName());
		assertEquals("org.junit.Test", junit3MigrationConfiguration.getTestAnnotationQualifiedName());
		assertEquals("org.junit.Before", junit3MigrationConfiguration.getSetUpAnnotationQualifiedName());
		assertEquals("org.junit.After", junit3MigrationConfiguration.getTearDownAnnotationQualifiedName());
	}

	@Test
	public void testCreateJUnitJupiterConfigurationValues() {
		junit3MigrationConfiguration = new Junit3MigrationConfigurationFactory()
			.createJUnitJupiterConfigurationValues();
		assertEquals("org.junit.jupiter.api.Assertions", junit3MigrationConfiguration.getAssertionClassQualifiedName());
		assertEquals("org.junit.jupiter.api.Test", junit3MigrationConfiguration.getTestAnnotationQualifiedName());
		assertEquals("org.junit.jupiter.api.BeforeEach",
				junit3MigrationConfiguration.getSetUpAnnotationQualifiedName());
		assertEquals("org.junit.jupiter.api.AfterEach",
				junit3MigrationConfiguration.getTearDownAnnotationQualifiedName());
	}
}
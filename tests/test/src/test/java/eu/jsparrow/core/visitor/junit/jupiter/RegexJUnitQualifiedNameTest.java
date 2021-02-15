package eu.jsparrow.core.visitor.junit.jupiter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class RegexJUnitQualifiedNameTest {

	@ParameterizedTest
	@ValueSource(strings = { "junit", "junit.x", "junit.x.y.T", "org.junit", "org.junit.x", "org.junit.x.y.T",
			"org.junit.jupiter.api", "org.junit.jupiter.api.x", "org.junit.jupiter.api.x.y.T" })
	public void testIsJUnitName(String name) throws Exception {
		Assertions.assertTrue(RegexJUnitQualifiedName.isJUnitName(name));
	}

	@ParameterizedTest
	@ValueSource(strings = { "junit1", "junit1.x", "org", "org1.junit", "org1.junit.x", "org.junit1", "org.junit1.x" })
	public void testIsNotJUnitName(String name) throws Exception {
		Assertions.assertFalse(RegexJUnitQualifiedName.isJUnitName(name));
	}

	@ParameterizedTest
	@ValueSource(strings = { "junit", "junit.x.y.T", "org.junit", "org.junit.x.y.T", "org.junit.jupiter.x.y.T",
			"org.junit.jupiter.api1.T" })
	public void testIsJUnitAndNotJUnitJupiterName(String name) throws Exception {
		Assertions.assertTrue(RegexJUnitQualifiedName.isJUnitName(name));
		Assertions.assertFalse(RegexJUnitQualifiedName.isJUnitJupiterName(name));
	}

	@ParameterizedTest
	@ValueSource(strings = {"org.junit.jupiter.api", "org.junit.jupiter.api.x", "org.junit.jupiter.api.x.y.T"})
	public void testIsJUnitAndJUnitJupiterName(String name) throws Exception {
		Assertions.assertTrue(RegexJUnitQualifiedName.isJUnitName(name));
		Assertions.assertTrue(RegexJUnitQualifiedName.isJUnitJupiterName(name));
	}
}

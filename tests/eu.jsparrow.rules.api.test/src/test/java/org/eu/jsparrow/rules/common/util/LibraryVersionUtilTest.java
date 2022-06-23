package org.eu.jsparrow.rules.common.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.Version;

import eu.jsparrow.rules.common.exception.InvalidLibraryVersionException;
import eu.jsparrow.rules.common.util.LibrariesVersionUtil;

class LibraryVersionUtilTest {

	private Predicate<Version> versionComparator;

	@BeforeEach
	public void setUp() {
		versionComparator = version -> version.compareTo(Version.parseVersion("5.0.0")) >= 0;
	}

	@ParameterizedTest
	@ValueSource(strings = { "5.7.0-M2", "5", "5.0", "5.0.0", "5.0.0.0", "5.0.1.RELEASE", "5.0.1.RELEASE-M2" })
	void validVersions_shouldBeSatisfied(String version) throws InvalidLibraryVersionException {
		boolean satisfied = LibrariesVersionUtil.satisfies(version, versionComparator);
		assertTrue(satisfied);
	}

	@ParameterizedTest
	@ValueSource(strings = { "4.7.0-M2", "4", "4.0", "4.0.0", "4.0.0.0" })
	void validVersions_shouldBeNotSatisfied(String version) throws InvalidLibraryVersionException {
		boolean satisfied = LibrariesVersionUtil.satisfies(version, versionComparator);
		assertFalse(satisfied);
	}

	@ParameterizedTest
	@ValueSource(strings = { "INVALID_VERSION", "5.0.0.0.0", "5.0.0.0.0.0", "", "-", "5.R", "5.3.r", "5.0.0.1.RELEASE", "R" })
	void invalidVersion_shouldThrowException(String version) {
		assertThrows(InvalidLibraryVersionException.class,
				() -> LibrariesVersionUtil.satisfies(version, versionComparator));
	}
}

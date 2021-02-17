package eu.jsparrow.rules.common.util;

import java.util.function.Predicate;

import org.osgi.framework.Version;

import eu.jsparrow.rules.common.exception.InvalidLibraryVersionException;

/**
 * A utility class for parsing and comparing the versions of libraries.
 * 
 * @since 3.27.0
 *
 */
public class LibrariesVersionUtil {

	private LibrariesVersionUtil() {
		/*
		 * Hide Default constructor.
		 */
	}

	/**
	 * Validates the given version, parses it to a {@link Version}, and verifies
	 * if the predicate is satisfied
	 * 
	 * @param version
	 *            the version to be verified
	 * @param condition
	 *            a predicate to verify if the version is satisfied
	 * @return if the predicate is satisfied
	 * @throws InvalidLibraryVersionException
	 *             if the given version does not match this (simplified) pattern:
	 * 
	 *             <pre>
	 *             NUMBERS[.NUMBERS[.NUMBERS[.NUMBERS]]]-ANYTHING
	 */
	public static boolean satisfies(String version, Predicate<Version> condition)
			throws InvalidLibraryVersionException {
		if (version == null) {
			throw new InvalidLibraryVersionException("No version provided.");//$NON-NLS-1$
		}
		String numericVersion = chopNonNumeric(version);
		validate(numericVersion);
		Version actualVersion = Version.parseVersion(numericVersion);
		return condition.test(actualVersion);
	}

	private static void validate(String version) throws InvalidLibraryVersionException {
		if (version.isEmpty()) {
			String message = "No version provided."; //$NON-NLS-1$
			throw new InvalidLibraryVersionException(message);
		}

		boolean isVersionPattern = version.matches("^\\d+(\\.\\d+){0,3}$"); //$NON-NLS-1$
		if (!isVersionPattern) {
			String message = String.format("Invalid library version %s.", version); //$NON-NLS-1$
			throw new InvalidLibraryVersionException(message);
		}
	}

	private static String chopNonNumeric(String version) {
		String[] parts = version.split("-"); //$NON-NLS-1$
		if (parts.length > 0) {
			return parts[0];
		} else {
			return ""; //$NON-NLS-1$
		}
	}

}

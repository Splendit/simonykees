package at.splendit.simonykees.core.visitor.renaming;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NamingConventionUtil {

	public static boolean isComplyingWithConventions(String identifier) {
		// the following regex is taken from sonarqube
		return identifier.matches("^[a-z][a-zA-Z0-9]*$"); //$NON-NLS-1$
	}

	/**
	 * Converts the given string to camelCase by removing the non-alphanumeric
	 * symbols '$' and '_' and capitalizing the character which is following
	 * them, unless it is the fist character of the string. Furthermore, checks
	 * whether the new string is eligible for being used as a variable name (i.e. it
	 * doesn't start with a digit and is not a java key word).
	 * <p>
	 * For instance, the following string:
	 * <p>
	 * {@code "_MY_var$name"}
	 * <p>
	 * is converted to:
	 * <p>
	 * {@code "myVarName"}
	 * <p>
	 * 
	 * @param identifier
	 *            the string to be converted
	 * @return Optional of a camel-cased string if it is a valid variable name,
	 *         or an empty optional otherwise.
	 */
	public static Optional<String> generateNewIdetifier(String identifier) {
		// split by $ or by _ or by upper-case letters
		String[] parts = identifier.split("\\$|_|(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"); //$NON-NLS-1$
	
		List<String> partsList = Arrays.asList(parts).stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
	
		String newName = null;
		if (!partsList.isEmpty()) {
			// the prefix has to start with lower case
			String prefix = partsList.remove(0);
			String lowerCasePrefix = prefix.toLowerCase();
	
			// convert the other parts to Title case.
			String suffix = partsList.stream().filter(s -> !s.isEmpty()).map(String::toLowerCase)
					.map(input -> input.substring(0, 1).toUpperCase() + input.substring(1))
					.collect(Collectors.joining());
	
			// the final identifier
			String camelCasedIdenitfier = lowerCasePrefix + suffix;
	
			// check if it is eligible variable name
			if (!JavaReservedKeyWords.isKeyWord(camelCasedIdenitfier)
					&& !Character.isDigit(camelCasedIdenitfier.charAt(0))) {
				newName = camelCasedIdenitfier;
			}
		}
	
		return Optional.ofNullable(newName).filter(s -> !s.isEmpty());
	}

}

package at.splendit.simonykees.core.visitor.renaming;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;

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

	/**
	 * Checks if the type declaration has a field named the same as the given
	 * name.
	 * @param typeDeclaration
	 *            a type declaration
	 * @param fragmentName
	 *            the name to look for
	 * 
	 * @return {@code true} if such a field is found, or {@code false} otherwise
	 */
	public static boolean hasField(TypeDeclaration typeDeclaration, SimpleName fragmentName) {
		return hasField(typeDeclaration, fragmentName.getIdentifier());
	}
	
	/**
	 * Checks if the type declaration has a field named the same as the given
	 * identifier.
	 * 
	 * @param typeDeclaration
	 *            a type declaration
	 * @param targetIdentifier
	 *            the name to look for
	 * @return {@code true} if such a field is found, or {@code false} otherwise
	 */
	public static boolean hasField(TypeDeclaration typeDeclaration, String targetIdentifier) {
		return ASTNodeUtil.convertToTypedList(typeDeclaration.bodyDeclarations(), FieldDeclaration.class).stream()
				.flatMap(fieldDecl -> ASTNodeUtil
						.convertToTypedList(fieldDecl.fragments(), VariableDeclarationFragment.class).stream()
						.map(VariableDeclarationFragment::getName).map(SimpleName::getIdentifier))
				.anyMatch(identifier -> identifier.equals(targetIdentifier))
				|| ClassRelationUtil.findInheretedFields(typeDeclaration.resolveBinding())
						.contains(targetIdentifier);
	}

}

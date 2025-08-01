package eu.jsparrow.core.visitor.renaming;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * A utility class containing functionalities that check/generates identifiers
 * according to the java conventions for variable names. 
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class NamingConventionUtil {

	private NamingConventionUtil() {
		/*
		 * Hiding the default public constructor.
		 */
	}
	
	/**
	 * Uses the regular expression {@code ^[a-z][a-zA-Z0-9]*$} for checking
	 * whether an identifier complies with the java conventions for variable
	 * names.
	 * 
	 * @param identifier
	 *            identifier to be checked.
	 * @return whether the identifier complies with java conventions.
	 */
	public static boolean isComplyingWithConventions(String identifier) {
		// the following regex is taken from sonarqube
		return identifier.matches("^[a-z][a-zA-Z0-9]*$"); //$NON-NLS-1$
	}

	/**
	 * Converts the given string to camelCase by removing the non-alphanumeric
	 * symbols '$' and '_' and capitalizing the character which is following
	 * them, unless it is the fist character of the string. Furthermore, checks
	 * whether the new string is eligible for being used as a variable name
	 * (i.e. it doesn't start with a digit and is not a java key word).
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
	public static Optional<String> generateNewIdentifier(String identifier) {
		// split by $ or by _ or by upper-case letters
		String[] parts = identifier.split("\\$|_|(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"); //$NON-NLS-1$

		List<String> partsList = Arrays.asList(parts)
			.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.collect(Collectors.toList());

		if (partsList.isEmpty()) {
			return Optional.empty();
		}

		String newName = null;

		// the prefix has to start with lower case
		String prefix = partsList.remove(0);
		String lowerCasePrefix = StringUtils.lowerCase(prefix);

		// convert the other parts to Title case.
		String suffix = partsList.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.map(String::toLowerCase)
			.map(input -> StringUtils
				.upperCase(input.substring(0, 1)) + StringUtils.substring(input, 1))
			.collect(Collectors.joining());

		// the final identifier
		String camelCasedIdenitfier = lowerCasePrefix + suffix;

		// check if it is eligible variable name
		if (!JavaReservedKeyWords.isKeyWord(camelCasedIdenitfier)
				&& !Character.isDigit(camelCasedIdenitfier.charAt(0))) {
			newName = camelCasedIdenitfier;
		}

		return Optional.ofNullable(newName)
			.filter(s -> !StringUtils.isEmpty(s));
	}
	
	public static Optional<String> generateNewIdentifier(String identifier, boolean upperCaseAfterDollar,
			boolean upperCaseAfterUScore) {
		String charFreeId = identifier;
		if (!upperCaseAfterDollar) {
			charFreeId = charFreeId.replace("$", ""); //$NON-NLS-1$//$NON-NLS-2$
		}

		if (!upperCaseAfterUScore) {
			charFreeId = charFreeId.replace("_", ""); //$NON-NLS-1$//$NON-NLS-2$
		}
		return generateNewIdentifier(charFreeId);
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

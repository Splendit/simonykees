package eu.jsparrow.rules.common.visitor.helper;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Offers a method to create new variable names which are guaranteed not to
 * cause compiler errors due to duplicate variable names.
 * 
 * @since 4.20.0
 */
public class SafeVariableNameFactory extends LiveVariableScope {

	/**
	 * 
	 * @return a String which represents a new variable identifier which is
	 *         guaranteed not to be a duplicate variable name.
	 */
	public String createSafeVariableName(ASTNode astNode, String identifier) {
		ASTNode enclosingScope = findEnclosingScope(astNode)
			.orElse(null);
		lazyLoadScopeNames(enclosingScope);
		// creating safe identifier which is not a duplicate
		String name = identifier;
		int suffix = 1;
		while (isInScope(name)) {
			name = identifier + suffix;
			suffix++;
		}
		// storing name for enclosing Scope
		addName(enclosingScope, name);
		return name;
	}
}

package eu.jsparrow.rules.common.visitor.helper;

import org.eclipse.jdt.core.dom.ASTNode;

public class SafeVariableNameFactory extends LiveVariableScope {

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

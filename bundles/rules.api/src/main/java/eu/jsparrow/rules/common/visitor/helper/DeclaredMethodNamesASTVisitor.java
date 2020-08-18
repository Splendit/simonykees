package eu.jsparrow.rules.common.visitor.helper;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * A visitor for collecting the names of methods declared in a compilation unit.
 *
 * @since 3.21.0
 */
public class DeclaredMethodNamesASTVisitor extends ASTVisitor {

	private Set<String> declaredMethodNames = new HashSet<>();

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		declaredMethodNames.add(methodDeclaration.getName()
			.getIdentifier());
		return true;
	}

	public Set<String> getDeclaredMethodNames() {
		return declaredMethodNames;
	}

}

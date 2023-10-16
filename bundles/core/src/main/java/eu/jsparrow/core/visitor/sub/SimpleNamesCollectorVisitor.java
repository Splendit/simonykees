package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Collects {@link SimpleName}-nodes which have an expected identifier.
 * 
 * @since 4.20.0
 */
public class SimpleNamesCollectorVisitor extends ASTVisitor {

	private final String expectedIdentifier;
	private final List<SimpleName> matchingSimpleNames = new ArrayList<>();

	public SimpleNamesCollectorVisitor(String expectedIdentifier) {
		this.expectedIdentifier = expectedIdentifier;
	}

	@Override
	public boolean visit(SimpleName node) {
		if (node.getIdentifier()
			.equals(expectedIdentifier)) {
			matchingSimpleNames.add(node);
		}
		return false;
	}

	public List<SimpleName> getMatchingSimpleNames() {
		return matchingSimpleNames;
	}
}

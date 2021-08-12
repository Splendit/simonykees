package eu.jsparrow.rules.java16;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.SimpleName;

class UsePatternMatchingForInstanceofData {

	private final InstanceofExpression instanceOf;
	private final SimpleName patternInstanceOfName;
	private final ASTNode declarationNodeToRemove;

	UsePatternMatchingForInstanceofData(InstanceofExpression instanceOf, SimpleName patternInstanceOfName,
			ASTNode declarationNodeToRemove) {
		this.instanceOf = instanceOf;
		this.patternInstanceOfName = patternInstanceOfName;
		this.declarationNodeToRemove = declarationNodeToRemove;
	}

	InstanceofExpression getInstanceOf() {
		return instanceOf;
	}

	SimpleName getPatternInstanceOfName() {
		return patternInstanceOfName;
	}

	ASTNode getDeclarationNodeToRemove() {
		return declarationNodeToRemove;
	}
}

package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IfStatement;

class IfStatementsCollector {
	static List<IfStatement> collectIfStatements(ASTNode node) {
		List<IfStatement> ifStatements = new ArrayList<>();
		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public boolean visit(IfStatement node) {
				ifStatements.add(node);
				return false;
			}
		};
		node.accept(visitor);
		return ifStatements;
	}

	private IfStatementsCollector() {

	}
}

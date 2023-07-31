package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.markers.common.InlineLocalVariablesEvent;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * A visitor that searches for nested {@link IfStatement} and collapses them to
 * a single one if possible. Introduces a boolean variable to store the
 * condition if it contains more than 2 components.
 * 
 * @since 3.2.0
 *
 */
public class InlineLocalVariablesASTVisitor extends AbstractASTRewriteASTVisitor implements InlineLocalVariablesEvent {

	@Override
	public boolean visit(VariableDeclarationFragment declarationFragment) {

		Expression initializer = declarationFragment.getInitializer();
		if (initializer == null) {
			return false;
		}

		if (declarationFragment.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return true;
		}

		VariableDeclarationStatement declarationStatement = (VariableDeclarationStatement) declarationFragment
			.getParent();
		SimpleName fragmentName = declarationFragment.getName();
		LocalVariableUsagesVisitor usageVisitor = new LocalVariableUsagesVisitor(fragmentName);
		declarationStatement.getParent()
			.accept(usageVisitor);

		List<SimpleName> usages = usageVisitor.getUsages();
		usages.remove(fragmentName);
		if (usages.size() != 1) {
			return true;
		}

		SimpleName usageToReplace = usages.get(0);

		ASTNode initializerMoved = astRewrite.createMoveTarget(initializer);

		astRewrite.replace(usageToReplace, initializerMoved, null);

		if (declarationStatement.fragments()
			.size() == 1) {
			astRewrite.remove(declarationStatement, null);
		} else {
			astRewrite.remove(declarationFragment, null);
		}

		addMarkerEvent(declarationFragment);
		onRewrite();

		return true;
	}

}
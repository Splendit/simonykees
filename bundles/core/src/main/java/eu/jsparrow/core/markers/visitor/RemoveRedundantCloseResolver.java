package eu.jsparrow.core.markers.visitor;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.RemoveRedundantCloseRule;
import eu.jsparrow.core.visitor.impl.trycatch.close.RemoveRedundantCloseASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link RemoveRedundantCloseASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public class RemoveRedundantCloseResolver extends RemoveRedundantCloseASTVisitor implements Resolver {

	public static final String ID = "RemoveRedundantCloseResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public RemoveRedundantCloseResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(RemoveRedundantCloseRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(TryStatement node) {
		List<VariableDeclarationFragment> resourceDeclarations = collectResourceDeclarations(node);
		for (VariableDeclarationFragment resourceDeclaration : resourceDeclarations) {
			ExpressionStatement closeStatement = findRedundantCloseStatementToRemove(node, resourceDeclaration)
				.filter(positionChecker::test)
				.orElse(null);
			if (closeStatement != null) {
				super.visit(node);
				break;
			}
		}
		return true;
	}

	@Override
	protected void transform(ExpressionStatement closeStatement) {
		if (positionChecker.test(closeStatement)) {
			super.transform(closeStatement);
		}
	}

	@Override
	public void addMarkerEvent(ExpressionStatement node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}
}

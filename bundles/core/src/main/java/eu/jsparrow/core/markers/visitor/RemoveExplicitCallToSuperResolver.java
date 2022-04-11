package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.RemoveExplicitCallToSuperRule;
import eu.jsparrow.core.visitor.impl.RemoveExplicitCallToSuperASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

public class RemoveExplicitCallToSuperResolver extends RemoveExplicitCallToSuperASTVisitor implements Resolver {

	public static final String ID = "RemoveExplicitCallToSuperResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public RemoveExplicitCallToSuperResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(RemoveExplicitCallToSuperRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(SuperConstructorInvocation superInvocation) {
		if (positionChecker.test(superInvocation)) {
			super.visit(superInvocation);
		}
		return true;
	}

	@Override
	public void addMarkerEvent(SuperConstructorInvocation node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}
}

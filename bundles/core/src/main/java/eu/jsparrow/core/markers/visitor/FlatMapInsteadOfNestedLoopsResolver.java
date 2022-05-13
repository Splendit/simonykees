package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.FlatMapInsteadOfNestedLoopsRule;
import eu.jsparrow.core.visitor.impl.FlatMapInsteadOfNestedLoopsASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

public class FlatMapInsteadOfNestedLoopsResolver extends FlatMapInsteadOfNestedLoopsASTVisitor implements Resolver {

	public static final String ID = "FlatMapInsteadOfNestedLoopsResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public FlatMapInsteadOfNestedLoopsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(FlatMapInsteadOfNestedLoopsRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation)) {
			super.visit(methodInvocation);
		}
		return true;
	}

	@Override
	public void endVisit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation)) {
			super.endVisit(methodInvocation);
		}
	}

	@Override
	public void addMarkerEvent(MethodInvocation node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}
}

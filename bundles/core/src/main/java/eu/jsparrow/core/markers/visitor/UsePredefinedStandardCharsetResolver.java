package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.UsePredefinedStandardCharsetRule;
import eu.jsparrow.core.visitor.impl.UsePredefinedStandardCharsetASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link UsePredefinedStandardCharsetASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public class UsePredefinedStandardCharsetResolver extends UsePredefinedStandardCharsetASTVisitor implements Resolver {

	public static final String ID = "UsePredefinedStandardCharsetResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public UsePredefinedStandardCharsetResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(UsePredefinedStandardCharsetRule.RULE_ID);
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
	public void addMarkerEvent(MethodInvocation node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}

}

package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.DateDeprecatedRule;
import eu.jsparrow.core.visitor.impl.DateDeprecatedASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

public class DateDeprecatedResolver extends DateDeprecatedASTVisitor implements Resolver {
	public static final String ID = "DateDeprecatedResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public DateDeprecatedResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(DateDeprecatedRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(ClassInstanceCreation classInstanceCreation) {
		if (positionChecker.test(classInstanceCreation)) {
			super.visit(classInstanceCreation);
		}
		return true;
	}

	@Override
	public void addMarkerEvent(ClassInstanceCreation node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}

}

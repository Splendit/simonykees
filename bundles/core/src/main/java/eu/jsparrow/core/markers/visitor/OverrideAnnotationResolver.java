package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.OverrideAnnotationRule;
import eu.jsparrow.core.visitor.impl.OverrideAnnotationRuleASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

public class OverrideAnnotationResolver extends OverrideAnnotationRuleASTVisitor implements Resolver {

	public static final String ID = "OverrideAnnotationResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public OverrideAnnotationResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(OverrideAnnotationRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		SimpleName name = methodDeclaration.getName();
		if (positionChecker.test(name)) {
			super.visit(methodDeclaration);
		}
		return true;
	}

	@Override
	public void addMarkerEvent(MethodDeclaration node) {
		SimpleName name = node.getName();
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), name, ID, description);
		addMarkerEvent(event);
	}
}

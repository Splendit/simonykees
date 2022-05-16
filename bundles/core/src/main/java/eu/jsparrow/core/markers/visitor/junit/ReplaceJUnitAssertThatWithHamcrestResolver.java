package eu.jsparrow.core.markers.visitor.junit;

import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.ReplaceJUnitAssertThatWithHamcrestRule;
import eu.jsparrow.core.visitor.junit.ReplaceJUnitAssertThatWithHamcrestASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link ReplaceJUnitAssertThatWithHamcrestASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public class ReplaceJUnitAssertThatWithHamcrestResolver extends ReplaceJUnitAssertThatWithHamcrestASTVisitor
		implements Resolver {

	public static final String ID = "ReplaceJUnitAssertThatWithHamcrestResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public ReplaceJUnitAssertThatWithHamcrestResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(ReplaceJUnitAssertThatWithHamcrestRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		if (positionChecker.test(node)) {
			super.visit(node);
		}
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (positionChecker.test(node)) {
			super.visit(node);
		}
		return true;
	}

	@Override
	public void addMarkerEvent(ImportDeclaration node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}

	@Override
	public void addMarkerEvent(MethodInvocation node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}
}

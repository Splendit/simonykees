package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.BracketsToControlRule;
import eu.jsparrow.core.visitor.impl.BracketsToControlASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link BracketsToControlASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public class BracketsToControlResolver extends BracketsToControlASTVisitor implements Resolver {
	public static final String ID = "BracketsToControlResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public BracketsToControlResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(BracketsToControlRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(ForStatement node) {
		Statement body = node.getBody();
		if (positionChecker.test(body)) {
			super.visit(node);
		}
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		Statement body = node.getBody();
		if (positionChecker.test(body)) {
			super.visit(node);
		}
		return true;
	}

	@Override
	public boolean visit(WhileStatement node) {
		Statement body = node.getBody();
		if (positionChecker.test(body)) {
			super.visit(node);
		}
		return true;
	}

	@Override
	public boolean visit(DoStatement node) {
		Statement body = node.getBody();
		if (positionChecker.test(body)) {
			super.visit(node);
		}
		return true;
	}

	@Override
	public boolean visit(IfStatement node) {
		Statement body = node.getThenStatement();
		if (positionChecker.test(body)) {
			super.visit(node);
		}
		return true;
	}

	@Override
	public void addMarkerEvent(Statement node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}

}

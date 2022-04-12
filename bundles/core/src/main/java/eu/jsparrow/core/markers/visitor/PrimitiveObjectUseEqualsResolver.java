package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.PrimitiveObjectUseEqualsRule;
import eu.jsparrow.core.visitor.impl.PrimitiveObjectUseEqualsASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link PrimitiveObjectUseEqualsASTVisitor}.
 * 
 * @since 4.10.0
 *
 */
public class PrimitiveObjectUseEqualsResolver extends PrimitiveObjectUseEqualsASTVisitor implements Resolver {

	public static final String ID = "PrimitiveObjectUseEqualsResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public PrimitiveObjectUseEqualsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(PrimitiveObjectUseEqualsRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(InfixExpression infixExpression) {
		if (positionChecker.test(infixExpression)) {
			super.visit(infixExpression);
		}
		return true;
	}

	@Override
	public void addMarkerEvent(InfixExpression node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}
}

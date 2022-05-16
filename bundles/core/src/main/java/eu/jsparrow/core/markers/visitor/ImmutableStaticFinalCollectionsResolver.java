package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.ImmutableStaticFinalCollectionsRule;
import eu.jsparrow.core.visitor.impl.ImmutableStaticFinalCollectionsASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link ImmutableStaticFinalCollectionsASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public class ImmutableStaticFinalCollectionsResolver extends ImmutableStaticFinalCollectionsASTVisitor
		implements Resolver {

	public static final String ID = "ImmutableStaticFinalCollectionsResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public ImmutableStaticFinalCollectionsResolver(Predicate<ASTNode> positionChecker) {
		super(JavaCore.VERSION_1_8);
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(ImmutableStaticFinalCollectionsRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(VariableDeclarationFragment fragmentNode) {
		if (positionChecker.test(fragmentNode)) {
			return super.visit(fragmentNode);
		}

		return true;
	}

	@Override
	public void addMarkerEvent(VariableDeclarationFragment node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}
}

package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NormalAnnotation;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.ReplaceRequestMappingAnnotationRule;
import eu.jsparrow.core.visitor.impl.UseOffsetBasedStringMethodsASTVisitor;
import eu.jsparrow.core.visitor.spring.ReplaceRequestMappingAnnotationASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link UseOffsetBasedStringMethodsASTVisitor}.
 * 
 * @since 4.10.0
 *
 */
public class ReplaceRequestMappingAnnotationResolver extends ReplaceRequestMappingAnnotationASTVisitor implements Resolver {

	public static final String ID = "ReplaceRequestMappingAnnotationEventResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public ReplaceRequestMappingAnnotationResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(ReplaceRequestMappingAnnotationRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(NormalAnnotation normalAnnotation) {
		if (positionChecker.test(normalAnnotation)) {
			super.visit(normalAnnotation);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(NormalAnnotation node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}
}

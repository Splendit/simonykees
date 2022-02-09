package eu.jsparrow.core.markers.visitor.optional;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LambdaExpression;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.OptionalMapRule;
import eu.jsparrow.core.visitor.optional.OptionalMapASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type {@link OptionalMapASTVisitor}.
 * 
 * @since 4.8.0
 * 
 */
public class OptionalMapResolver extends OptionalMapASTVisitor implements Resolver {

	public static final String ID = "OptionalMapResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public OptionalMapResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(OptionalMapRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(LambdaExpression lambdaExpression) {
		if (positionChecker.test(lambdaExpression)) {
			super.visit(lambdaExpression);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(LambdaExpression lambdaExpression) {
		int credit = description.getCredit();
		int highlightLength = 0;
		int offset = lambdaExpression.getStartPosition();
		int length = lambdaExpression.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(lambdaExpression.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(description.getDescription())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}
}

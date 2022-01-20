package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.UseComparatorMethodsRule;
import eu.jsparrow.core.visitor.impl.comparatormethods.UseComparatorMethodsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link UseComparatorMethodsASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public class UseComparatorMethodsResolver extends UseComparatorMethodsASTVisitor implements Resolver {

	public static final String ID = "UseComparatorMethodsResolver"; //$NON-NLS-1$
	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public UseComparatorMethodsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
				.findByRuleId(UseComparatorMethodsRule.RULE_ID);
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
	public void addMarkerEvent(LambdaExpression lambda, MethodInvocation lambdaReplacement) {
		int highlightLength = lambdaReplacement.toString()
			.length();
		int credit = description.getCredit();
		int offset = lambda.getStartPosition();
		int length = lambda.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(lambda.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(Messages.UseComparatorMethodsResolver_name)
			.withMessage(Messages.UseComparatorMethodsResolver_message)
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(lambdaReplacement.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}
}

package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.UseComparatorMethodsRule;
import eu.jsparrow.core.visitor.impl.comparatormethods.UseComparatorMethodsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * A visitor for resolving one issue of type
 * {@link UseComparatorMethodsASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public class UseComparatorMethodsResolver extends UseComparatorMethodsASTVisitor {

	public static final String ID = UseComparatorMethodsResolver.class.getName();
	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public UseComparatorMethodsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
				.findByRuleId(UseComparatorMethodsRule.RULE_ID);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
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
		int highlightLenght = lambdaReplacement.toString()
			.length();
		int credit = description.getCredit();
		RefactoringEventImpl event = new RefactoringEventImpl(ID, Messages.UseComparatorMethodsResolver_name,
				Messages.UseComparatorMethodsResolver_message, javaElement,
				highlightLenght, lambda, lambdaReplacement, credit);
		addMarkerEvent(event);
	}
}

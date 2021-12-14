package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.markers.common.Resolver;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.AvoidConcatenationInLoggingStatementsRule;
import eu.jsparrow.core.visitor.impl.AvoidConcatenationInLoggingStatementsASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * A visitor for resolving one issue of type
 * {@link AvoidConcatenationInLoggingStatementsASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public class AvoidConcatenationInLoggingStatementsResolver extends AvoidConcatenationInLoggingStatementsASTVisitor implements Resolver {

	public static final String ID = AvoidConcatenationInLoggingStatementsResolver.class.getName();

	private Predicate<ASTNode> positionChecker;
	private IJavaElement javaElement;
	private RuleDescription description;

	public AvoidConcatenationInLoggingStatementsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(AvoidConcatenationInLoggingStatementsRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation)) {
			super.visit(methodInvocation);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(InfixExpression infixExpression, ASTNode newNode) {
		int credit = description.getCredit();
		int highlightLength = newNode.getLength();
		RefactoringMarkerEvent event = new RefactoringEventImpl(ID,
				description.getName(),
				description.getDescription(),
				javaElement,
				highlightLength,
				infixExpression.getParent(), newNode, credit);
		addMarkerEvent(event);
	}
}

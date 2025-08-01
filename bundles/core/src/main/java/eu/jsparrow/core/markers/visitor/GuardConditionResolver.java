package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.GuardConditionRule;
import eu.jsparrow.core.visitor.impl.GuardConditionASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * 
 * A visitor for resolving one issue of type {@link GuardConditionASTVisitor}.
 * 
 * @since 4.9.0
 *
 */
public class GuardConditionResolver extends GuardConditionASTVisitor implements Resolver {

	public static final String ID = "GuardConditionResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public GuardConditionResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(GuardConditionRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		return true;
	}

	@Override
	public boolean visit(IfStatement ifStatement) {
		if (positionChecker.test(ifStatement)) {
			MethodDeclaration methodDeclaration = ASTNodeUtil.getSpecificAncestor(ifStatement, MethodDeclaration.class);
			super.visit(methodDeclaration);
			return false;
		}
		return true;
	}

	@Override
	public void addMarkerEvent(IfStatement node) {
		int credit = description.getCredit();
		int highlightLength = 0;
		int offset = node.getStartPosition();
		int length = node.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(node.getStartPosition());
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

package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;

import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;
import eu.jsparrow.rules.java16.switchexpression.UseSwitchExpressionASTVisitor;

/**
 * A visitor for resolving one issue of type
 * {@link UseSwitchExpressionASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public class ReplaceMultiBranchIfBySwitchResolver extends ReplaceMultiBranchIfBySwitchASTVisitor implements Resolver {

	public static final String ID = "ReplaceMultiBranchIfBySwitchResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public ReplaceMultiBranchIfBySwitchResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = new ReplaceMultiBranchIfBySwitchRule().getRuleDescription();
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(IfStatement ifStatement) {
		if (positionChecker.test(ifStatement)) {
			super.visit(ifStatement);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(IfStatement ifStatement) {
		int credit = description.getCredit();
		int highlightLength = 0;
		int offset = ifStatement.getStartPosition();
		int length = ifStatement.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(ifStatement.getStartPosition());
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

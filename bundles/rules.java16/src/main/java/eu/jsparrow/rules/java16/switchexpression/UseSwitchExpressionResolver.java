package eu.jsparrow.rules.java16.switchexpression;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;

import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

public class UseSwitchExpressionResolver extends UseSwitchExpressionASTVisitor implements Resolver {

	public static final String ID = "UseSwitchExpressionResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public UseSwitchExpressionResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = new UseSwitchExpressionRule().getRuleDescription();
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(SwitchStatement switchStatement) {
		if (positionChecker.test(switchStatement)) {
			super.visit(switchStatement);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(SwitchStatement switchStatement, Statement newNode) {
		int credit = description.getCredit();
		int highlightLength = newNode.getLength();
		int offset = switchStatement.getStartPosition();
		int length = switchStatement.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(switchStatement.getStartPosition());
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

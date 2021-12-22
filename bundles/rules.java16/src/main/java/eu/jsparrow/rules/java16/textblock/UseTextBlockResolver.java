package eu.jsparrow.rules.java16.textblock;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.TextBlock;

import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

public class UseTextBlockResolver extends UseTextBlockASTVisitor
	 implements Resolver {

	public static final String ID = "UseTextBlockResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public UseTextBlockResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = new UseTextBlockRule().getRuleDescription();
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
		return false;
	}

	@Override
	public void addMarkerEvent(InfixExpression infixExpression, TextBlock newNode) {
		int credit = description.getCredit();
		int highlightLength = newNode.getLength();
		int offset = infixExpression.getStartPosition();
		int length = infixExpression.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(infixExpression.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(newNode.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}
}

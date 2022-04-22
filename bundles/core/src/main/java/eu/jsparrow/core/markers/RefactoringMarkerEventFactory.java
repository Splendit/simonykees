package eu.jsparrow.core.markers;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

public class RefactoringMarkerEventFactory {
	
	private RefactoringMarkerEventFactory() {
		/*
		 * Hide the default constructor.
		 */
	}

	public static RefactoringMarkerEvent createEventForNode(CompilationUnit compilationUnit, ASTNode node, String id, RuleDescription description) {
		int credit = description.getCredit();
		int highlightLength = 0;
		int offset = node.getStartPosition();
		int length = node.getLength();
		int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
		IJavaElement javaElement = compilationUnit.getJavaElement();
		return new RefactoringEventImpl.Builder()
			.withResolver(id)
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
	}
}

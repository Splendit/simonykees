package eu.jsparrow.rules.java16.javarecords;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type {@link UseJavaRecordsASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public class UseJavaRecordsResolver extends UseJavaRecordsASTVisitor implements Resolver {
	public static final String ID = "UseJavaRecordsResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public UseJavaRecordsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = new UseJavaRecordsRule().getRuleDescription();
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		if (positionChecker.test(typeDeclaration)) {
			super.visit(typeDeclaration);
		}
		return true;
	}

	@Override
	public void addMarkerEvent(TypeDeclaration switchStatement) {
		int credit = description.getCredit();
		int highlightLength = 0;
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

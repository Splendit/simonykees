package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.impl.StringLiteralEqualityCheckASTVisitor;

public class StringLiteralEqualityCheckResolver extends StringLiteralEqualityCheckASTVisitor {

	private static final String NAME = "Reorder String equality check"; //$NON-NLS-1$
	private static final String MESSAGE = "To avoid NullPointerExceptions, String literals should be placed on the left side when checking for equality."; //$NON-NLS-1$
	public static final String ID = StringLiteralEqualityCheckResolver.class.getName();
	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;

	public StringLiteralEqualityCheckResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(StringLiteral stringLiteral) {
		if (positionChecker.test(stringLiteral)) {
			return super.visit(stringLiteral);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(ASTNode original, ASTNode newNode) {
		RefactoringEventImpl event = new RefactoringEventImpl(ID, NAME, MESSAGE,
				javaElement, original,
				newNode);
		addMarkerEvent(event);
	}
}

package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.impl.RemoveNullCheckBeforeInstanceofASTVisitor;

public class RemoveNullCheckBeforeInstanceofResolver extends RemoveNullCheckBeforeInstanceofASTVisitor {

	private static final String MARKER_NAME = "Remove Null-Checks Before Instanceof"; //$NON-NLS-1$
	private static final String MARKER_DESCRIPTION = "null is not an instance of anything, therefore the null-check is redundant."; //$NON-NLS-1$
	public static final String RESOLVER_NAME = RemoveNullCheckBeforeInstanceofResolver.class.getName();
	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;

	public RemoveNullCheckBeforeInstanceofResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(InstanceofExpression instanceOfExpression) {
		ASTNode parent = instanceOfExpression.getParent();
		if (parent.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			InfixExpression infixExpression = (InfixExpression) parent;
			if (positionChecker.test(infixExpression.getLeftOperand())) {
				super.visit(instanceOfExpression);
			}
		}
		return false;
	}

	@Override
	public void addMarkerEvent(ASTNode original, ASTNode newNode) {
		RefactoringEventImpl event = new RefactoringEventImpl(RESOLVER_NAME, MARKER_NAME, MARKER_DESCRIPTION,
				javaElement, original,
				newNode);
		addMarkerEvent(event);
	}

}

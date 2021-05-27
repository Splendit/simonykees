package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LambdaExpression;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.impl.LambdaToMethodReferenceASTVisitor;

public class LambdaToMethodReferenceResolver extends LambdaToMethodReferenceASTVisitor {

	private static final String NAME = "Replace lambda expression with method reference"; //$NON-NLS-1$
	private static final String MESSAGE = "Simplify the lambda expression by using a method reference."; //$NON-NLS-1$
	public static final String ID = LambdaToMethodReferenceResolver.class.getName();

	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;

	public LambdaToMethodReferenceResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(LambdaExpression lambdaExpressionNode) {
		if (positionChecker.test(lambdaExpressionNode)) {
			return super.visit(lambdaExpressionNode);
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

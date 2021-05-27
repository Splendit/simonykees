package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.impl.UseIsEmptyOnCollectionsASTVisitor;

public class UseIsEmptyOnCollectionsResolver extends UseIsEmptyOnCollectionsASTVisitor {

	private static final String NAME = "Replace Equality Check with isEmpty()"; //$NON-NLS-1$
	private static final String MESSAGE = "Use isEmpty() on Strings, Maps, and Collections."; //$NON-NLS-1$
	public static final String ID = UseIsEmptyOnCollectionsResolver.class.getName();
	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;

	public UseIsEmptyOnCollectionsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation.getParent())) {
			return super.visit(methodInvocation);
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

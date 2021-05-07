package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.functionalinterface.FunctionalInterfaceASTVisitor;

public class FunctionalInterfaceResolver extends FunctionalInterfaceASTVisitor {

	private static final String MARKER_NAME = "Replace with Lambda Expression"; //$NON-NLS-1$
	private static final String MARKER_DESCRIPTION = "Anonymous class can be replaced by lambda expression"; //$NON-NLS-1$
	public static final String RESOLVER_NAME = FunctionalInterfaceResolver.class.getName();

	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;

	public FunctionalInterfaceResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		if (positionChecker.test(node.getParent())) {
			return super.visit(node);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(ASTNode original, ASTNode newNode) {
		RefactoringEventImpl event = new RefactoringEventImpl(RESOLVER_NAME, MARKER_NAME, MARKER_DESCRIPTION, javaElement, original,
				newNode);
		addMarkerEvent(event);
	}
}

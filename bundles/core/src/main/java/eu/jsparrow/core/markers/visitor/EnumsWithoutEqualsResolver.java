package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.impl.EnumsWithoutEqualsASTVisitor;

public class EnumsWithoutEqualsResolver extends EnumsWithoutEqualsASTVisitor {

	private static final String MARKER_NAME = "Replace equals() on Enum constants"; //$NON-NLS-1$
	private static final String MARKER_DESCRIPTION = "Replace occurrences of equals() on Enum constants with an identity comparison (==)."; //$NON-NLS-1$
	public static final String RESOLVER_NAME = EnumsWithoutEqualsResolver.class.getName();

	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;

	public EnumsWithoutEqualsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		boolean isRelevant = positionChecker.test(node) || positionChecker.test(node.getParent());
		if (isRelevant) {
			return super.visit(node);
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

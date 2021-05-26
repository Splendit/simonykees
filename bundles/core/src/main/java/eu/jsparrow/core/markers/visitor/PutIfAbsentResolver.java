package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.impl.PutIfAbsentASTVisitor;

public class PutIfAbsentResolver extends PutIfAbsentASTVisitor {

	private static final String MARKER_NAME = "Replace put(..) with putIfAbsent(..)"; //$NON-NLS-1$
	private static final String MARKER_DESCRIPTION = "Use the Java 8 API that allows for conditionally adding entries to a map."; //$NON-NLS-1$
	public static final String RESOLVER_NAME = PutIfAbsentResolver.class.getName();
	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;

	public PutIfAbsentResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation)) {
			return super.visit(methodInvocation);
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

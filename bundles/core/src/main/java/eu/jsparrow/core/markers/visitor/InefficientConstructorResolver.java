package eu.jsparrow.core.markers.visitor;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.impl.InefficientConstructorASTVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class InefficientConstructorResolver extends InefficientConstructorASTVisitor {

	private static final String MARKER_NAME = "Replace Inefficient Constructors with valueOf()"; //$NON-NLS-1$
	private static final String MARKER_DESCRIPTION = "The factory method valueOf() is generally a better choice as it is likely to yield significantly better space and time performance."; //$NON-NLS-1$
	public static final String RESOLVER_NAME = InefficientConstructorResolver.class.getName();

	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;

	public InefficientConstructorResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(node.arguments(), Expression.class);
		if (arguments.isEmpty()) {
			return false;
		}
		Expression argument = arguments.get(0);
		if (positionChecker.test(argument)) {
			return super.visit(node);
		}
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (positionChecker.test(node)) {
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

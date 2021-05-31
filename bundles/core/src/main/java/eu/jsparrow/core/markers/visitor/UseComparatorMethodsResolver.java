package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.impl.comparatormethods.UseComparatorMethodsASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * A visitor for resolving one issue of type
 * {@link UseComparatorMethodsASTVisitor}.
 * 
 * @since 3.31.0
 *
 */
public class UseComparatorMethodsResolver extends UseComparatorMethodsASTVisitor {

	public static final String ID = UseComparatorMethodsResolver.class.getName();
	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;

	public UseComparatorMethodsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(LambdaExpression lambdaExpression) {
		if (positionChecker.test(lambdaExpression)) {
			super.visit(lambdaExpression);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(LambdaExpression lambda, MethodInvocation lambdaReplacement) {
		int highlightLenght = lambdaReplacement.toString()
			.length();
		RefactoringEventImpl event = new RefactoringEventImpl(ID, Messages.UseComparatorMethodsResolver_name,
				Messages.UseComparatorMethodsResolver_message, javaElement,
				highlightLenght, lambda, lambdaReplacement);
		addMarkerEvent(event);
	}
}

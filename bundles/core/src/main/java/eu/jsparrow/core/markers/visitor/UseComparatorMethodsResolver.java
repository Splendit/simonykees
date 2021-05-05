package eu.jsparrow.core.markers.visitor;

import org.eclipse.jdt.core.dom.LambdaExpression;

import eu.jsparrow.core.visitor.impl.comparatormethods.UseComparatorMethodsASTVisitor;

public class UseComparatorMethodsResolver extends UseComparatorMethodsASTVisitor {

	private int offset;

	public UseComparatorMethodsResolver(int offset) {
		this.offset = offset;
	}
	
	@Override
	public boolean visit(LambdaExpression lambdaExpression) {
		int startPosition = lambdaExpression.getStartPosition();
		int endPosition = startPosition + lambdaExpression.getLength();
		if(startPosition <= offset && endPosition >= offset) {
			return super.visit(lambdaExpression);
		}
		return false;
	}
}

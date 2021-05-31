package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.impl.RemoveNullCheckBeforeInstanceofASTVisitor;

public class RemoveNullCheckBeforeInstanceofResolver extends RemoveNullCheckBeforeInstanceofASTVisitor {

	private static final String NAME = "Remove Null-Checks Before Instanceof"; //$NON-NLS-1$
	private static final String MESSAGE = "null is not an instance of anything, therefore the null-check is redundant."; //$NON-NLS-1$
	public static final String ID = RemoveNullCheckBeforeInstanceofResolver.class.getName();
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
	public void addMarkerEvent(Expression leftOperand, InfixExpression infixExpression, Expression expression) {
		ASTNode newNode = createRepresentingNode(infixExpression, expression);
		RefactoringEventImpl event = new RefactoringEventImpl(ID, NAME, MESSAGE,
				javaElement, 0, leftOperand, newNode);
		addMarkerEvent(event);
	}

	private ASTNode createRepresentingNode(InfixExpression infixExpression, Expression expression) {
		AST ast = infixExpression.getAST();
		StructuralPropertyDescriptor structuralProperty = infixExpression.getLocationInParent();
		ASTNode parent = ASTNode.copySubtree(ast, infixExpression.getParent());
		parent.setStructuralProperty(structuralProperty, (Expression)ASTNode.copySubtree(ast, expression));
		return parent;
	}
}

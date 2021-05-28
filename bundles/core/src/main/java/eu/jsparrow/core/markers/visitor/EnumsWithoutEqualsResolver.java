package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.impl.EnumsWithoutEqualsASTVisitor;
import eu.jsparrow.rules.common.builder.NodeBuilder;

public class EnumsWithoutEqualsResolver extends EnumsWithoutEqualsASTVisitor {

	private static final String NAME = "Replace equals() on Enum constants"; //$NON-NLS-1$
	private static final String MESSAGE = "Replace occurrences of equals() on Enum constants with an identity comparison (==)."; //$NON-NLS-1$
	public static final String ID = EnumsWithoutEqualsResolver.class.getName();

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
	public void addMarkerEvent(Expression replacedNode, Expression expression, Expression argument,
			InfixExpression.Operator newOperator) {
		Expression representingNode = createRepresentingNode(expression, argument, newOperator);
		RefactoringEventImpl event = new RefactoringEventImpl(ID, NAME, MESSAGE,
				javaElement, replacedNode,
				representingNode);
		addMarkerEvent(event);

	}

	private Expression createRepresentingNode(Expression expression, Expression argument,
			InfixExpression.Operator newOperator) {
		AST ast = expression.getAST();
		return NodeBuilder.newInfixExpression(ast, newOperator,
				(Expression) ASTNode.copySubtree(ast, expression),
				(Expression) ASTNode.copySubtree(ast, argument));
	}
}

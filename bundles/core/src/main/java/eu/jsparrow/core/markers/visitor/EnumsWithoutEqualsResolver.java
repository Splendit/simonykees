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
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.builder.NodeBuilder;

/**
 * A visitor for resolving one issue of type
 * {@link EnumsWithoutEqualsASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public class EnumsWithoutEqualsResolver extends EnumsWithoutEqualsASTVisitor {

	public static final String ID = EnumsWithoutEqualsResolver.class.getName();
	private static final int WEIGHT_VALUE = 1;

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
		int highlightLength = representingNode.toString()
			.length();
		RefactoringEventImpl event = new RefactoringEventImpl(ID, Messages.EnumsWithoutEqualsResolver_name,
				Messages.EnumsWithoutEqualsResolver_message,
				javaElement,
				highlightLength, replacedNode,
				representingNode,
				WEIGHT_VALUE);
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

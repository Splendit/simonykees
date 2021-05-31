package eu.jsparrow.core.markers.visitor;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.impl.StringLiteralEqualityCheckASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * A visitor for resolving one issue of type
 * {@link StringLiteralEqualityCheckASTVisitor}.
 * 
 * @since 3.31.0
 *
 */
public class StringLiteralEqualityCheckResolver extends StringLiteralEqualityCheckASTVisitor {

	public static final String ID = StringLiteralEqualityCheckResolver.class.getName();
	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;

	public StringLiteralEqualityCheckResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(StringLiteral stringLiteral) {
		if (positionChecker.test(stringLiteral)) {
			return super.visit(stringLiteral);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(StringLiteral stringLiteral, Expression expression) {
		MethodInvocation newNode = createRepresentingNode(expression, stringLiteral);
		RefactoringEventImpl event = new RefactoringEventImpl(ID, Messages.StringLiteralEqualityCheckResolver_name, Messages.StringLiteralEqualityCheckResolver_message,
				javaElement, 0, stringLiteral, newNode);
		addMarkerEvent(event);
	}

	private MethodInvocation createRepresentingNode(Expression expression, StringLiteral stringLiteral) {
		AST ast = expression.getAST();
		MethodInvocation equals = ast.newMethodInvocation();
		equals.setName(ast.newSimpleName(EQUALS));
		equals.setExpression((Expression) ASTNode.copySubtree(ast, stringLiteral));
		@SuppressWarnings("unchecked")
		List<Expression> arguments = equals.arguments();
		arguments.add((Expression) ASTNode.copySubtree(ast, expression));
		return equals;
	}
}

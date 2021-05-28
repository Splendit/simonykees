package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.impl.PrimitiveBoxedForStringASTVisitor;

public class PrimitiveBoxedForStringResolver extends PrimitiveBoxedForStringASTVisitor {

	private static final String NAME = "Remove boxing for String conversions"; //$NON-NLS-1$
	private static final String MESSAGE = "Avoid constructing boxed primitives by using the factory method toString"; //$NON-NLS-1$
	public static final String ID = PrimitiveBoxedForStringResolver.class.getName();
	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;

	public PrimitiveBoxedForStringResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (positionChecker.test(node)) {
			return super.visit(node);
		}
		return false;
	}

	@Override
	public boolean visit(StringLiteral node) {
		if (positionChecker.test(node)) {
			return super.visit(node);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(ASTNode node, Expression refactorCandidateExpression, SimpleName name,
			SimpleName refactorPrimitiveType) {
		MethodInvocation newNode = createRepresentingNode(refactorCandidateExpression, name, refactorPrimitiveType);
		RefactoringEventImpl event = new RefactoringEventImpl(ID, NAME, MESSAGE, javaElement, node, newNode);
		addMarkerEvent(event);
	}

	@SuppressWarnings("unchecked")
	private MethodInvocation createRepresentingNode(Expression refactorCandidateExpression,
			SimpleName methodName,
			SimpleName primitiveType) {
		AST ast = refactorCandidateExpression.getAST();
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		Expression moveTargetArgument = (Expression) ASTNode.copySubtree(ast, refactorCandidateExpression);
		methodInvocation.arguments()
			.add(moveTargetArgument);

		SimpleName staticClassType = astRewrite.getAST()
			.newSimpleName(primitiveType.getIdentifier());
		methodInvocation.setExpression(staticClassType);
		methodInvocation.setName(ast.newSimpleName(methodName.getIdentifier()));
		return methodInvocation;
	}
}

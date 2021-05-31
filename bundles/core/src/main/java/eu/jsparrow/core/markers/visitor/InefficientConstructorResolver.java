package eu.jsparrow.core.markers.visitor;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.constants.ReservedNames;
import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.impl.InefficientConstructorASTVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * A visitor for resolving one issue of type
 * {@link InefficientConstructorASTVisitor}.
 * 
 * @since 3.31.0
 *
 */
public class InefficientConstructorResolver extends InefficientConstructorASTVisitor {

	private static final String NAME = "Replace inefficient constructors with valueOf()"; //$NON-NLS-1$
	private static final String MESSAGE = "The factory method valueOf() is generally a better choice as it is likely to yield significantly better space and time performance."; //$NON-NLS-1$
	public static final String ID = InefficientConstructorResolver.class.getName();

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
	public void addMarkerEvent(Expression refactorCandidateParameter, MethodInvocation node,
			Expression replaceParameter) {
		MethodInvocation newNode = createRepresentingNode(node, replaceParameter);
		RefactoringEventImpl event = new RefactoringEventImpl(ID, NAME, MESSAGE,
				javaElement, 0, refactorCandidateParameter,
				newNode);
		addMarkerEvent(event);
	}

	@Override
	public void addMarkerEvent(ClassInstanceCreation node, SimpleName refactorPrimitiveType,
			Expression refactorCandidateParameter) {
		MethodInvocation newNode = createRepresentingNode(refactorPrimitiveType, refactorCandidateParameter);
		int highlightLenght = newNode.toString()
			.length();
		RefactoringEventImpl event = new RefactoringEventImpl(ID, NAME, MESSAGE,
				javaElement, highlightLenght, node,
				newNode);
		addMarkerEvent(event);
	}

	private MethodInvocation createRepresentingNode(MethodInvocation node, Expression replaceParameter) {
		AST ast = node.getAST();
		MethodInvocation methodInvocation = (MethodInvocation) ASTNode.copySubtree(ast, node);
		@SuppressWarnings("unchecked")
		List<Expression> arguments = methodInvocation.arguments();
		arguments.clear();
		arguments.add((Expression) ASTNode.copySubtree(ast, replaceParameter));
		return methodInvocation;
	}

	private MethodInvocation createRepresentingNode(SimpleName typeName, Expression replaceParameter) {
		AST ast = replaceParameter.getAST();
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setName(ast.newSimpleName(ReservedNames.MI_VALUE_OF));
		@SuppressWarnings("unchecked")
		List<Expression> arguments = methodInvocation.arguments();
		arguments.add((Expression) ASTNode.copySubtree(ast, replaceParameter));
		SimpleName typeNameCopy = (SimpleName) ASTNode.copySubtree(ast, typeName);
		methodInvocation.setExpression(typeNameCopy);
		return methodInvocation;
	}
}

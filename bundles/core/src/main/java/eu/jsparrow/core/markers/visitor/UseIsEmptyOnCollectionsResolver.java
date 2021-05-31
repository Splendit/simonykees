package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.visitor.impl.UseIsEmptyOnCollectionsASTVisitor;
import eu.jsparrow.rules.common.builder.NodeBuilder;

public class UseIsEmptyOnCollectionsResolver extends UseIsEmptyOnCollectionsASTVisitor {

	private static final String NAME = "Replace Equality Check with isEmpty()"; //$NON-NLS-1$
	private static final String MESSAGE = "Use isEmpty() on Strings, Maps, and Collections."; //$NON-NLS-1$
	public static final String ID = UseIsEmptyOnCollectionsResolver.class.getName();
	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;

	public UseIsEmptyOnCollectionsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation.getParent())) {
			return super.visit(methodInvocation);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(InfixExpression parent, Expression varExpression) {
		ASTNode newNode = createRepresentationNode(parent, varExpression);
		RefactoringEventImpl event = new RefactoringEventImpl(ID, NAME, MESSAGE,
				javaElement, 0, parent, newNode);
		addMarkerEvent(event);
	}

	private ASTNode createRepresentationNode(InfixExpression infixExpression, Expression varExpression) {
		AST ast = infixExpression.getAST();
		SimpleName isEmptyMethod = ast.newSimpleName("isEmpty"); //$NON-NLS-1$
		MethodInvocation replaceNode = NodeBuilder.newMethodInvocation(ast,
				(Expression) ASTNode.copySubtree(ast, varExpression), isEmptyMethod);
		StructuralPropertyDescriptor locationInParent = infixExpression.getLocationInParent();
		ASTNode parent = ASTNode.copySubtree(ast, infixExpression.getParent());
		parent.setStructuralProperty(locationInParent, replaceNode);
		return parent;
	}
}

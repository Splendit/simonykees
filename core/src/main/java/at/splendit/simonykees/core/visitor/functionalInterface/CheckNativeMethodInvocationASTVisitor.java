package at.splendit.simonykees.core.visitor.functionalInterface;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

class CheckNativeMethodInvocationASTVisitor extends ASTVisitor{
	
	private static String OBJECT = "java.lang.Object"; //$NON-NLS-1$

	private boolean onlyLegalMethodCalls = true;
	
	@Override
	public boolean preVisit2(ASTNode node) {
		preVisit(node);
		return onlyLegalMethodCalls;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding mb = node.resolveMethodBinding();
		OBJECT.equals(mb.getDeclaringClass().getQualifiedName());
		return true;
	}
	
}

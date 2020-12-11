package eu.jsparrow.core.visitor.functionalinterface;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Checks if tree of accepting node uses Methods that are derived from Object
 * 
 * @author Martin Huter
 * @since 1.2
 *
 */
class CheckNativeMethodInvocationVisitor extends ASTVisitor {

	private static String object = java.lang.Object.class.getName();

	private boolean nonObjectMethodsInvocated = true;

	public boolean objectMethodDeclarationInvocated() {
		return !nonObjectMethodsInvocated;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		preVisit(node);
		return nonObjectMethodsInvocated;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding mb = node.resolveMethodBinding();
		if (mb != null && mb.getDeclaringClass() != null) {
			nonObjectMethodsInvocated = !object.equals(mb.getDeclaringClass()
				.getQualifiedName());
		}
		return nonObjectMethodsInvocated;
	}

}

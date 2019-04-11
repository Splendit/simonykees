package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * A visitor for finding the method invocations of the provided method
 * declaration. Interrupts the search if the {@link IMethodBinding} of any of
 * the method invocations having the same name with the provided method
 * declaration cannot be resolved.
 * 
 * @since 3.4.0
 *
 */
public class MethodInvocationsVisitor extends ASTVisitor {

	private IMethodBinding declaration;
	private String methodName;
	private List<MethodInvocation> invocations = new ArrayList<>();
	private boolean unresolvedBindingsFlag = false;

	public MethodInvocationsVisitor(IMethodBinding declaration) {
		this.declaration = declaration;
		this.methodName = declaration.getName();
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !unresolvedBindingsFlag;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName invocationName = methodInvocation.getName();
		String invocationIdentifier = invocationName.getIdentifier();
		if (!invocationIdentifier.equals(methodName)) {
			return true;
		}
		IMethodBinding invocationBinding = methodInvocation.resolveMethodBinding();
		if (invocationBinding == null) {
			unresolvedBindingsFlag = true;
			return false;
		}

		IMethodBinding invocationDeclaration = invocationBinding.getMethodDeclaration();
		if (declaration.isEqualTo(invocationDeclaration)) {
			invocations.add(methodInvocation);
		}

		return true;
	}

	/**
	 * 
	 * @return the list of the {@link MethodInvocation}s found in the visited
	 *         node.
	 */
	public List<MethodInvocation> getMethodInvocations() {
		return this.invocations;
	}

	/**
	 * 
	 * @return if the method binding of any of the method invocations could not
	 *         be resolved.
	 */
	public boolean hasUnresolvedBindings() {
		return unresolvedBindingsFlag;
	}
}

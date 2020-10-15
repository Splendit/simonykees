package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
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
	private List<ExpressionMethodReference> methodReferences = new ArrayList<>();
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
	public boolean visit(ExpressionMethodReference methodReference) {
		SimpleName invocationName = methodReference.getName();
		IMethodBinding binding = methodReference.resolveMethodBinding();
		analyzeInvocation(invocationName, binding, methodReference, methodReferences::add);

		return true;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName invocationName = methodInvocation.getName();
		IMethodBinding invocationBinding = methodInvocation.resolveMethodBinding();
		analyzeInvocation(invocationName, invocationBinding, methodInvocation, invocations::add);
		return true;
	}

	private <T> void analyzeInvocation(SimpleName invocationName, IMethodBinding binding, T reference,
			Consumer<T> stateUpdater) {
		String invocationIdentifier = invocationName.getIdentifier();
		if (!invocationIdentifier.equals(methodName)) {
			return;
		}

		if (binding == null) {
			unresolvedBindingsFlag = true;
			return;
		}

		IMethodBinding invocationDeclaration = binding.getMethodDeclaration();
		if (declaration.isEqualTo(invocationDeclaration)) {
			stateUpdater.accept(reference);
		}
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

	/**
	 * 
	 * @return the list of method references ({@link ExpressionMethodReference})
	 *         found in the visited node.
	 */
	public List<ExpressionMethodReference> getExpressionMethodReferences() {
		return this.methodReferences;
	}
}

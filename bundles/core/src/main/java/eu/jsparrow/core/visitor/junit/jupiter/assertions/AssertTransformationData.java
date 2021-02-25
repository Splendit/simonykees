package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * 
 * @since 3.28.0
 *
 */
public class AssertTransformationData {
	private final MethodInvocation methodInvocationToReplace;
	private final Supplier<MethodInvocation> newMethodInvocationSupplier;
	private final Supplier<List<Expression>> newArgumentsSupplier;

	public AssertTransformationData(MethodInvocation methodInvocationToReplace,
			Supplier<MethodInvocation> newMethodInvocationSupplier, Supplier<List<Expression>> newArgumentsSupplier) {
		this.newArgumentsSupplier = newArgumentsSupplier;
		this.methodInvocationToReplace = methodInvocationToReplace;
		this.newMethodInvocationSupplier = newMethodInvocationSupplier;
	}

	public MethodInvocation getMethodInvocationToReplace() {
		return methodInvocationToReplace;
	}

	public MethodInvocation createAssertionMethodInvocation() {
		return newMethodInvocationSupplier.get();
	}

	public List<Expression> createNewArgumentList() {
		return newArgumentsSupplier.get();
	}

}
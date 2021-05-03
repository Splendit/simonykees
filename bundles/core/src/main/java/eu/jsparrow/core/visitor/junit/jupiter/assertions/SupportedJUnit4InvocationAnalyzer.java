package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Helper class analyzing a {@link MethodInvocation}-node. If the
 * {@link MethodInvocation} represents the invocation of one of the supported
 * methods of the class {@code org.junit.Assert} or {@code org.junit.Assume},
 * then all necessary informations for a possible transformation are collected
 * in an instance of {@link JUnit4MethodInvocationAnalysisResult}.
 * 
 * @since 3.28.0
 *
 */
class SupportedJUnit4InvocationAnalyzer {
	private final JUnitJupiterTestMethodsStore jUnitJupiterTestMethodsStore;
	private final Predicate<IMethodBinding> supportedJUnit4MethodPredicate;

	SupportedJUnit4InvocationAnalyzer(CompilationUnit compilationUnit,
			Predicate<IMethodBinding> supportedJUnit4MethodPredicate) {
		jUnitJupiterTestMethodsStore = new JUnitJupiterTestMethodsStore(compilationUnit);
		this.supportedJUnit4MethodPredicate = supportedJUnit4MethodPredicate;
	}

	Optional<JUnit4AssertThrowsInvocationData> findAssertThrowsAnalysisResult(
			MethodInvocation methodInvocation) {

		if (!methodInvocation.getName()
			.getIdentifier()
			.equals("assertThrows")) { //$NON-NLS-1$
			return Optional.empty();
		}

		SupportedJUnit4InvocationData jUnit4InvocationData = findAnalysisResult(methodInvocation)
			.orElse(null);
		if (jUnit4InvocationData == null) {
			return Optional.empty();
		}
		return Optional.of(createAssertThrowsInvocationData(jUnit4InvocationData));
	}

	private JUnit4AssertThrowsInvocationData createAssertThrowsInvocationData(
			SupportedJUnit4InvocationData jUnit4InvocationData) {

		if (!jUnit4InvocationData.isTransformable()) {
			return new JUnit4AssertThrowsInvocationData(jUnit4InvocationData);
		}

		ThrowingRunnableArgumentAnalyzer throwingRunnableArgumentAnalyser = new ThrowingRunnableArgumentAnalyzer();
		List<Expression> arguments = jUnit4InvocationData.getArguments();
		if (throwingRunnableArgumentAnalyser.analyze(arguments)) {
			Type throwingRunnableTypeToReplace = throwingRunnableArgumentAnalyser.getLocalVariableTypeToReplace()
				.orElse(null);
			if (throwingRunnableTypeToReplace != null) {
				return new JUnit4AssertThrowsInvocationData(jUnit4InvocationData, throwingRunnableTypeToReplace);
			}
			return new JUnit4AssertThrowsInvocationData(jUnit4InvocationData);
		}

		MethodInvocation methodInvocation = jUnit4InvocationData.getMethodInvocation();
		IMethodBinding methodBinding = jUnit4InvocationData.getMethodBinding();
		return new JUnit4AssertThrowsInvocationData(
				new SupportedJUnit4InvocationData(methodInvocation, methodBinding, arguments, false));
	}

	Optional<SupportedJUnit4InvocationData> findAnalysisResult(
			MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding != null && supportedJUnit4MethodPredicate.test(methodBinding)) {
			List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
			boolean isTransformable = jUnitJupiterTestMethodsStore.isSurroundedWithJUnitJupiterTest(methodInvocation)
					&& arguments
						.stream()
						.allMatch(this::isArgumentWithUnambiguousType);
			return Optional
				.of(new SupportedJUnit4InvocationData(methodInvocation, methodBinding, arguments, isTransformable));
		}
		return Optional.empty();
	}

	private boolean isArgumentWithUnambiguousType(Expression expression) {
		if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			return methodBinding != null && !(methodBinding.isParameterizedMethod() && methodInvocation.typeArguments()
				.isEmpty());
		}
		if (expression.getNodeType() == ASTNode.SUPER_METHOD_INVOCATION) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) expression;
			IMethodBinding superMethodBinding = superMethodInvocation.resolveMethodBinding();
			return superMethodBinding != null
					&& !(superMethodBinding.isParameterizedMethod() && superMethodInvocation.typeArguments()
						.isEmpty());
		}
		return true;
	}

}

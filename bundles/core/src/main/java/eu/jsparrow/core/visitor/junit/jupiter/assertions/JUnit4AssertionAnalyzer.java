package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;

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
class JUnit4AssertionAnalyzer extends AbstractJUnit4MethodInvocationAnalyzer<JUnit4AssertionAnalysisResult> {
	static final String ASSERT_THROWS = "assertThrows"; //$NON-NLS-1$

	JUnit4AssertionAnalyzer(CompilationUnit compilationUnit,
			Predicate<IMethodBinding> supportedJUnit4MethodPredicate) {
		super(compilationUnit, supportedJUnit4MethodPredicate);
	}

	protected JUnit4AssertionAnalysisResult createAnalysisResult(
			MethodInvocation methodInvocation, List<Expression> arguments, IMethodBinding methodBinding,
			boolean isTransformable) {

		String methodIdentifier = methodInvocation.getName()
			.getIdentifier();

		if (isTransformable && methodIdentifier.equals(ASSERT_THROWS)) {

			ThrowingRunnableArgumentAnalyzer throwingRunnableArgumentAnalyser = new ThrowingRunnableArgumentAnalyzer();
			if (!throwingRunnableArgumentAnalyser.analyze(arguments)) {
				return new JUnit4AssertionAnalysisResult(methodInvocation, methodBinding, false);
			}
			Type throwingRunnableTypeToReplace = throwingRunnableArgumentAnalyser.getLocalVariableTypeToReplace()
				.orElse(null);
			if (throwingRunnableTypeToReplace != null) {
				return new JUnit4AssertionAnalysisResult(methodInvocation, methodBinding,
						throwingRunnableTypeToReplace, true);
			}
		}
		return new JUnit4AssertionAnalysisResult(methodInvocation, methodBinding, isTransformable);
	}
}

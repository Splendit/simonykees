package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

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
class JUnit4MethodInvocationAnalyzer
		extends AbstractJUnit4MethodInvocationAnalyzer<JUnit4MethodInvocationAnalysisResult> {

	JUnit4MethodInvocationAnalyzer(CompilationUnit compilationUnit,
			Predicate<IMethodBinding> supportedJUnit4MethodPredicate) {
		super(compilationUnit, supportedJUnit4MethodPredicate);
	}

	protected JUnit4MethodInvocationAnalysisResult createAnalysisResult(
			MethodInvocation methodInvocation, List<Expression> arguments, IMethodBinding methodBinding,
			boolean isTransformable) {
		return new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, isTransformable);
	}
}

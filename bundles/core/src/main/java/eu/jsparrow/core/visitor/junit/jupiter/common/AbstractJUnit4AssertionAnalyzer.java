package eu.jsparrow.core.visitor.junit.jupiter.common;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

public abstract class AbstractJUnit4AssertionAnalyzer<T> {

	public List<T> collectJUnit4AssertionAnalysisResults(
			CompilationUnit compilationUnit) {

		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);
		List<MethodInvocation> allMethodInvocations = invocationCollectorVisitor.getMethodInvocations();

		return allMethodInvocations
			.stream()
			.map(this::findAnalysisResult)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}
	
	/**
	 * @return true if the type of the given {@link Expression} can be related
	 *         to a type declaration unequivocally.
	 */
	public boolean isArgumentWithUnambiguousType(Expression expression) {
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

	protected abstract Optional<T> findAnalysisResult(
			MethodInvocation methodInvocation);

}
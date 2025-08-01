package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Analyzes an invocation of {@code org.junit.Assume.assumeNotNull(Object...)}
 * and stores all informations needed for the replacement by corresponding
 * invocations of
 * {@code org.hamcrest.junit.MatcherAssume.assertThat(T, Matcher<T>}.
 * 
 * @since 4.0.0
 */
class AssumeNotNullArgumentsAnalysis {
	private boolean multipleVarargs;
	private boolean singleVarargArrayCreation;
	private AssumeNotNullWithNullableArray assumptionWithNullableArray;

	boolean analyzeNotNullAssumptionVarargs(MethodInvocation methodInvocation, List<Expression> arguments) {

		if (arguments.size() != 1) {
			multipleVarargs = true;
			return true;
		}

		Expression singleVararg = arguments.get(0);

		if (singleVararg.getNodeType() == ASTNode.NULL_LITERAL) {
			return true;
		}

		if (!isResolvedAsObjectArray(singleVararg)) {
			return true;
		}

		if (singleVararg.getNodeType() == ASTNode.ARRAY_CREATION) {
			singleVarargArrayCreation = true;
			return true;
		}

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return false;
		}

		ExpressionStatement methodInvocationStatement = (ExpressionStatement) methodInvocation.getParent();
		Block block = ASTNodeUtil.findParentBlock(methodInvocationStatement)
			.orElse(null);
		if (block == null) {
			return false;
		}
		assumptionWithNullableArray = new AssumeNotNullWithNullableArray(singleVararg, methodInvocationStatement,
				block);

		return true;
	}

	private static boolean isResolvedAsObjectArray(Expression singleVararg) {
		ITypeBinding typeBinding = singleVararg.resolveTypeBinding();
		if (!typeBinding.isArray()) {
			return false;
		}
		return !typeBinding.getComponentType()
			.isPrimitive();
	}

	boolean isMultipleVarargs() {
		return multipleVarargs;
	}

	boolean isSingleVarargArrayCreation() {
		return singleVarargArrayCreation;
	}

	Optional<AssumeNotNullWithNullableArray> getAssumptionWithNullableArray() {
		return Optional.ofNullable(assumptionWithNullableArray);
	}

}

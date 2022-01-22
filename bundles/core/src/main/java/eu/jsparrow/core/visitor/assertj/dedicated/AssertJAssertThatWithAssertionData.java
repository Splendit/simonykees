package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.assertj.SupportedAssertJAssertions;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class AssertJAssertThatWithAssertionData {
	private final MethodInvocation assertThatInvocation;
	private final Expression assertThatArgument;
	private final String assertionName;
	private Expression assertionArgument;

	static Optional<AssertJAssertThatWithAssertionData> findDataForAssumedAssertion(MethodInvocation assumedAssertion) {

		if (assumedAssertion.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}

		Expression assertionInvocationExpression = assumedAssertion.getExpression();
		if (assertionInvocationExpression == null
				|| assertionInvocationExpression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}

		MethodInvocation assumedAssertThatInvocation = (MethodInvocation) assertionInvocationExpression;
		List<Expression> assumedAssertThatArguments = ASTNodeUtil
			.convertToTypedList(assumedAssertThatInvocation.arguments(), Expression.class);
		if (assumedAssertThatArguments.size() != 1) {
			return Optional.empty();
		}
		Expression assumedAssertThatArgument = assumedAssertThatArguments.get(0);

		if (assumedAssertThatInvocation.getLocationInParent() != MethodInvocation.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}

		List<Expression> assumedAssertionArguments = ASTNodeUtil.convertToTypedList(assumedAssertion.arguments(),
				Expression.class);

		if (assumedAssertionArguments.size() > 1) {
			return Optional.empty();
		}

		String assumedAssertThatMethodName = assumedAssertThatInvocation.getName()
			.getIdentifier();

		if (!SupportedAssertJAssertions.isSupportedAssertJAsserThatMethodName(assumedAssertThatMethodName)) {
			return Optional.empty();
		}
		IMethodBinding assertThatMethodBinding = assumedAssertThatInvocation.resolveMethodBinding();

		if (assertThatMethodBinding == null ||
				!SupportedAssertJAssertions.isSupportedAssertionsType(assertThatMethodBinding.getDeclaringClass())) {
			return Optional.empty();
		}

		String assumedAssertionName = assumedAssertion.getName()
			.getIdentifier();
		if (assumedAssertionArguments.size() == 1) {
			return Optional
				.of(new AssertJAssertThatWithAssertionData(assumedAssertThatInvocation, assumedAssertThatArgument,
						assumedAssertionName, assumedAssertionArguments.get(0)));
		}
		return Optional
			.of(new AssertJAssertThatWithAssertionData(assumedAssertThatInvocation, assumedAssertThatArgument,
					assumedAssertionName));

	}

	static AssertJAssertThatWithAssertionData createNewDataWithoutAssertionArgument(
			AssertJAssertThatWithAssertionData data, String newAssertionName) {
		Expression sameAssertThatArgument = data.getAssertThatArgument();
		return createNewDataWithoutAssertionArgument(data, sameAssertThatArgument, newAssertionName);
	}

	static AssertJAssertThatWithAssertionData createNewDataWithoutAssertionArgument(
			AssertJAssertThatWithAssertionData data, Expression newAssertThatArgument, String newAssertionName) {
		return new AssertJAssertThatWithAssertionData(data.getAssertThatInvocation(), newAssertThatArgument,
				newAssertionName);
	}

	static AssertJAssertThatWithAssertionData createNewDataWithAssertionArgument(
			AssertJAssertThatWithAssertionData data, Expression newAssertThatArgument, String newAssertionName,
			Expression assertionArgument) {
		return new AssertJAssertThatWithAssertionData(data.getAssertThatInvocation(), newAssertThatArgument,
				newAssertionName, assertionArgument);
	}

	private AssertJAssertThatWithAssertionData(MethodInvocation assertThatInvocation, Expression newAssertThatArgument,
			String assertionName) {
		this.assertThatInvocation = assertThatInvocation;
		this.assertThatArgument = newAssertThatArgument;
		this.assertionName = assertionName;
	}

	private AssertJAssertThatWithAssertionData(MethodInvocation assertThatInvocation, Expression newAssertThatArgument,
			String assertionName,
			Expression assertionArgument) {
		this(assertThatInvocation, newAssertThatArgument, assertionName);
		this.assertionArgument = assertionArgument;
	}

	MethodInvocation getAssertThatInvocation() {
		return assertThatInvocation;
	}

	Expression getAssertThatArgument() {
		return assertThatArgument;
	}

	Optional<Expression> getAssertThatExpression() {
		return Optional.ofNullable(assertThatInvocation.getExpression());
	}

	String getAssertionName() {
		return assertionName;
	}

	Optional<Expression> getAssertionArgument() {
		return Optional.ofNullable(assertionArgument);
	}

}

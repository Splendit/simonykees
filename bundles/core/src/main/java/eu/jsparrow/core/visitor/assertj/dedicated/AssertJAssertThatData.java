package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.assertj.SupportedAssertJAssertions;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

class AssertJAssertThatData {
	private final MethodInvocation assertThatInvocation;
	private final String assertThatMethodname;
	private final Expression assertThatArgument;

	static Optional<AssertJAssertThatData> findDataForAssumedAssertThat(MethodInvocation assumedAssertThatInvocation) {

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

		List<Expression> assertThatArguments = ASTNodeUtil.convertToTypedList(assumedAssertThatInvocation.arguments(),
				Expression.class);

		if (assertThatArguments.size() != 1) {
			return Optional.empty();
		}
		Expression assertThatArgument = assertThatArguments.get(0);

		return Optional.of(new AssertJAssertThatData(assumedAssertThatInvocation, assertThatArgument));
	}

	static AssertJAssertThatData createDataReplacingArgument(AssertJAssertThatData assertThatData,
			Expression assertThatArgumentReplacement) {
		return new AssertJAssertThatData(assertThatData.assertThatInvocation, assertThatArgumentReplacement);
	}

	private AssertJAssertThatData(MethodInvocation assertThatInvocation, Expression assertThatArgument) {
		this.assertThatInvocation = assertThatInvocation;
		this.assertThatMethodname = assertThatInvocation.getName()
			.getIdentifier();
		this.assertThatArgument = assertThatArgument;
	}

	String getAssertThatMethodName() {
		return assertThatMethodname;
	}

	Expression getAssertThatArgument() {
		return assertThatArgument;
	}

	Optional<Expression> getAssertThatInvocationExpression() {
		return Optional.ofNullable(assertThatInvocation.getExpression());
	}
}

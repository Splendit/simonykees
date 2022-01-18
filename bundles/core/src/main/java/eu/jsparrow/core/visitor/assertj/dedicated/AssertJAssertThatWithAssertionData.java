package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class AssertJAssertThatWithAssertionData {
	private final AssertJAssertThatData assertThatData;
	private final String assertionName;
	private Expression assertionArgument;

	static Optional<AssertJAssertThatWithAssertionData> findDataForAssumedAssertThat(
			MethodInvocation assumedAssertThatInvocation) {

		if (assumedAssertThatInvocation.getLocationInParent() != MethodInvocation.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}

		MethodInvocation assumedAssertion = (MethodInvocation) assumedAssertThatInvocation.getParent();
		if (assumedAssertion.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}

		List<Expression> assumedAssertionArguments = ASTNodeUtil.convertToTypedList(assumedAssertion.arguments(),
				Expression.class);

		if (assumedAssertionArguments.size() > 1) {
			return Optional.empty();
		}

		AssertJAssertThatData assertThatData = AssertJAssertThatData
			.findDataForAssumedAssertThat(assumedAssertThatInvocation)
			.orElse(null);

		if (assertThatData != null) {
			String assumedAssertionName = assumedAssertThatInvocation.getName()
				.getIdentifier();
			if (assumedAssertionArguments.size() == 1) {
				return Optional.of(new AssertJAssertThatWithAssertionData(assertThatData, assumedAssertionName,
						assumedAssertionArguments.get(0)));
			}
			return Optional.of(new AssertJAssertThatWithAssertionData(assertThatData, assumedAssertionName));

		}
		return Optional.empty();
	}

	static AssertJAssertThatWithAssertionData createNewDataWithoutAssertionArgument(
			AssertJAssertThatWithAssertionData data, Expression newAssertThatArgument, String newAssertionName) {
		AssertJAssertThatData newAssertThatData = AssertJAssertThatData
			.createDataReplacingArgument(data.getAssertThatData(), newAssertThatArgument);
		return new AssertJAssertThatWithAssertionData(newAssertThatData, newAssertionName);
	}

	static AssertJAssertThatWithAssertionData createNewDataWithAssertionArgument(
			AssertJAssertThatWithAssertionData data, Expression newAssertThatArgument, String newAssertionName,
			Expression assertionArgument) {
		AssertJAssertThatData newAssertThatData = AssertJAssertThatData
			.createDataReplacingArgument(data.getAssertThatData(), newAssertThatArgument);
		return new AssertJAssertThatWithAssertionData(newAssertThatData, newAssertionName, assertionArgument);
	}

	private AssertJAssertThatWithAssertionData(AssertJAssertThatData assertThatData, String assertionName) {
		this.assertThatData = assertThatData;
		this.assertionName = assertionName;
	}

	private AssertJAssertThatWithAssertionData(AssertJAssertThatData assertThatData, String assertionName,
			Expression assertionArgument) {
		this(assertThatData, assertionName);
		this.assertionArgument = assertionArgument;
	}

	AssertJAssertThatData getAssertThatData() {
		return assertThatData;
	}

	String getAssertionName() {
		return assertionName;
	}

	Optional<Expression> getAssertionArgument() {
		return Optional.ofNullable(assertionArgument);
	}

}

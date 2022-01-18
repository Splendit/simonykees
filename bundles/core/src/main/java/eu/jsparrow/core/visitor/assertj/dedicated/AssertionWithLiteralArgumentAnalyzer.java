package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NumberLiteral;

public class AssertionWithLiteralArgumentAnalyzer {

	@SuppressWarnings("nls")
	private static final List<String> ZERO_LITERAL_TOKENS = Collections.unmodifiableList(Arrays.asList(
			"0", "0L", "0l", "0F", "0f", "0.0F", "0.0f", "0.0"));

	static Optional<String> findNameReplacementForNullLiteralArgument(String methodName, Expression argument) {
		if (argument.getNodeType() == ASTNode.NULL_LITERAL) {
			if (methodName.equals(Constants.IS_SAME_AS) || methodName.equals(Constants.IS_EQUAL_TO)) {
				return Optional.of(Constants.IS_NULL);
			}
			if (methodName.equals(Constants.IS_NOT_SAME_AS) || methodName.equals(Constants.IS_NOT_EQUAL_TO)) {
				return Optional.of(Constants.IS_NOT_NULL);
			}
		}
		return Optional.empty();
	}

	static Optional<String> findNameReplacementForZeroLiteralArgument(String methodName, Expression argument) {
		if (argument.getNodeType() == ASTNode.NUMBER_LITERAL) {
			NumberLiteral numberLiteral = (NumberLiteral) argument;
			String numericTooken = numberLiteral.getToken();
			if (ZERO_LITERAL_TOKENS.contains(numericTooken)) {
				if (methodName.equals(Constants.IS_EQUAL_TO)) {
					return Optional.of(Constants.IS_ZERO);
				}

				if (methodName.equals(Constants.IS_NOT_EQUAL_TO)) {
					return Optional.of(Constants.IS_NOT_ZERO);
				}
			}
		}
		return Optional.empty();
	}

}

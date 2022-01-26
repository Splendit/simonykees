package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Helper class to analyze assertions in connection with the size of a
 * collection or map, with the length of a string or with the length of an
 * array.
 * <p>
 * An example like {@code assertThat(list.size()).isEqualTo(expectedSize);} <br>
 * can be replaced by {@code assertThat(list).hasSize(expectedSize);}
 * 
 * @since 4.8.0
 *
 */
public class AssertionWithSizeAndLengthAnalyzer {

	private static final String LENGTH = "length"; //$NON-NLS-1$
	private static final String SIZE = "size"; //$NON-NLS-1$

	static Optional<AssertJAssertThatWithAssertionData> findResultForAssertionWithSizeOrLength(
			AssertJAssertThatWithAssertionData assertThatWithAssertion) {

		Expression assertThatArgument = assertThatWithAssertion.getAssertThatArgument();
		if (assertThatArgument.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation methodInvocationAsAssertThatArgument = (MethodInvocation) assertThatArgument;
			Expression newAssertThatArgument = methodInvocationAsAssertThatArgument.getExpression();
			if (newAssertThatArgument == null) {
				return Optional.empty();
			}
			if (!isSupportedSizeOrLengthMethodInvocation(newAssertThatArgument, methodInvocationAsAssertThatArgument)) {
				return Optional.empty();
			}
			return findReplacementData(assertThatWithAssertion, newAssertThatArgument);
		}

		Expression assumedArrayExpression = findAssumedArrayByLengthAccess(assertThatArgument).orElse(null);
		if (assumedArrayExpression == null) {
			return Optional.empty();
		}
		ITypeBinding assumedSupportedArrayType = assumedArrayExpression.resolveTypeBinding();

		if (assumedSupportedArrayType == null
				|| !SupportedAssertJAssertThatArgumentTypes.IS_SUPPORTED_ARRAY_TYPE.test(assumedSupportedArrayType)) {
			return Optional.empty();
		}
		return findReplacementData(assertThatWithAssertion, assumedArrayExpression);
	}

	private static Optional<Expression> findAssumedArrayByLengthAccess(Expression assertThatArgument) {
		Expression assumedArrayExpression = null;
		if (assertThatArgument.getNodeType() == ASTNode.FIELD_ACCESS) {
			FieldAccess fieldAccess = (FieldAccess) assertThatArgument;
			String assumedArrayLengthIdentifier = fieldAccess.getName()
				.getIdentifier();
			if (assumedArrayLengthIdentifier.equals(LENGTH)) {
				assumedArrayExpression = fieldAccess.getExpression();
			}
		} else if (assertThatArgument.getNodeType() == ASTNode.QUALIFIED_NAME) {
			QualifiedName qualifiedName = (QualifiedName) assertThatArgument;
			String assumedArrayLengthIdentifier = qualifiedName.getName()
				.getIdentifier();
			if (assumedArrayLengthIdentifier.equals(LENGTH)) {
				assumedArrayExpression = qualifiedName.getQualifier();
			}
		}
		if (assumedArrayExpression != null) {
			return Optional.of(assumedArrayExpression);
		}
		return Optional.empty();
	}

	private static Optional<AssertJAssertThatWithAssertionData> findReplacementData(
			AssertJAssertThatWithAssertionData assertThatWithAssertion,
			Expression newAssertThatArgument) {

		String oldAssertionName = assertThatWithAssertion.getAssertionName();
		Expression oldAssertionArgument = assertThatWithAssertion.getAssertionArgument()
			.orElse(null);

		if (oldAssertionArgument == null) {
			return findNewAssertionNameForAssertionWithoutArgument(oldAssertionName)
				.map(newAssertionName -> new AssertJAssertThatWithAssertionData(newAssertThatArgument,
						newAssertionName));
		}

		if (AssertionWithLiteralArgumentAnalyzer.isZeroLiteralToken(oldAssertionArgument)) {
			return findNewAssertionNameForAssertionWithZeroArgument(oldAssertionName)
				.map(newAssertionName -> new AssertJAssertThatWithAssertionData(newAssertThatArgument,
						newAssertionName));
		}

		return findNewAssertionNameForAssertionWithOtherArgument(oldAssertionName)
			.map(newAssertionName -> new AssertJAssertThatWithAssertionData(newAssertThatArgument,
					newAssertionName, oldAssertionArgument));

	}

	private static boolean isSupportedSizeOrLengthMethodInvocation(Expression newAssertThatArgument,
			MethodInvocation methodInvocationAsAssertThatArgument) {

		if (!methodInvocationAsAssertThatArgument.arguments()
			.isEmpty()) {
			return false;
		}

		ITypeBinding newAssertThatArguemntTypeBinding = newAssertThatArgument.resolveTypeBinding();
		if (newAssertThatArguemntTypeBinding == null) {
			return false;
		}

		String methodNameOfAassertThatArgument = methodInvocationAsAssertThatArgument.getName()
			.getIdentifier();

		if (methodNameOfAassertThatArgument.equals(SIZE)) {
			return SupportedAssertJAssertThatArgumentTypes.isSupportedCollectionType(newAssertThatArguemntTypeBinding)
					|| SupportedAssertJAssertThatArgumentTypes.isSupportedMapType(newAssertThatArguemntTypeBinding);
		}
		return methodNameOfAassertThatArgument.equals(LENGTH) && ClassRelationUtil
			.isContentOfType(newAssertThatArguemntTypeBinding, java.lang.String.class.getName());
	}

	private static Optional<String> findNewAssertionNameForAssertionWithoutArgument(String oldAssertionName) {
		if (oldAssertionName.equals(Constants.IS_ZERO) || oldAssertionName.equals(Constants.IS_NOT_POSITIVE)) {
			return Optional.of(Constants.IS_EMPTY);
		}
		if (oldAssertionName.equals(Constants.IS_NOT_ZERO) || oldAssertionName.equals(Constants.IS_POSITIVE)) {
			return Optional.of(Constants.IS_NOT_EMPTY);
		}
		return Optional.empty();
	}

	private static Optional<String> findNewAssertionNameForAssertionWithZeroArgument(
			String oldAssertionName) {
		if (oldAssertionName.equals(Constants.IS_EQUAL_TO)) {
			return Optional.of(Constants.IS_EMPTY);
		}
		if (oldAssertionName.equals(Constants.IS_LESS_THAN_OR_EQUAL_TO)) {
			return Optional.of(Constants.IS_EMPTY);
		}
		if (oldAssertionName.equals(Constants.IS_NOT_EQUAL_TO)) {
			return Optional.of(Constants.IS_NOT_EMPTY);
		}
		if (oldAssertionName.equals(Constants.IS_GREATER_THAN)) {
			return Optional.of(Constants.IS_NOT_EMPTY);
		}
		return Optional.empty();
	}

	private static Optional<String> findNewAssertionNameForAssertionWithOtherArgument(
			String oldAssertionName) {

		if (oldAssertionName.equals(Constants.IS_EQUAL_TO)) {
			return Optional.of(Constants.HAS_SIZE);
		}
		if (oldAssertionName.equals(Constants.IS_GREATER_THAN)) {
			return Optional.of(Constants.HAS_SIZE_GREATER_THAN);
		}
		if (oldAssertionName.equals(Constants.IS_GREATER_THAN_OR_EQUAL_TO)) {
			return Optional.of(Constants.HAS_SIZE_GREATER_THAN_OR_EQUAL_TO);
		}
		if (oldAssertionName.equals(Constants.IS_LESS_THAN)) {
			return Optional.of(Constants.HAS_SIZE_LESS_THAN);
		}
		if (oldAssertionName.equals(Constants.IS_LESS_THAN_OR_EQUAL_TO)) {
			return Optional.of(Constants.HAS_SIZE_LESS_THAN_OR_EQUAL_TO);
		}
		return Optional.empty();
	}

	static Optional<AssertJAssertThatWithAssertionData> findHasSameSizeAssertionData(
			AssertJAssertThatWithAssertionData assertThatWithAssertion) {
		String oldAssertionName = assertThatWithAssertion.getAssertionName();
		if (!oldAssertionName.equals(Constants.HAS_SIZE)) {
			return Optional.empty();
		}

		Expression oldAssertionArgument = assertThatWithAssertion.getAssertionArgument()
			.orElse(null);
		if (oldAssertionArgument == null) {
			return Optional.empty();
		}

		Expression sameAssertThatArgument = assertThatWithAssertion.getAssertThatArgument();

		if (oldAssertionArgument.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation methodInvocation = (MethodInvocation) oldAssertionArgument;
			Expression assumedNewAssertionArgument = null;
			assumedNewAssertionArgument = methodInvocation.getExpression();
			if (assumedNewAssertionArgument == null) {
				return Optional.empty();
			}
			if (!isSupportedSizeOrLengthMethodInvocation(sameAssertThatArgument, methodInvocation)) {
				return Optional.empty();
			}

			if (!ClassRelationUtil.compareITypeBinding(sameAssertThatArgument.resolveTypeBinding(),
					assumedNewAssertionArgument.resolveTypeBinding())) {
				return Optional.empty();
			}

			return Optional.of(new AssertJAssertThatWithAssertionData(sameAssertThatArgument, "hasSameSizeAs", //$NON-NLS-1$
					assumedNewAssertionArgument));
		}

		Expression assumedArrayAsNewAssertionArgument = null;
		assumedArrayAsNewAssertionArgument = findAssumedArrayByLengthAccess(oldAssertionArgument).orElse(null);
		if (assumedArrayAsNewAssertionArgument == null) {
			return Optional.empty();
		}
		ITypeBinding assumedSupportedArrayType = assumedArrayAsNewAssertionArgument.resolveTypeBinding();

		if (assumedSupportedArrayType == null
				|| !SupportedAssertJAssertThatArgumentTypes.IS_SUPPORTED_ARRAY_TYPE.test(assumedSupportedArrayType)) {
			return Optional.empty();
		}

		if (!ClassRelationUtil.compareITypeBinding(sameAssertThatArgument.resolveTypeBinding(),
				assumedArrayAsNewAssertionArgument.resolveTypeBinding())) {
			return Optional.empty();
		}

		return Optional.of(new AssertJAssertThatWithAssertionData(sameAssertThatArgument, "hasSameSizeAs", //$NON-NLS-1$
				assumedArrayAsNewAssertionArgument));
	}

	private AssertionWithSizeAndLengthAnalyzer() {
		/*
		 * private default constructor hiding implicit public one
		 */
	}
}

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

	private AssertionWithSizeAndLengthAnalyzer() {
		/*
		 * private default constructor hiding implicit public one
		 */
	}

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

		Expression supportedArrayExpression = findSupportedArrayByLengthAccess(assertThatArgument).orElse(null);
		if (supportedArrayExpression == null) {
			return Optional.empty();
		}

		return findReplacementData(assertThatWithAssertion, supportedArrayExpression);

	}

	private static Optional<Expression> findSupportedArrayByLengthAccess(Expression assertThatArgument) {
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

		return Optional.ofNullable(assumedArrayExpression)
			.filter(assumed -> {
				ITypeBinding typeBinding = assumed.resolveTypeBinding();
				return typeBinding != null
						&& SupportedAssertJAssertThatArgumentTypes.IS_SUPPORTED_ARRAY_TYPE.test(typeBinding);
			});

	}

	private static Optional<AssertJAssertThatWithAssertionData> findReplacementData(
			AssertJAssertThatWithAssertionData assertThatWithAssertion,
			Expression newAssertThatArgument) {

		String oldAssertionName = assertThatWithAssertion.getAssertionName();
		Expression oldAssertionArgument = assertThatWithAssertion.getAssertionArgument()
			.orElse(null);

		if (oldAssertionArgument == null) {
			return findNewAssertionNameForAssertionWithNoArgument(oldAssertionName)
				.map(newAssertionName -> new AssertJAssertThatWithAssertionData(newAssertThatArgument,
						newAssertionName));
		}

		if (AssertionWithLiteralArgumentAnalyzer.isZeroLiteralToken(oldAssertionArgument)) {
			return findNewAssertionNameForAssertionWithZeroLiteralArgument(oldAssertionName)
				.map(newAssertionName -> new AssertJAssertThatWithAssertionData(newAssertThatArgument,
						newAssertionName));
		}

		return findHasSizeAssertionName(oldAssertionName)
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

	private static Optional<String> findNewAssertionNameForAssertionWithNoArgument(String oldAssertionName) {
		switch (oldAssertionName) {
		case Constants.IS_ZERO:
		case Constants.IS_NOT_POSITIVE:
			return Optional.of(Constants.IS_EMPTY);
		case Constants.IS_NOT_ZERO:
		case Constants.IS_POSITIVE:
			return Optional.of(Constants.IS_NOT_EMPTY);
		default:
			return Optional.empty();
		}
	}

	private static Optional<String> findNewAssertionNameForAssertionWithZeroLiteralArgument(
			String oldAssertionName) {
		switch (oldAssertionName) {
		case Constants.IS_EQUAL_TO:
		case Constants.IS_LESS_THAN_OR_EQUAL_TO:
			return Optional.of(Constants.IS_EMPTY);
		case Constants.IS_NOT_EQUAL_TO:
		case Constants.IS_GREATER_THAN:
			return Optional.of(Constants.IS_NOT_EMPTY);
		default:
			return Optional.empty();
		}
	}

	private static Optional<String> findHasSizeAssertionName(
			String oldAssertionName) {
		switch (oldAssertionName) {
		case Constants.IS_EQUAL_TO:
			return Optional.of(Constants.HAS_SIZE);
		case Constants.IS_GREATER_THAN:
			return Optional.of(Constants.HAS_SIZE_GREATER_THAN);
		case Constants.IS_GREATER_THAN_OR_EQUAL_TO:
			return Optional.of(Constants.HAS_SIZE_GREATER_THAN_OR_EQUAL_TO);
		case Constants.IS_LESS_THAN:
			return Optional.of(Constants.HAS_SIZE_LESS_THAN);
		case Constants.IS_LESS_THAN_OR_EQUAL_TO:
			return Optional.of(Constants.HAS_SIZE_LESS_THAN_OR_EQUAL_TO);
		default:
			return Optional.empty();
		}
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

		Expression arrayAsNewAssertionArgument = null;
		arrayAsNewAssertionArgument = findSupportedArrayByLengthAccess(oldAssertionArgument).orElse(null);
		if (arrayAsNewAssertionArgument == null) {
			return Optional.empty();
		}

		if (!ClassRelationUtil.compareITypeBinding(sameAssertThatArgument.resolveTypeBinding(),
				arrayAsNewAssertionArgument.resolveTypeBinding())) {
			return Optional.empty();
		}

		return Optional.of(new AssertJAssertThatWithAssertionData(sameAssertThatArgument, "hasSameSizeAs", //$NON-NLS-1$
				arrayAsNewAssertionArgument));
	}
}

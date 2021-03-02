package eu.jsparrow.core.visitor.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * A visitor for collection all {@link MethodInvocation}s on
 * {@code org.junit.rules.ExpectedException}. Supports
 * {@code expectedEception.expect()}, {@code expectedEception.expectMessage()},
 * and {@code expectedEception.expectCause()}. Any other invocation is
 * considered unsupported.
 * 
 * @since 3.24.0
 *
 */
public class ExpectedExceptionVisitor extends ASTVisitor {

	private static final String EXPECT = "expect"; //$NON-NLS-1$
	private static final String EXPECT_CAUSE = "expectCause"; //$NON-NLS-1$
	private static final String EXPECT_MESSAGE = "expectMessage"; //$NON-NLS-1$
	private static final String EXPECTED_EXCEPTION = "org.junit.rules.ExpectedException"; //$NON-NLS-1$
	private static final String TEST_RULE = "org.junit.rules.TestRule"; //$NON-NLS-1$
	private static final String ORG_HAMCREST_MATCHER = "org.hamcrest.Matcher"; //$NON-NLS-1$

	private List<MethodInvocation> expectedExceptionInvocations = new ArrayList<>();
	private List<SimpleName> expectedExceptionNames = new ArrayList<>();
	private List<MethodInvocation> unresolvedInvocations = new ArrayList<>();

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return true;
		}
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		boolean isExpectedException = ClassRelationUtil.isContentOfTypes(declaringClass,
				Arrays.asList(EXPECTED_EXCEPTION, TEST_RULE));

		if (!isExpectedException) {
			return true;
		}

		Expression expression = methodInvocation.getExpression();
		if (expression == null) {
			unresolvedInvocations.add(methodInvocation);
			return true;
		}

		if (!isFieldAccess(expression)) {
			unresolvedInvocations.add(methodInvocation);
			return true;
		}
		expectedExceptionInvocations.add(methodInvocation);
		return true;
	}

	private boolean isFieldAccess(Expression expression) {
		if (expression.getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleName name = (SimpleName) expression;
			IBinding typeBinding = name.resolveBinding();
			int kind = typeBinding.getKind();
			if (IBinding.VARIABLE == kind) {
				IVariableBinding variableBinding = (IVariableBinding) typeBinding;
				this.expectedExceptionNames.add(name);
				return variableBinding.isField();
			}
		} else if (ASTNode.FIELD_ACCESS == expression.getNodeType()) {
			FieldAccess fieldAccess = (FieldAccess) expression;
			SimpleName name = fieldAccess.getName();
			this.expectedExceptionNames.add(name);
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @return the list of {@link Expression}s found in
	 *         {@code expectedException.expect(...)}. If
	 *         {@code ExpectedException} is used properly, should return a list
	 *         of one element.
	 */
	public List<Expression> getExpectedExceptionsExpression() {
		List<MethodInvocation> expectedInvocations = getExpectExceptionInvocations();
		List<Expression> expectedExceptions = new ArrayList<>();
		for (MethodInvocation expected : expectedInvocations) {
			List<Expression> arguments = ASTNodeUtil.convertToTypedList(expected.arguments(), Expression.class);
			for (Expression argument : arguments) {
				expectedExceptions.add(argument);
			}
		}
		return expectedExceptions;
	}

	/**
	 * @return the list of all {@code expectedException.expect(...)} found in
	 *         the visited node. If {@code ExpectedException} is used properly,
	 *         should return a list of one element.
	 */
	public List<MethodInvocation> getExpectExceptionInvocations() {
		return this.expectedExceptionInvocations.stream()
			.filter(mi -> EXPECT.equals(mi.getName()
				.getIdentifier()))
			.collect(Collectors.toList());
	}

	/**
	 * 
	 * @param argTypeFilter
	 *            a predicate for filtering the type of the expression in
	 *            {@code expectedException.expectMessage(...)}, which is either
	 *            a {@link String} or a Hamcrest {@code Matcher<String>}.
	 * @return the list of all {@code expectedException.expectMessage(...)}
	 *         found in the visited node.
	 */
	public List<Expression> getExpectedMessages(Predicate<MethodInvocation> argTypeFilter) {
		return this.expectedExceptionInvocations.stream()
			.filter(mi -> EXPECT_MESSAGE.equals(mi.getName()
				.getIdentifier()))
			.filter(argTypeFilter)
			.flatMap(mi -> ASTNodeUtil.convertToTypedList(mi.arguments(), Expression.class)
				.stream())
			.collect(Collectors.toList());
	}

	public boolean hasSingleParameterOfType(MethodInvocation methodInvocation, String qualifedTypeName) {
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return false;
		}
		Expression argument = arguments.get(0);
		ITypeBinding argumentType = argument.resolveTypeBinding();
		return ClassRelationUtil.isContentOfType(argumentType, qualifedTypeName);
	}

	/**
	 * 
	 * @return the list of all {@code expectedException.expectCause(matcher)}
	 *         found in the visited node.
	 */
	public List<Expression> getExpectedCauses() {
		return this.expectedExceptionInvocations.stream()
			.filter(mi -> EXPECT_CAUSE.equals(mi.getName()
				.getIdentifier()))
			.filter(mi -> this.hasSingleParameterOfType(mi, ORG_HAMCREST_MATCHER))
			.flatMap(mi -> ASTNodeUtil.convertToTypedList(mi.arguments(), Expression.class)
				.stream())
			.collect(Collectors.toList());
	}

	/**
	 * @return the list of invocations in {@code ExpectedException} which is not
	 *         any of: {@code expect(...)}, {@code expectMessage(...)}, or
	 *         {@code expectCause(Matcher<Throwable>)}.
	 */
	public boolean hasUnsupportedMethods() {
		return this.expectedExceptionInvocations
			.stream()
			.map(MethodInvocation::getName)
			.map(SimpleName::getIdentifier)
			.anyMatch(identifier -> !EXPECT.equals(identifier)
					&& !EXPECT_CAUSE.equals(identifier)
					&& !EXPECT_MESSAGE.equals(identifier));
	}

	/**
	 * 
	 * @return if only one {@code ExpectedException} object is used in the
	 *         visited node.
	 */
	public boolean hasUniqueExpectedExceptionRule() {
		return this.expectedExceptionNames.stream()
			.map(SimpleName::getIdentifier)
			.distinct()
			.count() <= 1;

	}

	/**
	 * 
	 * @return if the expression of the {@code expectedException.expect(...)} is
	 *         missing or is not a {@link FieldAccess}.
	 */
	public boolean hasUnresolvedInvocations() {
		return !this.unresolvedInvocations.isEmpty();
	}

	/**
	 * 
	 * @return if the Hamcrest Matcher type argument used in the
	 *         {@code expectedException.expectCause(Matcher<Throwable>)} is a
	 *         {@link Throwable}.
	 */
	public boolean verifyExpectCauseMatchers() {
		List<Expression> matchers = getExpectedCauses();
		for (Expression matcher : matchers) {
			ITypeBinding type = matcher.resolveTypeBinding();
			if (type.isParameterizedType()) {
				ITypeBinding[] typeParameters = type.getTypeArguments();
				if (typeParameters.length == 1) {
					ITypeBinding matcherType = typeParameters[0];
					boolean isThrowable = ClassRelationUtil.isContentOfType(matcherType, Throwable.class.getName());
					if (matcherType.isCapture() || !isThrowable) {
						return false;
					}
				}
			}
		}
		return true;
	}

}

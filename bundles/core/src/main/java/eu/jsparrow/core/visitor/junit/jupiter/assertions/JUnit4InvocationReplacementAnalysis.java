package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;

/**
 * Analyzes an invocation of a JUnit4 method declared in one of the following
 * classes:
 * <ul>
 * <li>{@code org.junit.Assert}</li>
 * <li>{@code org.junit.Assume}.</li>
 * </ul>
 * Stores all informations which are necessary for the replacement of this
 * invocation.
 * <p>
 * 
 * @since 3.30.0
 *
 */
class JUnit4InvocationReplacementAnalysis {

	private final MethodInvocation methodInvocation;
	private final IMethodBinding methodBinding;
	private final List<Expression> arguments;
	private final String originalMethodName;

	private String methodNameReplacement;
	private Expression messageMovedToLastPosition;
	private Type typeOfThrowingRunnableToReplace;
	private AssumeNotNullArgumentsAnalysis assumeNotNullArgumentsAnalysis;

	public JUnit4InvocationReplacementAnalysis(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			List<Expression> arguments) {
		this.methodInvocation = methodInvocation;
		this.methodBinding = methodBinding;
		this.arguments = arguments;
		this.originalMethodName = methodBinding.getName();
	}

	boolean analyzeAssertion() {
		if (originalMethodName.equals("assertThrows")) { //$NON-NLS-1$
			ThrowingRunnableArgumentAnalyzer throwingRunnableArgumentAnalyser = new ThrowingRunnableArgumentAnalyzer();
			if (!throwingRunnableArgumentAnalyser.analyze(arguments)) {
				return false;
			}
			typeOfThrowingRunnableToReplace = throwingRunnableArgumentAnalyser.getLocalVariableTypeToReplace()
				.orElse(null);
		}

		ITypeBinding[] declaredParameterTypes = methodBinding
			.getMethodDeclaration()
			.getParameterTypes();
		if (isDeprecatedAssertEqualsComparingObjectArrays(originalMethodName, declaredParameterTypes)) {
			methodNameReplacement = "assertArrayEquals"; //$NON-NLS-1$
		}

		messageMovedToLastPosition = findMessageMovedDoLastPosition(arguments, declaredParameterTypes).orElse(null);

		return true;
	}

	void analyzeAssumptionToJupiter() {

		ITypeBinding[] declaredParameterTypes = methodBinding
			.getMethodDeclaration()
			.getParameterTypes();

		messageMovedToLastPosition = findMessageMovedDoLastPosition(arguments, declaredParameterTypes).orElse(null);
	}

	boolean analyzeAssumptionToHamcrest() {

		methodNameReplacement = "assumeThat"; //$NON-NLS-1$

		if (!originalMethodName.equals("assumeNotNull")) {//$NON-NLS-1$
			return true;
		}

		assumeNotNullArgumentsAnalysis = new AssumeNotNullArgumentsAnalysis();
		return assumeNotNullArgumentsAnalysis.analyze(methodInvocation, arguments);
	}

	private static Optional<Expression> findMessageMovedDoLastPosition(List<Expression> arguments,
			ITypeBinding[] declaredParameterTypes) {
		if (declaredParameterTypes.length == 0) {
			return Optional.empty();
		}
		if (arguments.size() < 2) {
			return Optional.empty();
		}
		if (isParameterTypeString(declaredParameterTypes[0])) {
			return Optional.of(arguments.get(0));
		}
		return Optional.empty();
	}

	static boolean isDeprecatedAssertEqualsComparingObjectArrays(String methodName,
			ITypeBinding[] declaredParameterTypes) {
		if (!methodName.equals("assertEquals")) { //$NON-NLS-1$
			return false;
		}

		if (declaredParameterTypes.length == 2) {
			return isParameterTypeObjectArray(declaredParameterTypes[0])
					&& isParameterTypeObjectArray(declaredParameterTypes[1]);
		}

		if (declaredParameterTypes.length == 3) {
			return isParameterTypeString(declaredParameterTypes[0])
					&& isParameterTypeObjectArray(declaredParameterTypes[1])
					&& isParameterTypeObjectArray(declaredParameterTypes[2]);
		}
		return false;
	}

	static boolean isParameterTypeObjectArray(ITypeBinding parameterType) {
		if (parameterType.isArray() && parameterType.getDimensions() == 1) {
			return isContentOfType(parameterType.getComponentType(), "java.lang.Object"); //$NON-NLS-1$
		}
		return false;
	}

	static boolean isParameterTypeString(ITypeBinding parameterType) {
		return isContentOfType(parameterType, "java.lang.String"); //$NON-NLS-1$
	}

	MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	List<Expression> getArguments() {
		return arguments;
	}

	String getOriginalMethodName() {
		return originalMethodName;
	}

	String getNewMethodName() {
		return methodNameReplacement != null ? methodNameReplacement : originalMethodName;
	}

	Optional<Type> getTypeOfThrowingRunnableToReplace() {
		return Optional.ofNullable(typeOfThrowingRunnableToReplace);
	}

	Optional<Expression> getMessageMovedToLastPosition() {
		return Optional.ofNullable(messageMovedToLastPosition);
	}

	Optional<AssumeNotNullArgumentsAnalysis> getAssumeNotNullArgumentsAnalysis() {
		return Optional.ofNullable(assumeNotNullArgumentsAnalysis);
	}

	Optional<AssumeNotNullWithNullableArray> getAssumeNotNullWithNullableArray() {
		if (assumeNotNullArgumentsAnalysis != null) {
			return assumeNotNullArgumentsAnalysis.getAssumptionWithNullableArray();
		}
		return Optional.empty();
	}
}
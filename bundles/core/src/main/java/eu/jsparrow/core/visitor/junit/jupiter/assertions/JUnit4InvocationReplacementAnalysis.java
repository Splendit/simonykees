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

	private static final String ASSUME_THAT = "assumeThat"; //$NON-NLS-1$
	private static final String JAVA_LANG_STRING = java.lang.String.class.getName();
	private final MethodInvocation methodInvocation;
	private final IMethodBinding methodBinding;
	private final List<Expression> arguments;
	private final String originalMethodName;

	private String methodNameReplacement;
	private Expression messageMovedToLastPosition;
	private Type typeOfThrowingRunnableToReplace;
	private AssumeNotNullArgumentsAnalysis assumeNotNullArgumentsAnalysis;
	private boolean changingArguments;

	JUnit4InvocationReplacementAnalysis(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			List<Expression> arguments) {
		this.methodInvocation = methodInvocation;
		this.methodBinding = methodBinding;
		this.arguments = arguments;
		this.originalMethodName = methodBinding.getName();
	}

	boolean analyzeAssertionToJupiter() {
		if ("assertThrows".equals(originalMethodName)) { //$NON-NLS-1$
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

		messageMovedToLastPosition = findMessageMovedToLastPosition(arguments, declaredParameterTypes).orElse(null);
		changingArguments = messageMovedToLastPosition != null;

		return true;
	}

	boolean analyzeAssumptionToJupiter() {

		ITypeBinding[] declaredParameterTypes = methodBinding
			.getMethodDeclaration()
			.getParameterTypes();

		messageMovedToLastPosition = findMessageMovedToLastPosition(arguments, declaredParameterTypes).orElse(null);
		changingArguments = messageMovedToLastPosition != null;
		return true;
	}

	boolean analyzeAssumptionToHamcrest() {

		methodNameReplacement = ASSUME_THAT;
		changingArguments = !ASSUME_THAT.equals(originalMethodName);

		if (!"assumeNotNull".equals(originalMethodName)) { //$NON-NLS-1$
			return true;
		}

		assumeNotNullArgumentsAnalysis = new AssumeNotNullArgumentsAnalysis();
		return assumeNotNullArgumentsAnalysis.analyzeNotNullAssumptionVarargs(methodInvocation, arguments);
	}

	private static Optional<Expression> findMessageMovedToLastPosition(List<Expression> arguments,
			ITypeBinding[] declaredParameterTypes) {
		if (declaredParameterTypes.length == 0) {
			return Optional.empty();
		}
		if (arguments.size() < 2) {
			return Optional.empty();
		}
		if (isContentOfType(declaredParameterTypes[0], JAVA_LANG_STRING)) {
			return Optional.of(arguments.get(0));
		}
		return Optional.empty();
	}

	private static boolean isDeprecatedAssertEqualsComparingObjectArrays(String methodName,
			ITypeBinding[] declaredParameterTypes) {
		if (!methodName.equals("assertEquals")) { //$NON-NLS-1$
			return false;
		}

		if (declaredParameterTypes.length == 2) {
			return isObjectArray(declaredParameterTypes[0])
					&& isObjectArray(declaredParameterTypes[1]);
		}

		if (declaredParameterTypes.length == 3) {
			return isContentOfType(declaredParameterTypes[0], JAVA_LANG_STRING)
					&& isObjectArray(declaredParameterTypes[1])
					&& isObjectArray(declaredParameterTypes[2]);
		}
		return false;
	}

	private static boolean isObjectArray(ITypeBinding parameterType) {
		if (parameterType.isArray() && parameterType.getDimensions() == 1) {
			return isContentOfType(parameterType.getComponentType(), "java.lang.Object"); //$NON-NLS-1$
		}
		return false;
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
		return Optional.ofNullable(assumeNotNullArgumentsAnalysis)
			.flatMap(AssumeNotNullArgumentsAnalysis::getAssumptionWithNullableArray);
	}

	public boolean isChangingArguments() {
		return changingArguments;
	}
}
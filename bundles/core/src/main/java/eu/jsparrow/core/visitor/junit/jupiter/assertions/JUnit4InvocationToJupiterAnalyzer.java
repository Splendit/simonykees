package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.core.visitor.junit.jupiter.assertions.JUnit4MethodInvocationAnalyzer.isDeprecatedAssertEqualsComparingObjectArrays;
import static eu.jsparrow.core.visitor.junit.jupiter.assertions.JUnit4MethodInvocationAnalyzer.isParameterTypeString;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;

class JUnit4InvocationToJupiterAnalyzer {
	private Type typeOfThrowingRunnableToReplace;
	private String methodNameReplacement;
	private Expression messsageMovedToLastPosition;

	boolean analyzeAssertion(IMethodBinding methodBinding, List<Expression> arguments) {
		String methodIdentifier = methodBinding.getName();
		if (methodIdentifier.equals("assertThrows")) { //$NON-NLS-1$
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
		if (isDeprecatedAssertEqualsComparingObjectArrays(methodIdentifier, declaredParameterTypes)) {
			methodNameReplacement = "assertArrayEquals"; //$NON-NLS-1$
		}

		messsageMovedToLastPosition = findMessageMovedDoLastPosition(arguments, declaredParameterTypes).orElse(null);

		return true;
	}

	void analyzeAssumption(IMethodBinding methodBinding, List<Expression> arguments) {

		ITypeBinding[] declaredParameterTypes = methodBinding
			.getMethodDeclaration()
			.getParameterTypes();

		messsageMovedToLastPosition = findMessageMovedDoLastPosition(arguments, declaredParameterTypes).orElse(null);
	}

	private static Optional<Expression> findMessageMovedDoLastPosition(List<Expression> arguments,
			ITypeBinding[] declaredParameterTypes) {
		if (declaredParameterTypes.length == 0) {
			return Optional.empty();
		}
		if (isParameterTypeString(declaredParameterTypes[0]) && arguments.size() > 1) {
			return Optional.of(arguments.get(0));
		}
		return Optional.empty();
	}

	Optional<Type> getTypeOfThrowingRunnableToReplace() {
		return Optional.ofNullable(typeOfThrowingRunnableToReplace);
	}

	Optional<String> getMethodNameReplacement() {
		return Optional.ofNullable(methodNameReplacement);
	}

	Optional<Expression> getMessageMovedToLastPosition() {
		return Optional.ofNullable(messsageMovedToLastPosition);
	}
}
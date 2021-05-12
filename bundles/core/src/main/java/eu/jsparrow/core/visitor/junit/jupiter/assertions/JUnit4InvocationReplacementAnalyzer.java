package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.core.visitor.junit.jupiter.assertions.JUnit4MethodInvocationAnalyzer.isParameterTypeString;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;

class JUnit4InvocationReplacementAnalyzer {

	private String originalMethodName;
	private String newMethodName;
	private Expression messsageMovedToLastPosition;
	private Type typeOfThrowingRunnableToReplace;
	private AssumeNotNullWithSingleVararg assumeNotNullWithSingleVararg;

	boolean analyzeAssertion(IMethodBinding methodBinding, List<Expression> arguments) {
		originalMethodName = methodBinding.getName();
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
			newMethodName = "assertArrayEquals"; //$NON-NLS-1$
		} else {
			newMethodName = originalMethodName;
		}

		messsageMovedToLastPosition = findMessageMovedDoLastPosition(arguments, declaredParameterTypes).orElse(null);

		return true;
	}

	void analyzeAssumption(IMethodBinding methodBinding, List<Expression> arguments) {
		originalMethodName = methodBinding.getName();
		newMethodName = originalMethodName;

		ITypeBinding[] declaredParameterTypes = methodBinding
			.getMethodDeclaration()
			.getParameterTypes();

		messsageMovedToLastPosition = findMessageMovedDoLastPosition(arguments, declaredParameterTypes).orElse(null);
	}

	boolean analyzeAssumptionToHamcrest(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			List<Expression> arguments) {
		originalMethodName = methodBinding.getName();
		newMethodName = "assumeThat"; //$NON-NLS-1$

		if (originalMethodName.equals("assumeNotNull")) {//$NON-NLS-1$
			return true;
		}

		if (arguments.size() != 1) {
			return true;
		}

		Expression singleVararrg = arguments.get(0);
		if (!isResolvedAsObjectArray(singleVararrg)) {
			assumeNotNullWithSingleVararg = new AssumeNotNullWithSingleVararg(singleVararrg);
			return true;
		}
		if (singleVararrg.getNodeType() == ASTNode.ARRAY_CREATION) {
			assumeNotNullWithSingleVararg = new AssumeNotNullWithSingleVararg((ArrayCreation) singleVararrg);
			return true;
		}

		AssumeNotNullWithNullableArray assumeNotNullWithNullableArray = findAssumeNotNullWithNullableArray(
				methodInvocation, singleVararrg).orElse(null);
		if (assumeNotNullWithNullableArray == null) {
			return false;
		}

		assumeNotNullWithSingleVararg = new AssumeNotNullWithSingleVararg(assumeNotNullWithNullableArray);

		return true;
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

	private static boolean isResolvedAsObjectArray(Expression singleVararg) {
		ITypeBinding typeBinding = singleVararg.resolveTypeBinding();
		if (!typeBinding.isArray()) {
			return false;
		}
		return !typeBinding.getComponentType()
			.isPrimitive();
	}

	private static Optional<AssumeNotNullWithNullableArray> findAssumeNotNullWithNullableArray(
			MethodInvocation methodInvocation, Expression arrayArgument) {
		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}

		ExpressionStatement methodInvocationStatement = (ExpressionStatement) methodInvocation.getParent();
		if (methodInvocationStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return Optional.empty();
		}

		Block block = (Block) methodInvocationStatement.getParent();
		return Optional
			.of(new AssumeNotNullWithNullableArray(arrayArgument, methodInvocationStatement, block));

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

	public String getOriginalMethodName() {
		return originalMethodName;
	}

	public String getNewMethodName() {
		return newMethodName;
	}

	Optional<Type> getTypeOfThrowingRunnableToReplace() {
		return Optional.ofNullable(typeOfThrowingRunnableToReplace);
	}

	Optional<Expression> getMessageMovedToLastPosition() {
		return Optional.ofNullable(messsageMovedToLastPosition);
	}

	Optional<AssumeNotNullWithSingleVararg> getAssumeNotNullWithSingleVararg() {
		return Optional.ofNullable(assumeNotNullWithSingleVararg);
	}

	Optional<AssumeNotNullWithNullableArray> getAssumeNotNullWithNullableArray() {
		if (assumeNotNullWithSingleVararg != null) {
			return assumeNotNullWithSingleVararg.getAssumeNotNullWithNullableArray();
		}
		return Optional.empty();
	}
}
package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class JUnit3AssertionAnalyzer {
	private static final String JAVA_LANG_STRING = java.lang.String.class.getName();
	private final JUnit3TestMethodsStore testMethodStore;
	private final String classDeclaringMethodReplacement;

	JUnit3AssertionAnalyzer(JUnit3TestMethodsStore testMethodStore, String classDeclaringMethodReplacement) {
		this.testMethodStore = testMethodStore;
		this.classDeclaringMethodReplacement = classDeclaringMethodReplacement;

	}

	Optional<JUnit3AssertionAnalysisResult> findAssertionAnalysisResult(MethodInvocation methodInvocation,
			IMethodBinding methodBinding) {

		if (!isSupportedTestCaseMethod(methodBinding)) {
			return Optional.empty();
		}

		if (!testMethodStore.isSurroundedWithJUnit3Test(methodInvocation)) {
			return Optional.empty();
		}

		List<Expression> assertionArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		if (!assertionArguments.stream()
			.allMatch(this::isArgumentWithExplicitType)) {
			return Optional.empty();
		}

		if (isAssertEqualsWithObjectArrayArguments(methodBinding.getName(), assertionArguments)) {
			return Optional.empty();
		}

		Expression messageMovedToLastPosition = findMessageMovedToLastPosition(methodBinding, assertionArguments)
			.orElse(null);
		if (messageMovedToLastPosition != null) {
			return Optional
				.of(new JUnit3AssertionAnalysisResult(methodInvocation, messageMovedToLastPosition,
						classDeclaringMethodReplacement));
		} else {
			return Optional.of(new JUnit3AssertionAnalysisResult(methodInvocation, classDeclaringMethodReplacement));
		}
	}

	private boolean isSupportedTestCaseMethod(IMethodBinding methodBinding) {
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();

		if (isContentOfType(declaringClass, "junit.framework.TestCase")) {//$NON-NLS-1$
			String simpleMethodName = methodBinding.getName();
			return simpleMethodName.startsWith("assert") || //$NON-NLS-1$
					"fail".equals(simpleMethodName); //$NON-NLS-1$
		}
		return false;
	}

	private boolean isArgumentWithExplicitType(Expression expression) {
		if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			return methodBinding != null && !(methodBinding.isParameterizedMethod() && methodInvocation.typeArguments()
				.isEmpty());
		}
		if (expression.getNodeType() == ASTNode.SUPER_METHOD_INVOCATION) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) expression;
			IMethodBinding superMethodBinding = superMethodInvocation.resolveMethodBinding();
			return superMethodBinding != null
					&& !(superMethodBinding.isParameterizedMethod() && superMethodInvocation.typeArguments()
						.isEmpty());
		}
		return true;
	}

	private boolean isAssertEqualsWithObjectArrayArguments(String methodName, List<Expression> assertionArguments) {
		int assertionArgumentsSize = assertionArguments.size();
		if (assertionArgumentsSize < 2 || assertionArgumentsSize > 3 || !"assertEquals".equals(methodName)) { //$NON-NLS-1$
			return false;
		}

		return isObjectArray(assertionArguments.get(assertionArgumentsSize - 1)
			.resolveTypeBinding())
				&& isObjectArray(assertionArguments.get(assertionArgumentsSize - 2)
					.resolveTypeBinding());
	}

	private static boolean isObjectArray(ITypeBinding parameterType) {
		if (parameterType.isArray() && parameterType.getDimensions() == 1) {
			return isContentOfType(parameterType.getComponentType(), "java.lang.Object"); //$NON-NLS-1$
		}
		return false;
	}

	private Optional<Expression> findMessageMovedToLastPosition(IMethodBinding methodBinding,
			List<Expression> arguments) {

		if (arguments.size() < 2) {
			return Optional.empty();
		}

		if (!"org.junit.jupiter.api.Assertions".equals(classDeclaringMethodReplacement)) { //$NON-NLS-1$
			return Optional.empty();
		}

		if (arguments.size() == 2 && "assertEquals".equals(methodBinding.getName())) { //$NON-NLS-1$
			boolean comparingStringArgumentsWithoutMessage = arguments.stream()
				.map(Expression::resolveTypeBinding)
				.allMatch(typeBinding -> isContentOfType(typeBinding, JAVA_LANG_STRING));
			if (comparingStringArgumentsWithoutMessage) {
				return Optional.empty();
			}
		}
		Expression firstArgument = arguments.get(0);
		if (isContentOfType(firstArgument.resolveTypeBinding(), JAVA_LANG_STRING)) {
			return Optional.of(firstArgument);
		}
		return Optional.empty();
	}
}
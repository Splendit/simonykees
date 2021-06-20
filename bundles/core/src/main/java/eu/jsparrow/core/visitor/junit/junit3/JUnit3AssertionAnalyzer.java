package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class JUnit3AssertionAnalyzer {
	private static final String JAVA_LANG_STRING = java.lang.String.class.getName();

	private Junit3MigrationConfiguration migrationConfiguration;
	private JUnit3TestMethodsStore testMethodStore;
	private MethodInvocation methodInvocation;
	private IMethodBinding methodBinding;
	private Expression messageMovedToLastPosition;
	private JUnit3AssertionAnalysisResult analysisResult;

	boolean analyzeMethodInvocation(Junit3MigrationConfiguration migrationConfiguration,
			JUnit3TestMethodsStore testMethodStore, MethodInvocation methodInvocation, IMethodBinding methodBinding) {

		this.migrationConfiguration = migrationConfiguration;
		this.testMethodStore = testMethodStore;
		this.methodInvocation = methodInvocation;
		this.methodBinding = methodBinding;
		if (methodBinding == null) {
			return false;
		}
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		String declaringClassQualifiedName = declaringClass
			.getQualifiedName();

		if (!JUnit3Utilities.isQualifiedNameInsideJUnit3(declaringClassQualifiedName)) {
			return true;
		}

		if (!isSupportedTestCaseMethod(methodBinding, declaringClass)) {
			return false;
		}

		if (testMethodStore.isSurroundedWithJUnit3Test(methodInvocation)) {
			return false;
		}

		List<Expression> assertionArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		if (isAssertEqualsWithObjectArrayArguments(methodBinding.getName(), assertionArguments)) {
			return false;
		}

		messageMovedToLastPosition = findMessageMovedToLastPosition(methodBinding, assertionArguments).orElse(null);

		analysisResult = new JUnit3AssertionAnalysisResult(this);
		return true;
	}

	private boolean isSupportedTestCaseMethod(IMethodBinding methodBinding, ITypeBinding declaringClass) {
		/*
		 * TODO: maybe allow also:
		 * junit.framework.Assert".equals(declaringClassQualifiedName)
		 */
		if (isContentOfType(declaringClass, "junit.framework.TestCase")) {//$NON-NLS-1$
			String simpleMethodName = methodBinding.getName();
			return simpleMethodName.startsWith("assert") || //$NON-NLS-1$
					"fail".equals(simpleMethodName); //$NON-NLS-1$
		}
		return false;
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

	/**
	 * TODO: discuss code redundancy because class
	 * {@code JUnit4InvocationReplacementAnalysis} contains such a method.
	 */
	private static boolean isObjectArray(ITypeBinding parameterType) {
		if (parameterType.isArray() && parameterType.getDimensions() == 1) {
			return isContentOfType(parameterType.getComponentType(), "java.lang.Object"); //$NON-NLS-1$
		}
		return false;
	}

	private Optional<Expression> findMessageMovedToLastPosition(IMethodBinding methodBinding,
			List<Expression> arguments) {

		if (!"org.junit.jupiter.api.Assertions".equals(migrationConfiguration.getAssertionClassQualifiedName())) { //$NON-NLS-1$
			return Optional.empty();
		}

		if (arguments.size() < 2) {
			return Optional.empty();
		}

		ITypeBinding[] declaredParameterTypes = methodBinding
			.getMethodDeclaration()
			.getParameterTypes();

		if (declaredParameterTypes.length < 2) {
			return Optional.empty();
		}

		if (arguments.size() == 2 && "assertEquals".equals(methodBinding.getName()) //$NON-NLS-1$
				&& isContentOfType(declaredParameterTypes[0], JAVA_LANG_STRING)
				&& isContentOfType(declaredParameterTypes[1], JAVA_LANG_STRING)) {
			return Optional.empty();
		}

		if (isContentOfType(declaredParameterTypes[0], JAVA_LANG_STRING)) {
			return Optional.of(arguments.get(0));
		}
		return Optional.empty();
	}

	public JUnit3AssertionAnalysisResult getAnalysisResult() {
		return analysisResult;
	}
}
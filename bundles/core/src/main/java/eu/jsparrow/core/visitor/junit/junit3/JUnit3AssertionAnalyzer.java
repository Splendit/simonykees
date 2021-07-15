package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Analyzes JUnit3 assertions found in a given {@link CompilationUnit}.
 * Determines whether the transformation can be carried out or is prohibited due
 * to a JUnit3 assertion which is not supported.
 * 
 * @since 4.1.0
 *
 */
class JUnit3AssertionAnalyzer {
	private static final String JAVA_LANG_STRING = java.lang.String.class.getName();
	private final List<JUnit3AssertionAnalysisResult> jUnit3AssertionAnalysisResults = new ArrayList<>();

	/**
	 * Fills an internal list with all analysis data found for supported JUnit3
	 * assertions.
	 * 
	 * @return {@code true} if all JUnit3 assertions can be supported and
	 *         {@code false} as soon as the first assertion occurs which
	 *         prohibits transformation.
	 */
	boolean analyzeAllMethodInvocations(CompilationUnit compilationUnit,
			JUnit3DataCollectorVisitor jUnit3DeclarationsCollectorVisitor,
			Junit3MigrationConfiguration migrationConfiguration) {

		String classDeclaringMethodReplacement = migrationConfiguration.getAssertionClassQualifiedName();
		List<MethodInvocation> methodInvocationsToAnalyze = jUnit3DeclarationsCollectorVisitor
			.getMethodInvocationsToAnalyze();

		for (MethodInvocation methodinvocation : methodInvocationsToAnalyze) {
			IMethodBinding methodBinding = methodinvocation.resolveMethodBinding();
			if (methodBinding == null) {
				return false;
			}
			JUnit3AssertionAnalysisResult assertionAnalysisResult = findAssertionAnalysisResult(
					classDeclaringMethodReplacement, methodinvocation,
					methodBinding).orElse(null);
			if (assertionAnalysisResult != null) {
				jUnit3AssertionAnalysisResults.add(assertionAnalysisResult);
			} else if (UnexpectedJunit3References.isUnexpectedJUnitReference(methodBinding.getDeclaringClass())) {
				ASTNode declaringNode = compilationUnit.findDeclaringNode(methodBinding);
				if (declaringNode == null) {
					return false;
				}
				if (declaringNode.getNodeType() != ASTNode.METHOD_DECLARATION) {
					return false;
				}
				if (declaringNode.getLocationInParent() != TypeDeclaration.BODY_DECLARATIONS_PROPERTY) {
					return false;
				}
				if (!jUnit3DeclarationsCollectorVisitor.getJUnit3TestCaseDeclarations()
					.contains(declaringNode.getParent())) {
					return false;
				}
			} else if (UnexpectedJunit3References.isUnexpectedJUnitReference(methodBinding.getReturnType())) {
				return false;
			}
		}
		return true;
	}

	private Optional<JUnit3AssertionAnalysisResult> findAssertionAnalysisResult(
			String classDeclaringMethodReplacement,
			MethodInvocation methodInvocation,
			IMethodBinding methodBinding) {

		if (!isSupportedTestCaseMethod(methodBinding)) {
			return Optional.empty();
		}

		List<Expression> assertionArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		boolean allArgumentsWithExplicitType = assertionArguments
			.stream()
			.allMatch(this::isArgumentWithExplicitType);
		if (!allArgumentsWithExplicitType) {
			return Optional.empty();
		}

		if (isAssertEqualsWithObjectArrayArguments(methodBinding.getName(), assertionArguments)) {
			return Optional.empty();
		}

		if ("org.junit.jupiter.api.Assertions".equals(classDeclaringMethodReplacement)) { //$NON-NLS-1$
			JUnit3AssertionAnalysisResult analysisResultForJupiter = findMessageMovedToLastPosition(methodBinding,
					assertionArguments)
						.map(messageMovedToLastPosition -> new JUnit3AssertionAnalysisResult(methodInvocation,
								assertionArguments, messageMovedToLastPosition))
						.orElse(new JUnit3AssertionAnalysisResult(methodInvocation, assertionArguments));

			return Optional.of(analysisResultForJupiter);
		}
		return Optional.of(new JUnit3AssertionAnalysisResult(methodInvocation, assertionArguments));
	}

	private boolean isSupportedTestCaseMethod(IMethodBinding methodBinding) {
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();

		if (isContentOfType(declaringClass, "junit.framework.TestCase") || //$NON-NLS-1$
				isContentOfType(declaringClass, "junit.framework.Assert")) { //$NON-NLS-1$
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

	public List<JUnit3AssertionAnalysisResult> getjUnit3AssertionAnalysisResults() {
		return jUnit3AssertionAnalysisResults;
	}

}
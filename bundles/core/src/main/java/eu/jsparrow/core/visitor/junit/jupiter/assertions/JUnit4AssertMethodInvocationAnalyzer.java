package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.core.visitor.junit.jupiter.RegexJUnitQualifiedName.isJUnitJupiterName;
import static eu.jsparrow.core.visitor.junit.jupiter.RegexJUnitQualifiedName.isJUnitName;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfTypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.core.visitor.junit.jupiter.AnnotationCollectorVisitor;
import eu.jsparrow.core.visitor.junit.jupiter.common.MethodDeclarationsCollectorVisitor;
import eu.jsparrow.core.visitor.junit.jupiter.common.MethodInvocationsCollectorVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Helper class analyzing a {@link MethodInvocation}-node . If the
 * {@link MethodInvocation} represents the invocation of one of the supported
 * methods of the class {@code org.junit.Assert}, then all necessary
 * informations for a possible transformation are collected in an instance of
 * {@link JUnit4AssertMethodInvocationAnalysisResult}.
 * 
 * @since 3.28.0
 *
 */
class JUnit4AssertMethodInvocationAnalyzer {
	static final String ASSERT_THROWS = "assertThrows"; //$NON-NLS-1$
	private static final List<String> ORG_JUNIT_JUPITER_API_ANNOTATIONS = Collections.unmodifiableList(Arrays.asList(
			"org.junit.jupiter.api.Disabled", //$NON-NLS-1$
			"org.junit.jupiter.api.Test", //$NON-NLS-1$
			"org.junit.jupiter.api.AfterEach", //$NON-NLS-1$
			"org.junit.jupiter.api.AfterAll", //$NON-NLS-1$
			"org.junit.jupiter.api.BeforeEach", //$NON-NLS-1$
			"org.junit.jupiter.api.BeforeAll" //$NON-NLS-1$
	));

	private final List<MethodDeclaration> jUnitJupiterTestMethods;

	JUnit4AssertMethodInvocationAnalyzer(CompilationUnit compilationUnit) {
		MethodDeclarationsCollectorVisitor methodDeclarationsCollectorVisitor = new MethodDeclarationsCollectorVisitor();
		compilationUnit.accept(methodDeclarationsCollectorVisitor);
		jUnitJupiterTestMethods = methodDeclarationsCollectorVisitor.getMethodDeclarations()
			.stream()
			.filter(this::isJUnitJupiterTestMethod)
			.collect(Collectors.toList());
	}

	List<JUnit4AssertMethodInvocationAnalysisResult> collectJUnit4AssertionAnalysisResults(
			CompilationUnit compilationUnit) {

		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);
		List<MethodInvocation> allMethodInvocations = invocationCollectorVisitor.getMethodInvocations();

		return allMethodInvocations
			.stream()
			.map(this::findAnalysisResult)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}

	private Optional<JUnit4AssertMethodInvocationAnalysisResult> findAnalysisResult(
			MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return Optional.empty();
		}
		if (!isSupportedJUnit4AssertMethod(methodBinding)) {
			return Optional.empty();
		}
		boolean transformableInvocation = isTransformableInvocation(methodInvocation);

		ITypeBinding[] declaredParameterTypes = methodBinding.getMethodDeclaration()
			.getParameterTypes();
		boolean messageAsFirstParameter = declaredParameterTypes.length > 0
				&& isParameterTypeString(declaredParameterTypes[0]);

		String methodName = methodBinding.getName();
		if (isDeprecatedAssertEqualsComparingObjectArrays(methodName, declaredParameterTypes)) {
			return Optional.of(new JUnit4AssertMethodInvocationAnalysisResult(methodInvocation,
					"assertArrayEquals", messageAsFirstParameter, transformableInvocation)); //$NON-NLS-1$
		} else {
			return Optional.of(new JUnit4AssertMethodInvocationAnalysisResult(methodInvocation,
					messageAsFirstParameter, transformableInvocation));
		}
	}

	boolean isSupportedJUnit4AssertMethod(IMethodBinding methodBinding) {
		return isContentOfType(methodBinding.getDeclaringClass(), "org.junit.Assert") //$NON-NLS-1$
				&& !methodBinding.getName()
					.equals("assertThat"); //$NON-NLS-1$
	}

	private boolean isDeprecatedAssertEqualsComparingObjectArrays(String methodName,
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

	private boolean isParameterTypeObjectArray(ITypeBinding parameterType) {
		if (parameterType.isArray() && parameterType.getDimensions() == 1) {
			return isContentOfType(parameterType.getComponentType(), "java.lang.Object"); //$NON-NLS-1$
		}
		return false;
	}

	private boolean isParameterTypeString(ITypeBinding parameterType) {
		return isContentOfType(parameterType, "java.lang.String"); //$NON-NLS-1$
	}

	private boolean isArgumentWithUnambiguousType(Expression expression) {
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

	private boolean isTransformableInvocation(MethodInvocation methodInvocation) {
		if (!isWithinJUnitJupiterTest(methodInvocation)) {
			return false;
		}
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);

		String methodIdentifier = methodInvocation.getName()
			.getIdentifier();

		boolean unambiguousArgumentTypes = arguments
			.stream()
			.allMatch(this::isArgumentWithUnambiguousType);

		if (unambiguousArgumentTypes && methodIdentifier.equals(ASSERT_THROWS)) {
			int throwingRunnableArgumentIndex = arguments.size() - 1;
			Expression throwingRunnableArgument = arguments.get(throwingRunnableArgumentIndex);
			return throwingRunnableArgument.getNodeType() == ASTNode.LAMBDA_EXPRESSION;
		}

		return unambiguousArgumentTypes;
	}

	private boolean isJUnitJupiterTestMethod(MethodDeclaration methodDeclaration) {
		if (methodDeclaration.getLocationInParent() != TypeDeclaration.BODY_DECLARATIONS_PROPERTY) {
			return false;
		}
		TypeDeclaration typeDeclaration = (TypeDeclaration) methodDeclaration.getParent();
		if (typeDeclaration.isLocalTypeDeclaration()) {
			return false;
		}
		boolean containsJUnitJupiterAnnotation = ASTNodeUtil
			.convertToTypedList(methodDeclaration.modifiers(), Annotation.class)
			.stream()
			.map(Annotation::resolveAnnotationBinding)
			.map(IAnnotationBinding::getAnnotationType)
			.anyMatch(typeBinding -> isContentOfTypes(typeBinding, ORG_JUNIT_JUPITER_API_ANNOTATIONS));

		if (!containsJUnitJupiterAnnotation) {
			return false;
		}
		AnnotationCollectorVisitor annotationCollector = new AnnotationCollectorVisitor();
		methodDeclaration.accept(annotationCollector);
		return annotationCollector.getAnnotations()
			.stream()
			.noneMatch(this::isJUnit4Annotation);
	}

	private boolean isJUnit4Annotation(Annotation annotation) {
		IAnnotationBinding resolveAnnotationBinding = annotation.resolveAnnotationBinding();
		String qualifiedTypeName = resolveAnnotationBinding
			.getAnnotationType()
			.getQualifiedName();
		return isJUnitName(qualifiedTypeName) && !isJUnitJupiterName(qualifiedTypeName);
	}

	private boolean isWithinJUnitJupiterTest(MethodInvocation methodInvocation) {
		BodyDeclaration bodyDeclarationAncestor = ASTNodeUtil.getSpecificAncestor(methodInvocation,
				BodyDeclaration.class);
		ASTNode parent = methodInvocation.getParent();
		while (parent != null) {
			if (parent == bodyDeclarationAncestor) {
				if (parent.getNodeType() == ASTNode.METHOD_DECLARATION) {
					return jUnitJupiterTestMethods.contains(parent);
				}
				return false;
			}
			if (parent.getNodeType() == ASTNode.LAMBDA_EXPRESSION) {
				return false;
			}
			parent = parent.getParent();
		}
		return false;
	}
}

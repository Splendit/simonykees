package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.core.visitor.junit.jupiter.RegexJUnitQualifiedName.isJUnitJupiterName;
import static eu.jsparrow.core.visitor.junit.jupiter.RegexJUnitQualifiedName.isJUnitName;
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
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.core.visitor.junit.jupiter.AnnotationCollectorVisitor;
import eu.jsparrow.core.visitor.junit.jupiter.common.MethodDeclarationsCollectorVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Helper class storing all JUnit Jupiter test methods found in a
 * {@link CompilationUnit}.
 *
 * @since 3.29.0
 */
class JUnitJupiterTestMethodsStore {

	private static final List<String> ORG_JUNIT_JUPITER_TEST_ANNOTATIONS = Collections.unmodifiableList(Arrays.asList(
			"org.junit.jupiter.api.Test", //$NON-NLS-1$
			"org.junit.jupiter.params.ParameterizedTest", //$NON-NLS-1$
			"org.junit.jupiter.api.RepeatedTest", //$NON-NLS-1$
			"org.junit.jupiter.api.TestFactory", //$NON-NLS-1$
			"org.junit.jupiter.api.TestTemplate", //$NON-NLS-1$
			"org.junit.jupiter.api.BeforeEach", //$NON-NLS-1$
			"org.junit.jupiter.api.AfterEach", //$NON-NLS-1$
			"org.junit.jupiter.api.BeforeAll", //$NON-NLS-1$
			"org.junit.jupiter.api.AfterAll", //$NON-NLS-1$
			"org.junit.jupiter.api.Disabled" //$NON-NLS-1$
	));

	private final List<MethodDeclaration> jUnitJupiterTestMethods;

	JUnitJupiterTestMethodsStore(CompilationUnit compilationUnit) {
		MethodDeclarationsCollectorVisitor methodDeclarationsCollectorVisitor = new MethodDeclarationsCollectorVisitor();
		compilationUnit.accept(methodDeclarationsCollectorVisitor);
		jUnitJupiterTestMethods = methodDeclarationsCollectorVisitor.getMethodDeclarations()
			.stream()
			.filter(this::isJUnitJupiterTestMethod)
			.collect(Collectors.toList());

	}

	private boolean isJUnitJupiterTestMethod(MethodDeclaration methodDeclaration) {
		if (methodDeclaration.getLocationInParent() != TypeDeclaration.BODY_DECLARATIONS_PROPERTY) {
			return false;
		}
		TypeDeclaration typeDeclaration = (TypeDeclaration) methodDeclaration.getParent();
		if (typeDeclaration.isLocalTypeDeclaration()) {
			return false;
		}

		boolean containsJUnitJupiterTestAnnotation = ASTNodeUtil
			.convertToTypedList(methodDeclaration.modifiers(), Annotation.class)
			.stream()
			.map(Annotation::resolveAnnotationBinding)
			.map(IAnnotationBinding::getAnnotationType)
			.anyMatch(typeBinding -> isContentOfTypes(typeBinding, ORG_JUNIT_JUPITER_TEST_ANNOTATIONS));

		if (!containsJUnitJupiterTestAnnotation) {
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

	Optional<MethodDeclaration> findSurroundingJUnitJupiterTest(ASTNode node) {
		BodyDeclaration bodyDeclarationAncestor = ASTNodeUtil.getSpecificAncestor(node,
				BodyDeclaration.class);
		ASTNode parent = node.getParent();
		while (parent != null) {
			if (parent == bodyDeclarationAncestor) {
				if (parent.getNodeType() == ASTNode.METHOD_DECLARATION && jUnitJupiterTestMethods.contains(parent)) {
					MethodDeclaration surroundingJUnitJupiterTest = (MethodDeclaration) parent;
					return Optional.of(surroundingJUnitJupiterTest);
				}
				return Optional.empty();
			}
			if (parent.getNodeType() == ASTNode.LAMBDA_EXPRESSION) {
				return Optional.empty();
			}
			parent = parent.getParent();
		}
		return Optional.empty();
	}
}
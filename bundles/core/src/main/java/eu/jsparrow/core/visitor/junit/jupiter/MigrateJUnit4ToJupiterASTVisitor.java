package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * O
 * 
 * @since 3.27.0
 *
 */
public class MigrateJUnit4ToJupiterASTVisitor extends AbstractAddImportASTVisitor {

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);

		if (!continueVisiting) {
			return false;
		}

		AnnotationCollectorVisitor collectorVisitor = new AnnotationCollectorVisitor();
		compilationUnit.accept(collectorVisitor);
		List<Annotation> jUnit4Annotations = collectorVisitor.getAnnotations();

		boolean analysisOK = jUnit4Annotations.stream()
			.allMatch(this::checkAnnotation);

		if (analysisOK) {
			transform(jUnit4Annotations);
		}
		return true;
	}

	private boolean isEmptyAnnotation(Annotation annotation) {
		if (annotation.isMarkerAnnotation()) {
			return true;
		}
		if (annotation.isNormalAnnotation()) {
			NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
			if (normalAnnotation.values()
				.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private boolean isJUnit4AnnotationType(ITypeBinding typeBinding) {
		IPackageBinding packageBinding = typeBinding.getPackage();

		if (packageBinding == null) {
			return false;

		}
		if (!packageBinding.getName()
			.equals("org.junit")) { //$NON-NLS-1$ )
			return false;
		}
		return true;
	}

	private boolean checkAnnotation(Annotation annotation) {

		ITypeBinding typeBinding = annotation.resolveTypeBinding();

		String simpleTypeName = typeBinding.getName();
		if (simpleTypeName.equals("Ignore")) { //$NON-NLS-1$
			return isJUnit4AnnotationType(typeBinding)
					&& annotation.getTypeName()
						.isSimpleName();
		}
		if (simpleTypeName.equals("Test") //$NON-NLS-1$
				|| simpleTypeName.equals("Before") //$NON-NLS-1$
				|| simpleTypeName.equals("BeforeClass") //$NON-NLS-1$
				|| simpleTypeName.equals("After") //$NON-NLS-1$
				|| simpleTypeName.equals("AfterClass") //$NON-NLS-1$
		) {
			return isJUnit4AnnotationType(typeBinding) &&
					isEmptyAnnotation(annotation) && annotation.getTypeName()
						.isSimpleName();
		}

		String qualifiedTypeName = typeBinding.getQualifiedName();
		return !qualifiedTypeName.startsWith("org.junit.") //$NON-NLS-1$
				&& !qualifiedTypeName.startsWith("junit."); //$NON-NLS-1$
	}

	private void transform(List<Annotation> jUnit4Annotations) {
		List<SimpleName> simpleAnnotationTypeNames = jUnit4Annotations.stream()
			.filter(annotation -> isJUnit4AnnotationType(annotation.resolveTypeBinding()))
			.map(annotation -> annotation.getTypeName())
			.filter(Name::isSimpleName)
			.map(SimpleName.class::cast)
			.collect(Collectors.toList());
		simpleAnnotationTypeNames.size();

		simpleAnnotationTypeNames.forEach(simpleName -> {
			findIdentifierReplacement(simpleName).ifPresent(newIdentifier -> {
				SimpleName newSimpleName = astRewrite.getAST()
					.newSimpleName(newIdentifier);
				this.astRewrite.replace(simpleName, newSimpleName, null);
				onRewrite();
			});
		});
	}

	private Optional<String> findIdentifierReplacement(SimpleName simpleName) {
		String identifier = simpleName.getIdentifier();
		if (identifier.equals("Ignore")) { //$NON-NLS-1$
			return Optional.of("Disabled"); //$NON-NLS-1$
		}
		if (identifier.equals("Before")) { //$NON-NLS-1$
			return Optional.of("BeforeEach"); //$NON-NLS-1$
		}
		if (identifier.equals("BeforeClass")) { //$NON-NLS-1$
			return Optional.of("BeforeAll"); //$NON-NLS-1$
		}
		if (identifier.equals("After")) { //$NON-NLS-1$
			return Optional.of("AfterEach"); //$NON-NLS-1$
		}
		if (identifier.equals("AfterClass")) { //$NON-NLS-1$
			return Optional.of("AfterAll"); //$NON-NLS-1$
		}
		return Optional.empty();
	}
}

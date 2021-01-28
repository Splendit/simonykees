package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NormalAnnotation;

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
			return isJUnit4AnnotationType(typeBinding);
		}
		if (simpleTypeName.equals("Test") //$NON-NLS-1$
				|| simpleTypeName.equals("Before") //$NON-NLS-1$
				|| simpleTypeName.equals("BeforeClass") //$NON-NLS-1$
				|| simpleTypeName.equals("After") //$NON-NLS-1$
				|| simpleTypeName.equals("AfterClass") //$NON-NLS-1$
		) {
			return isJUnit4AnnotationType(typeBinding) && isEmptyAnnotation(annotation);
		}

		String qualifiedTypeName = typeBinding.getQualifiedName();
		return !qualifiedTypeName.startsWith("org.junit.") //$NON-NLS-1$
				&& !qualifiedTypeName.startsWith("junit."); //$NON-NLS-1$
	}

	private void transform(List<Annotation> jUnit4Annotations) {

	}

}

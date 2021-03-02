package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.core.visitor.junit.jupiter.RegexJUnitQualifiedName.*;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

/**
 * Helper visitor analyzing a compilation unit to decide if it is possible to
 * migrate JUnit-4-annotations to JUnit-Jupiter-annotations.
 *
 * @since 3.27.0
 */
class JUnit4AssertToJupiterAnalyzerVisitor extends ASTVisitor {

	private boolean transformationPossible = true;

	@Override
	public boolean preVisit2(ASTNode node) {
		return transformationPossible;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		String packageName = node.resolveBinding()
			.getName();
		transformationPossible = !isJUnitName(packageName);
		return false;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		transformationPossible = !isJUnit4Annotation(node);
		return transformationPossible;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		transformationPossible = !isJUnit4Annotation(node);
		return transformationPossible;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		transformationPossible = !isJUnit4Annotation(node);
		return transformationPossible;
	}

	private boolean isJUnit4Annotation(Annotation annotation) {
		IAnnotationBinding resolveAnnotationBinding = annotation.resolveAnnotationBinding();
		String qualifiedTypeName = resolveAnnotationBinding
			.getAnnotationType()
			.getQualifiedName();
		return isJUnitName(qualifiedTypeName) && !isJUnitJupiterName(qualifiedTypeName);
	}

	boolean isTransformationPossible() {
		return transformationPossible;
	}
}

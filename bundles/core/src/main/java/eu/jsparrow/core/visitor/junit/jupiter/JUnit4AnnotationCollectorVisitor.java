package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

/**
 * Collects the following annotations: <br>
 * <ul>
 * <li>Annotations having the simple type names<br>
 * {@code Test}, {@code Ignore}, {@code Before}, {@code BeforeClass},
 * {@code After} and {@code AfterClass}</li>
 * <li>All other annotations which are JUnit-4-annotations.</li>
 * </ul>
 *
 */
public class JUnit4AnnotationCollectorVisitor extends ASTVisitor {

	private final List<Annotation> jUnit4Annotations = new ArrayList<>();

	@Override
	public boolean visit(MarkerAnnotation node) {
		if (isAnnotationToBeAddedToList(node)) {
			jUnit4Annotations.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		if (isAnnotationToBeAddedToList(node)) {
			jUnit4Annotations.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		if (isAnnotationToBeAddedToList(node)) {
			jUnit4Annotations.add(node);
		}
		return true;
	}

	private boolean isAnnotationToBeAddedToList(Annotation annotation) {
		Name name = annotation.getTypeName();
		if (name.getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleName simpleName = (SimpleName) name;
			String identifier = simpleName.getIdentifier();
			if (identifier.equals("Test") //$NON-NLS-1$
					|| identifier.equals("Ignore") //$NON-NLS-1$
					|| identifier.equals("Before") //$NON-NLS-1$
					|| identifier.equals("BeforeClass") //$NON-NLS-1$
					|| identifier.equals("After") //$NON-NLS-1$
					|| identifier.equals("AfterClass") //$NON-NLS-1$
			) {
				return true;
			}
		}
		
		String qualifiedName = annotation.resolveTypeBinding()
			.getQualifiedName();
		return qualifiedName.startsWith("org.junit.") //$NON-NLS-1$
				|| qualifiedName.startsWith("junit."); //$NON-NLS-1$

	}

	public List<Annotation> getJUnit4Annotations() {
		return jUnit4Annotations;
	}

}

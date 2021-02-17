package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
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
 * @since 3.27.0
 */
public class AnnotationCollectorVisitor extends ASTVisitor {

	private final List<Annotation> jUnit4Annotations = new ArrayList<>();

	@Override
	public boolean visit(MarkerAnnotation node) {
		jUnit4Annotations.add(node);
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		jUnit4Annotations.add(node);
		return true;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		jUnit4Annotations.add(node);
		return true;
	}

	public List<Annotation> getAnnotations() {
		return jUnit4Annotations;
	}
}

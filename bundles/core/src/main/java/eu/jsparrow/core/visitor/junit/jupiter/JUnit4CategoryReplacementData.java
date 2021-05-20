package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;

/**
 * Stores all informations which are necessary to replace a JUnit4
 * {@code @Category} annotation with a JUnit Jupiter {@code @Tag} annotation
 * 
 * @since 3.30.0
 * 
 */
public class JUnit4CategoryReplacementData {
	private final Annotation categoryAnnotation;
	private final List<String> categoryNames;
	private final ChildListPropertyDescriptor locationInParent;

	public JUnit4CategoryReplacementData(Annotation categoryAnnotation, List<String> categoryNames,
			ChildListPropertyDescriptor locationInParent) {
		this.categoryAnnotation = categoryAnnotation;
		this.categoryNames = categoryNames;
		this.locationInParent = locationInParent;
	}

	public Annotation getCategoryAnnotation() {
		return categoryAnnotation;
	}

	public List<String> getCategoryNames() {
		return categoryNames;
	}

	public ChildListPropertyDescriptor getLocationInParent() {
		return locationInParent;
	}
}

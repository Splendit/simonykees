package eu.jsparrow.core.visitor.junit.jupiter;

import org.eclipse.jdt.core.dom.Name;

public class AnnotationTransformationData {
	private final Name originalTypeName;
	private final String newTypeName;	
	
	public AnnotationTransformationData(Name originalTypeName, String newTypeName) {
		this.originalTypeName = originalTypeName;
		this.newTypeName = newTypeName;
	}
	public Name getOriginalTypeName() {
		return originalTypeName;
	}
	public String getNewTypeName() {
		return newTypeName;
	}
}

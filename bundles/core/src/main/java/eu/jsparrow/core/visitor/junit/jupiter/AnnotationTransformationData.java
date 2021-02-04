package eu.jsparrow.core.visitor.junit.jupiter;

import org.eclipse.jdt.core.dom.Name;

public class AnnotationTransformationData {
	private final Name originalTypeName;
	private final String newTypeQualifiedName;	
	
	public AnnotationTransformationData(Name originalTypeName, String newTypeQualifiedName) {
		this.originalTypeName = originalTypeName;
		this.newTypeQualifiedName = newTypeQualifiedName;
	}
	public Name getOriginalTypeName() {
		return originalTypeName;
	}
	public String getNewTypeQualifiedName() {
		return newTypeQualifiedName;
	}
}

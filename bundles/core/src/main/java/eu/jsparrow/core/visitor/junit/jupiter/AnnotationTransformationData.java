package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.Optional;

import org.eclipse.jdt.core.dom.Name;

/**
 * A wrapper for the details needed to transform an annotation.
 * 
 * @since 3.27.0
 */
public class AnnotationTransformationData {
	private final Name originalTypeName;
	private final String newTypeName;
	private String safeNewTypeImport;

	public AnnotationTransformationData(Name originalTypeName, String newTypeUnqualifiedName,
			String newTypeQualifiedName) {
		this(originalTypeName, newTypeUnqualifiedName);
		this.safeNewTypeImport = newTypeQualifiedName;
	}

	public AnnotationTransformationData(Name originalTypeName, String newQualifiedTypeName) {
		this.originalTypeName = originalTypeName;
		this.newTypeName = newQualifiedTypeName;
	}

	public Name getOriginalTypeName() {
		return originalTypeName;
	}

	public String getNewTypeName() {
		return newTypeName;
	}

	public Optional<String> getSafeNewTypeImport() {
		return Optional.ofNullable(safeNewTypeImport);
	}
}

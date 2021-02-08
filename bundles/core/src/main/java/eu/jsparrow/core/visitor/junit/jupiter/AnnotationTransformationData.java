package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.Optional;

import org.eclipse.jdt.core.dom.Name;

public class AnnotationTransformationData {
	private final Name originalTypeName;
	private final String newTypeName;
	private final String safeNewTypeImport;

	public AnnotationTransformationData(Name originalTypeName, String newTypeUnqualifiedName,
			String newTypeQualifiedName) {
		this.originalTypeName = originalTypeName;
		this.newTypeName = newTypeUnqualifiedName;
		this.safeNewTypeImport = newTypeQualifiedName;
	}

	public AnnotationTransformationData(Name originalTypeName, String newQualifiedTypeName) {
		this.originalTypeName = originalTypeName;
		this.newTypeName = newQualifiedTypeName;
		this.safeNewTypeImport = null;
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

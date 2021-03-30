package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Optional;

import org.eclipse.jdt.core.dom.Type;

public class ThrowingRunnableArgumentAnalysisResult {
	private final boolean transformable;
	private final Type localVariableTypeToReplace;

	public ThrowingRunnableArgumentAnalysisResult(boolean transformable,
			Type localThrowingRunnableVariableDeclaration) {
		this.transformable = transformable;
		this.localVariableTypeToReplace = localThrowingRunnableVariableDeclaration;
	}

	public ThrowingRunnableArgumentAnalysisResult(boolean transformable) {
		this.transformable = transformable;
		this.localVariableTypeToReplace = null;
	}

	public boolean isTransformable() {
		return transformable;
	}

	public Optional<Type> getLocalVariableTypeToReplace() {
		return Optional.ofNullable(localVariableTypeToReplace);
	}
}
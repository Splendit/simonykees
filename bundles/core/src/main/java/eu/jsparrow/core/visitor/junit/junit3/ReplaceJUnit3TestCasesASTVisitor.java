package eu.jsparrow.core.visitor.junit.junit3;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class ReplaceJUnit3TestCasesASTVisitor extends AbstractAddImportASTVisitor {
	private final boolean transformationToJupiter;

	public ReplaceJUnit3TestCasesASTVisitor(boolean transformationToJupiter) {
		this.transformationToJupiter = transformationToJupiter;
	}

	public boolean isTransformationToJupiter() {
		return transformationToJupiter;
	}
}
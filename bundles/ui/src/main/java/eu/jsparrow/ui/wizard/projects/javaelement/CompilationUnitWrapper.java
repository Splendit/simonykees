package eu.jsparrow.ui.wizard.projects.javaelement;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * @since 4.17.0
 */
public class CompilationUnitWrapper extends AbstractJavaElementWrapper {

	private final ICompilationUnit compilationUnit;
	private final String javaFileName;

	CompilationUnitWrapper(AbstractJavaElementParentWrapper parent, ICompilationUnit compilationUnit) {
		super(parent);
		this.compilationUnit = compilationUnit;
		this.javaFileName = compilationUnit.getElementName();
	}

	public ICompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public String getElementName() {
		return javaFileName;
	}
}

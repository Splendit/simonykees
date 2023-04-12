package eu.jsparrow.ui.wizard.projects.wrapper;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * @since 4.17.0
 */
public class CompilationUnitWrapper extends PackageFragmentChildWrapper {

	private final ICompilationUnit compilationUnit;
	private final String javaFileName;

	CompilationUnitWrapper(ICompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		this.javaFileName = compilationUnit.getElementName();
	}

	public ICompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public String getJavaFileName() {
		return javaFileName;
	}
}

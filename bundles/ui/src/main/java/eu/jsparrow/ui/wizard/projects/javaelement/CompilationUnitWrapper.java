package eu.jsparrow.ui.wizard.projects.javaelement;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * @since 4.17.0
 */
public class CompilationUnitWrapper implements AbstractJavaElementWrapper {
	private final PackageFragmentWrapper parent;
	private final ICompilationUnit compilationUnit;
	private final String javaFileName;

	CompilationUnitWrapper(PackageFragmentWrapper parent, ICompilationUnit compilationUnit) {
		this.parent = parent;
		this.compilationUnit = compilationUnit;
		this.javaFileName = compilationUnit.getElementName();
	}

	@Override
	public PackageFragmentWrapper getParent() {
		return parent;
	}

	@Override
	public ICompilationUnit getJavaElement() {
		return compilationUnit;
	}

	@Override
	public String getElementName() {
		return javaFileName;
	}
}

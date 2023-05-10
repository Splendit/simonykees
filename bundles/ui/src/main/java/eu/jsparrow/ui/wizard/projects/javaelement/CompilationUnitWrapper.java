package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * @since 4.17.0
 */
public class CompilationUnitWrapper implements IJavaElementWrapper {
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

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public List<IJavaElementWrapper> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public boolean hasChildListAtHand() {
		return false;
	}
}

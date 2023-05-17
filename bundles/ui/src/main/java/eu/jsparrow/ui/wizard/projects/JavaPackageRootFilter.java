package eu.jsparrow.ui.wizard.projects;

import org.eclipse.jface.viewers.Viewer;

import eu.jsparrow.ui.wizard.projects.javaelement.PackageFragmentRootWrapper;

public class JavaPackageRootFilter extends AbstractJavaElementWrapperFilter {

	protected JavaPackageRootFilter(String filterText) {
		super(filterText);
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof PackageFragmentRootWrapper) {
			return isMatching(((PackageFragmentRootWrapper) element));
		}
		return true;
	}

}

package eu.jsparrow.ui.wizard.projects;

import org.eclipse.jface.viewers.Viewer;

import eu.jsparrow.ui.wizard.projects.javaelement.PackageFragmentWrapper;

public class JavaPackageFilter extends AbstractJavaElementWrapperFilter {

	protected JavaPackageFilter(String filterText) {
		super(filterText);
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof PackageFragmentWrapper) {
			return isMatching(((PackageFragmentWrapper) element));
		}
		return true;
	}

}

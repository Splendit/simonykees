package eu.jsparrow.ui.wizard.projects;

import org.eclipse.jface.viewers.Viewer;

import eu.jsparrow.ui.wizard.projects.javaelement.CompilationUnitWrapper;

public class JavaFileFilter extends AbstractJavaElementWrapperFilter {

	protected JavaFileFilter(String filterText) {
		super(filterText);
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof CompilationUnitWrapper) {
			return isMatching(((CompilationUnitWrapper) element));
		}
		return true;
	}

}

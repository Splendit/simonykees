package eu.jsparrow.ui.wizard.projects;

import org.eclipse.jface.viewers.Viewer;

import eu.jsparrow.ui.wizard.projects.javaelement.JavaProjectWrapper;

public class JavaProjectFilter extends AbstractJavaElementWrapperFilter {

	public JavaProjectFilter(String textFilterProjects) {
		super(textFilterProjects);
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof JavaProjectWrapper) {
			return isMatching(((JavaProjectWrapper) element).getPathToDisplay());
		}
		return true;
	}
}

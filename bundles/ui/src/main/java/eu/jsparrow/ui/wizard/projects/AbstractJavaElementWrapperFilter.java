package eu.jsparrow.ui.wizard.projects;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ViewerFilter;

import eu.jsparrow.ui.wizard.projects.javaelement.IJavaElementWrapper;

public abstract class AbstractJavaElementWrapperFilter extends ViewerFilter {

	private final String filterText;

	protected AbstractJavaElementWrapperFilter(String filterText) {
		this.filterText = StringUtils.lowerCase(filterText);
	}

	protected boolean isMatching(IJavaElementWrapper javaElementWrapper) {
		return StringUtils.contains(StringUtils.lowerCase(javaElementWrapper.getLabelText()), filterText);
	}
}

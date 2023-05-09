package eu.jsparrow.ui.wizard.projects;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ViewerFilter;

public abstract class AbstractJavaElementWrapperFilter extends ViewerFilter {

	private final String filterText;

	protected AbstractJavaElementWrapperFilter(String filterText) {
		this.filterText = StringUtils.lowerCase(filterText);
	}

	protected boolean isMatching(String textToMatch) {
		return StringUtils.contains(StringUtils.lowerCase(textToMatch), filterText);
	}

}

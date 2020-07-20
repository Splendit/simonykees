package eu.jsparrow.ui.preview.model.summary;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * This is a search filter for file viewer in the summary page. The search match
 * is case insensitive.
 * 
 * @since 3.20.0
 *
 */
public class FileViewerFilter extends ViewerFilter {

	private String searchString;

	public void setSearchString(String searchString) {
		this.searchString = ".*" + searchString.toLowerCase() + ".*"; //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.isEmpty()) {
			return true;
		}

		ChangedFilesModel changedFilesModel = (ChangedFilesModel) element;
		String fileName = changedFilesModel.getName()
			.toLowerCase();
		if (fileName.matches(searchString)) {
			return true;
		}

		return changedFilesModel.getRules()
			.stream()
			.map(String::toLowerCase)
			.anyMatch(rule -> rule.matches(searchString));
	}

}

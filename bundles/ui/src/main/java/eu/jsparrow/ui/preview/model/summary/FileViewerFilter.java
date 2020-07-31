package eu.jsparrow.ui.preview.model.summary;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

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

		String fileName = ""; //$NON-NLS-1$
		List<String> rules = new ArrayList<>();
		if (element instanceof ChangedFilesModel) {
			ChangedFilesModel changedFilesModel = (ChangedFilesModel) element;
			fileName = changedFilesModel.getName()
				.toLowerCase();
			rules = changedFilesModel.getRules()
				.stream()
				.map(RulesPerFileModel::getName)
				.map(String::toLowerCase)
				.collect(Collectors.toList());
		} else if (element instanceof ChangedNamesInFileModel) {
			ChangedNamesInFileModel changedFilesModel = (ChangedNamesInFileModel) element;
			fileName = changedFilesModel.getFileName()
				.toLowerCase();
			rules = changedFilesModel.getRenamings()
				.stream()
				.map(RenamingPerFileModel::getName)
				.map(String::toLowerCase)
				.collect(Collectors.toList());
		}

		if (fileName.matches(searchString)) {
			return true;
		}

		return rules.stream()
			.anyMatch(rule -> rule.matches(searchString));
	}

}

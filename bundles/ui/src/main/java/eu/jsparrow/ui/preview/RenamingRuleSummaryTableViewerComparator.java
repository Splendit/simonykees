package eu.jsparrow.ui.preview;

import org.eclipse.jface.viewers.Viewer;

import eu.jsparrow.ui.preview.model.summary.RenamingPerFileModel;

/**
 * @see SortableViewerComparator
 * 
 * @since 3.20.0
 */
public class RenamingRuleSummaryTableViewerComparator extends SortableViewerComparator {
	
	public RenamingRuleSummaryTableViewerComparator() {
		super();
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {

		RenamingPerFileModel model1 = (RenamingPerFileModel) e1;
		RenamingPerFileModel model2 = (RenamingPerFileModel) e2;

		int rc = 0;
		switch (propertyIndex) {
		case 0:
			rc = model1.getName()
				.compareTo(model2.getName());
			break;
		case 1:
			rc = Long.compare(model1.getTimes(), model2.getTimes());
			break;
		default:
			rc = 0;
		}
		// If descending order, flip the direction
		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}

}

package eu.jsparrow.ui.preview.comparator;

import org.eclipse.jface.viewers.Viewer;

import eu.jsparrow.ui.preview.model.summary.RuleTimesModel;

/**
 * @see SortableViewerComparator
 * 
 * @since 3.15.0
 */
public class SummaryPageRuleTableViewerComparator extends SortableViewerComparator {
	
	public SummaryPageRuleTableViewerComparator() {
		super();
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		RuleTimesModel r1 = (RuleTimesModel) e1;
		RuleTimesModel r2 = (RuleTimesModel) e2;
		int rc = 0;
		switch (propertyIndex) {
		case 0:
			rc = r1.getName()
				.compareTo(r2.getName());
			break;
		case 1:
			rc = r1.getTimes()
				.compareTo(r2.getTimes());
			break;
		case 2:
			rc = r1.getTimeSavedDuration()
				.compareTo(r2.getTimeSavedDuration());
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

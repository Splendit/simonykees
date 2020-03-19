package eu.jsparrow.ui.preview;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import eu.jsparrow.ui.preview.model.summary.RuleTimesModel;

/**
 * This is a comparator for sorting the entries of the rules table viewer on the
 * summary page. This has been taken from: <a href=
 * "https://www.vogella.com/tutorials/EclipseJFaceTable/article.html#sort-content-of-table-columns">https://www.vogella.com/tutorials/EclipseJFaceTable/article.html#sort-content-of-table-columns</a>
 *
 * @since 3.15.0
 */
public class SummaryPageRuleTableViewerComparator extends ViewerComparator {
	private int propertyIndex;
	private static final int DESCENDING = 1;
	private int direction = DESCENDING;

	public SummaryPageRuleTableViewerComparator() {
		this.propertyIndex = 0;
		direction = DESCENDING;
	}

	public int getDirection() {
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = DESCENDING;
		}
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

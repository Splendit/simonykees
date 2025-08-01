package eu.jsparrow.rules.common.markers;

/**
 * Clients should implement a listener for {@link RefactoringMarkerEvent}.
 * 
 * @since 4.0.0
 */
public interface RefactoringMarkerListener {

	/**
	 * Update all listeners interested on {@link RefactoringMarkerEvent}.
	 * 
	 * @param event
	 *            a new {@link RefactoringMarkerEvent}.
	 */
	void update(RefactoringMarkerEvent event);
}

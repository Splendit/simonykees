package eu.jsparrow.rules.java16.patternmatching;

import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link UsePatternMatchingForInstanceofASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public interface UsePatternMatchingForInstanceofEvent {

	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param instanceOf
	 *            the original node to be replaced, i.e., an instanceof
	 *            expression
	 * @param patternInstanceOfName
	 *            the name of instanceof pattern.
	 */
	default void addMarkerEvent(InstanceofExpression instanceOf, SimpleName patternInstanceOfName) {
	}
}

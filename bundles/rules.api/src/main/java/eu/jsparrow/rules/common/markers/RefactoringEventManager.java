package eu.jsparrow.rules.common.markers;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Implementers of this interface provide functionalities for generating and
 * resolving jSparrow markers in a compilation unit.
 * 
 * @since 4.0.0
 *
 */
public interface RefactoringEventManager {

	/**
	 * Triggers {@link RefactoringMarkerEvent}s for the given compilation unit.
	 * 
	 * @param iCompilationUnit
	 *            the source to find jSparrow markers for.
	 */
	void discoverRefactoringEvents(ICompilationUnit iCompilationUnit);
	
	void discoverRefactoringEvents(ICompilationUnit iCompilationUnit, List<String> markerIds);

	/**
	 * Resolves one jSparrow marker in the given {@link ICompilationUnit}.
	 * 
	 * @param iCompilationUnit
	 *            a {@link ICompilationUnit} containing jSparrow markers.
	 * @param resolver
	 *            the ID of the resolver. Usually the fully qualified name of a
	 *            registered resolver.
	 * @param offset
	 *            the offset of the marker in the given compilation unit.
	 */
	void resolve(ICompilationUnit iCompilationUnit, String resolver, int offset);

}

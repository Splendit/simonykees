package eu.jsparrow.core.visitor.renaming;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchMatch;

/**
 * A type for encapsulating a search match and the value of the name being matched.
 * 
 * references.
 * 
 * @author Ardit Ymeri
 * @since 2.3.0
 *
 */
public class ReferenceSearchMatch extends SearchMatch {

	private String matchedName;
	private ICompilationUnit iCompilationUnit;

	public ReferenceSearchMatch(SearchMatch searchMatch, String matchedName, ICompilationUnit iCompilationUnit) {
		super((IJavaElement) searchMatch.getElement(), searchMatch.getAccuracy(), searchMatch.getOffset(),
				searchMatch.getLength(), searchMatch.getParticipant(), searchMatch.getResource());

		((IJavaElement) getElement()).getElementName();
		this.matchedName = matchedName;
		this.iCompilationUnit = iCompilationUnit;
	}

	/**
	 * 
	 * @return the identifier of the field being search for.
	 */
	public String getMatchedName() {
		return this.matchedName;
	}

	public ICompilationUnit getICompilationUnit() {
		return this.iCompilationUnit;
	}

}

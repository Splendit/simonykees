package at.splendit.simonykees.core.visitor.renaming;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchMatch;

/**
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class ReferenceSearchMatch extends SearchMatch {
	
	private String matchedName;
	private FieldMetadata metaData;

	public ReferenceSearchMatch(SearchMatch searchMatch, String matchedName) {
		super(
				(IJavaElement)searchMatch.getElement(), 
				searchMatch.getAccuracy(), 
				searchMatch.getOffset(), 
				searchMatch.getLength(), 
				searchMatch.getParticipant(), 
				searchMatch.getResource());

		((IJavaElement)getElement()).getElementName();
		this.matchedName = matchedName;
	}
	
	public String getMatchedName() {
		return this.matchedName;
	}
	
	void setMetadata(FieldMetadata metaData) {
		this.metaData = metaData;
	}
	
	public FieldMetadata getMetadata() {
		return this.metaData;
	}
}

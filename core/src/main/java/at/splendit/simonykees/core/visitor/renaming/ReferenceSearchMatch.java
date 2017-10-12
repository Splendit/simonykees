package at.splendit.simonykees.core.visitor.renaming;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchMatch;

/**
 * A type for encapsulating a search match, the value of the 
 * name being matched and the metadata related with the field 
 * which is being searched for references. 
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class ReferenceSearchMatch extends SearchMatch {
	
	private String matchedName;
	private FieldMetadata metaData;
	private ICompilationUnit iCompilationUnit;

	public ReferenceSearchMatch(SearchMatch searchMatch, String matchedName, ICompilationUnit iCompilationUnit) {
		super(
				(IJavaElement)searchMatch.getElement(), 
				searchMatch.getAccuracy(), 
				searchMatch.getOffset(), 
				searchMatch.getLength(), 
				searchMatch.getParticipant(), 
				searchMatch.getResource());

		((IJavaElement)getElement()).getElementName();
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
	
	void setMetadata(FieldMetadata metaData) {
		this.metaData = metaData;
	}
	
	public ICompilationUnit getICompilationUnit() {
		return this.iCompilationUnit;
	}
	
	/**
	 * 
	 * @return the meta data related to the field
	 */
	public FieldMetadata getMetadata() {
		return this.metaData;
	}
}

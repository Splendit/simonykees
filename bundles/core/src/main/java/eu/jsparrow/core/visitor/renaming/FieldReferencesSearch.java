package eu.jsparrow.core.visitor.renaming;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;

import eu.jsparrow.core.visitor.unused.JavaElementSearchEngine;

/**
 * A class for wrapping a {@link SearchEngine} which is used for finding 
 * the references of the field provided in the constructor. 
 * 
 * @author Ardit Ymeri
 * @since 2.3.0
 *
 */
public class FieldReferencesSearch {

	private Set<ICompilationUnit> targetIJavaElements = new HashSet<>();
	private IJavaElement[] searchScope;

	public FieldReferencesSearch(IJavaElement[] searchScope) {
		this.searchScope = searchScope;
	}

	/**
	 * Makes use of {@link SearchEngine} for finding the references of a field
	 * which is declared in the given declaration fragment. Uses
	 * {@link #searchScope} for as the scope of the search. Discards the whole
	 * search if an error occurs during the search process.
	 * 
	 * @param fragment
	 *            a declaration fragment belonging to a field declaration.
	 * @return an optional of the list of {@link ReferenceSearchMatch}s or an
	 *         empty optional if the references cannot be found.
	 */
	public Optional<List<ReferenceSearchMatch>> findFieldReferences(VariableDeclarationFragment fragment) {
		IVariableBinding fragmentBinding = fragment.resolveBinding();
		if (fragmentBinding == null) {
			return Optional.empty();
		}
		IJavaElement iVariableBinding = fragmentBinding.getJavaElement();

		/*
		 * Create a pattern that searches for references of a field.
		 */
		IField iField = (IField) iVariableBinding;
		SearchPattern searchPattern = SearchPattern.createPattern(iField, IJavaSearchConstants.REFERENCES);
		
		JavaElementSearchEngine elementSearch = new JavaElementSearchEngine(this.searchScope);
		Optional<List<ReferenceSearchMatch>> result = elementSearch.findReferences(searchPattern, fragment.getName().getIdentifier());
		this.targetIJavaElements = elementSearch.getTargetIJavaElements();
		
		return result;
	}

	/**
	 * 
	 * @return the set of the {@link IJavaElement}s containing a reference to a 
	 * field being renamed.
	 */
	public Set<ICompilationUnit> getTargetIJavaElements() {
		return this.targetIJavaElements;
	}

}

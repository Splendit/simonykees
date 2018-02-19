package eu.jsparrow.core.visitor.renaming;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.runtime.FileWithCompilationErrorException;
import eu.jsparrow.core.exception.runtime.ICompilationUnitNotFoundException;
import eu.jsparrow.rules.common.util.RefactoringUtil;

/**
 * A class for wrapping a {@link SearchEngine} which is used for finding 
 * the references of the field provided in the constructor. 
 * 
 * @author Ardit Ymeri
 * @since 2.3.0
 *
 */
public class FieldReferencesSearch {

	private static final Logger logger = LoggerFactory.getLogger(FieldReferencesSearch.class);
	
	private Set<ICompilationUnit> targetIJavaElements = new HashSet<>();

	private static final String FILE_WITH_COMPILATION_ERROR_EXCEPTION_MESSAGE = "A reference was found in a CompilationUnit with compilation errors."; //$NON-NLS-1$

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
		/*
		 * Create the search scope based on the provided scope.
		 */
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(searchScope);

		/*
		 * A list to store the references resulting from the search process.
		 */
		List<ReferenceSearchMatch> references = new ArrayList<>();
		String fragmentIdentifier = fragment.getName()
			.getIdentifier();

		/*
		 * The object that stores the search result.
		 */
		SearchRequestor requestor = createSearchRequestor(references, fragmentIdentifier);

		/*
		 * Finally, the search engine which performs the actual search based on
		 * the prepared pattern, scope and the requestor.
		 */
		SearchEngine searchEngine = new SearchEngine();
		try {
			searchEngine.search(searchPattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
					scope, requestor, null);
		} catch (CoreException | FileWithCompilationErrorException | ICompilationUnitNotFoundException e) {
			logger.error(e.getMessage());
			return Optional.empty();
		}

		return Optional.of(references);
	}

	private SearchRequestor createSearchRequestor(List<ReferenceSearchMatch> references, String fragmentIdentifier) {
		return new SearchRequestor() {

			@Override
			public void acceptSearchMatch(SearchMatch match) {

				ICompilationUnit icu = findCompilationUnit(match);
				if (icu == null) {
					references.clear();
					throw new ICompilationUnitNotFoundException("Compilation unit of the search match was not found"); //$NON-NLS-1$
				}
				ReferenceSearchMatch reference = new ReferenceSearchMatch(match, fragmentIdentifier, icu);
				references.add(reference);
				if (RefactoringUtil.checkForSyntaxErrors(icu)) {
					references.clear();
					throw new FileWithCompilationErrorException(FILE_WITH_COMPILATION_ERROR_EXCEPTION_MESSAGE);
				}
				storeIJavaElement(icu);
			}
		};
	}
	
	private ICompilationUnit findCompilationUnit(SearchMatch match) {
		ICompilationUnit icu = null;
		IJavaElement iJavaElement = (IJavaElement) match.getElement();
		if (iJavaElement instanceof IMember) {
			IMember iMember = (IMember) iJavaElement;
			icu = iMember.getCompilationUnit();

		} else if (iJavaElement instanceof IImportDeclaration) {
			IImportContainer importContainer = (IImportContainer) iJavaElement.getParent();
			icu = (ICompilationUnit) importContainer.getParent();
		}
		return icu;
	}
	
	private void storeIJavaElement(ICompilationUnit iJavaElement) {
		this.targetIJavaElements.add(iJavaElement);
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

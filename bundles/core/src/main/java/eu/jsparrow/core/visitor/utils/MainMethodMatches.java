package eu.jsparrow.core.visitor.utils;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

public class MainMethodMatches {

	private MainMethodMatches() {
		// private constructor hiding implicit public one
	}

	public static List<SearchMatch> findMainMethodMatches(ITypeBinding typeBinding) throws CoreException {
		String mainMathodPattern = typeBinding.getQualifiedName() + ".main(String[]) void"; //$NON-NLS-1$
		List<SearchMatch> matches = new LinkedList<>();

		SearchPattern searchPattern = SearchPattern.createPattern(mainMathodPattern, IJavaSearchConstants.METHOD,
				IJavaSearchConstants.REFERENCES, SearchPattern.R_EXACT_MATCH);

		IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();

		SearchRequestor searchRequestor = createSearchRequestor(matches);

		SearchEngine searchEngine = new SearchEngine();
		searchEngine.search(searchPattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
				searchScope, searchRequestor, null);

		return matches;
	}

	private static SearchRequestor createSearchRequestor(List<SearchMatch> matches) {
		return new SearchRequestor() {

			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				if (match.getAccuracy() == SearchMatch.A_ACCURATE) {
					matches.add(match);
				}
			}
		};
	}
}

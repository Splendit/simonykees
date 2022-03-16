package eu.jsparrow.core.visitor.unused.method;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Holds the relevant information (e.g., did the search process complete
 * successfully, does a method have references to a main sources, the list of
 * test cases that are the only clients of the method) resulting from a method
 * search result.
 * 
 * @since 4.9.0
 */
public class UnusedMethodReferenceSearchResult {

	private boolean mainSourceReferenceFound;
	private boolean invalidSearchResult;
	private List<TestSourceReference> relatedTestDeclarations;

	public UnusedMethodReferenceSearchResult(boolean mainSourceReferenceFound, boolean invalidSearchResult,
			List<TestSourceReference> relatedTestDeclarations) {
		this.mainSourceReferenceFound = mainSourceReferenceFound;
		this.invalidSearchResult = invalidSearchResult;
		this.relatedTestDeclarations = relatedTestDeclarations;
	}

	public boolean isMainSourceReferenceFound() {
		return mainSourceReferenceFound;
	}

	public boolean isInvalidSearchEngineResult() {
		return invalidSearchResult;
	}

	public List<TestSourceReference> getReferencesInTestSources() {
		return relatedTestDeclarations;
	}

	/**
	 * 
	 * @param unusedMethods
	 *            the list of unused methods that are already marked for
	 *            removal.
	 * @return if this unused method has references some test cases that are
	 *         already marked for removal.
	 */
	public boolean hasOverlappingTestDeclarations(List<UnusedMethodWrapper> unusedMethods) {
		for (TestSourceReference testReference : relatedTestDeclarations) {
			Set<MethodDeclaration> testDeclarations = testReference.getTestDeclarations();
			for (UnusedMethodWrapper unusedMethod : unusedMethods) {
				List<TestSourceReference> testsForRemoval = unusedMethod.getTestReferences();
				for (TestSourceReference testForRemoval : testsForRemoval) {
					Set<MethodDeclaration> forRemovalDeclarations = testForRemoval.getTestDeclarations();
					boolean retained = forRemovalDeclarations.stream()
						.anyMatch(testDeclarations::contains);
					if (retained) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(invalidSearchResult, mainSourceReferenceFound, relatedTestDeclarations);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UnusedMethodReferenceSearchResult)) {
			return false;
		}
		UnusedMethodReferenceSearchResult other = (UnusedMethodReferenceSearchResult) obj;
		return invalidSearchResult == other.invalidSearchResult
				&& mainSourceReferenceFound == other.mainSourceReferenceFound
				&& Objects.equals(relatedTestDeclarations, other.relatedTestDeclarations);
	}

	@Override
	public String toString() {
		return String.format(
				"UnusedMethodReferenceSearchResult [mainSourceReferenceFound=%s, invalidSearchResult=%s, relatedTestDeclarations=%s]", //$NON-NLS-1$
				mainSourceReferenceFound, invalidSearchResult, relatedTestDeclarations);
	}

}

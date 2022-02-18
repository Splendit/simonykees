package eu.jsparrow.core.visitor.unused.method;

import java.util.List;
import java.util.Objects;

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

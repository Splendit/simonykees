package eu.jsparrow.core.visitor.unused;

import java.util.List;
import java.util.Objects;

public class UnusedFieldReferenceSearchResult {

	private boolean activeReferenceFound;
	private boolean invalidSearchEngineResult;
	private List<UnusedExternalReferences> unusedReferences;

	public UnusedFieldReferenceSearchResult(boolean activeReferenceFound, boolean invalidSearchEngineResult,
			List<UnusedExternalReferences> unusedReferences) {
		this.activeReferenceFound = activeReferenceFound;
		this.invalidSearchEngineResult = invalidSearchEngineResult;
		this.unusedReferences = unusedReferences;
	}

	public boolean isInvalidSearchEngineResult() {
		return invalidSearchEngineResult;
	}

	public boolean isActiveReferenceFound() {
		return activeReferenceFound;
	}

	public List<UnusedExternalReferences> getUnusedReferences() {
		return unusedReferences;
	}

	@Override
	public int hashCode() {
		return Objects.hash(activeReferenceFound, invalidSearchEngineResult, unusedReferences);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UnusedFieldReferenceSearchResult)) {
			return false;
		}
		UnusedFieldReferenceSearchResult other = (UnusedFieldReferenceSearchResult) obj;
		return activeReferenceFound == other.activeReferenceFound
				&& invalidSearchEngineResult == other.invalidSearchEngineResult
				&& Objects.equals(unusedReferences, other.unusedReferences);
	}

	@Override
	public String toString() {
		return String.format(
				"UnusedFieldReferenceSearchResult [activeReferenceFound=%s, invalidSearchEngineResult=%s, unusedReferences=%s]", //$NON-NLS-1$
				activeReferenceFound, invalidSearchEngineResult, unusedReferences);
	}
}

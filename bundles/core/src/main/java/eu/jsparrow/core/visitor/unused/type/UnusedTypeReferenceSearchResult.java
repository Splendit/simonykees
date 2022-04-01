package eu.jsparrow.core.visitor.unused.type;

import java.util.Objects;

public class UnusedTypeReferenceSearchResult {

	private boolean mainSourceReferenceFound;
	private boolean invalidSearchResult;

	public UnusedTypeReferenceSearchResult(boolean mainSourceReferenceFound, boolean invalidSearchResult) {
		this.mainSourceReferenceFound = mainSourceReferenceFound;
		this.invalidSearchResult = invalidSearchResult;
	}

	public boolean isMainSourceReferenceFound() {
		return mainSourceReferenceFound;
	}

	public boolean isInvalidSearchEngineResult() {
		return invalidSearchResult;
	}

	@Override
	public int hashCode() {
		return Objects.hash(invalidSearchResult, mainSourceReferenceFound);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UnusedTypeReferenceSearchResult)) {
			return false;
		}
		UnusedTypeReferenceSearchResult other = (UnusedTypeReferenceSearchResult) obj;
		return invalidSearchResult == other.invalidSearchResult
				&& mainSourceReferenceFound == other.mainSourceReferenceFound;
	}

	@Override
	public String toString() {
		return String.format(
				"UnusedTypeReferenceSearchResult [mainSourceReferenceFound=%s, invalidSearchResult=%s]", //$NON-NLS-1$
				mainSourceReferenceFound, invalidSearchResult);
	}
}

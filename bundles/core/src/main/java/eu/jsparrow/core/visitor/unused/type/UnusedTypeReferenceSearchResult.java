package eu.jsparrow.core.visitor.unused.type;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UnusedTypeReferenceSearchResult {

	private boolean mainSourceReferenceFound;
	private boolean invalidSearchResult;
	private List<TestReferenceOnType> testReferencesOnType;

	public UnusedTypeReferenceSearchResult(boolean mainSourceReferenceFound, boolean invalidSearchResult) {
		this(mainSourceReferenceFound, invalidSearchResult, Collections.emptyList());
	}

	public UnusedTypeReferenceSearchResult(boolean mainSourceReferenceFound, boolean invalidSearchResult,
			List<TestReferenceOnType> testReferencesOnType) {
		this.mainSourceReferenceFound = mainSourceReferenceFound;
		this.invalidSearchResult = invalidSearchResult;
		this.testReferencesOnType = testReferencesOnType;
	}

	public boolean isMainSourceReferenceFound() {
		return mainSourceReferenceFound;
	}

	public boolean isInvalidSearchEngineResult() {
		return invalidSearchResult;
	}

	public List<TestReferenceOnType> getTestReferencesOnType() {
		return testReferencesOnType;
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

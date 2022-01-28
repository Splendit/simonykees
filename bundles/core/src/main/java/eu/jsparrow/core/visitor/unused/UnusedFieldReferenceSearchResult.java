package eu.jsparrow.core.visitor.unused;

import java.util.List;

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

}

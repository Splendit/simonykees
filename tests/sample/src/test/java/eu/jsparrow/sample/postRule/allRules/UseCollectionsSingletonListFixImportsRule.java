package eu.jsparrow.sample.postRule.allRules;

import java.util.Collections;
import java.util.List;

public class UseCollectionsSingletonListFixImportsRule {

	public void baseCase() {
		final List<String> list = Collections.singletonList("value");
		final List<String> emptyList = Collections.emptyList();
	}

	public void usingStaticImport() {
		final List<String> list = Collections.singletonList("value");
		final List<String> emptyList = Collections.emptyList();
	}
}

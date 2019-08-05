package eu.jsparrow.sample.postRule.allRules;

import java.util.Collections;
import java.util.List;

public class UseCollectionsSingletonListFixImportsRule {

	public void baseCase() {
		List<String> list = Collections.singletonList("value");
		List<String> emptyList = Collections.emptyList();
	}

	public void usingStaticImport() {
		List<String> list = Collections.singletonList("value");
		List<String> emptyList = Collections.emptyList();
	}
}

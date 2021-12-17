package eu.jsparrow.sample.postRule.allRules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChainAssertJAssertThatStatementsAlwaysTransformedRule {

	public void allAssertThatOnOneList() {
		final List<String> stringList = Arrays.asList("String-1", "String-2", "String-3", "String-4");
		assertThat(stringList).isNotNull()
			.isNotEmpty()
			.contains("String-1", atIndex(0))
			.contains("String-2", atIndex(1))
			.contains("String-3", atIndex(2))
			.contains("String-4", atIndex(3))
			.containsAll(Arrays.asList("String-3", "String-4"));
	}

	public void assertThatOnTwoLists() {
		final List<String> stringList = Collections.singletonList("");
		final List<String> stringList2 = Arrays.asList("String-1", "String-2", "String-3", "String-4");
		assertThat(stringList).isNotNull()
			.isNotEmpty();
		assertThat(stringList2).contains("String-1", atIndex(0))
			.contains("String-2", atIndex(1))
			.contains("String-3", atIndex(2))
			.contains("String-4", atIndex(3))
			.containsAll(Arrays.asList("String-3", "String-4"));
	}
}

package eu.jsparrow.sample.preRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import java.util.Arrays;
import java.util.List;

public class ChainAssertJAssertThatStatementsAlwaysTransformedRule {

	public void allAssertThatOnOneList() {
		List<String> stringList = Arrays.asList("String-1", "String-2", "String-3", "String-4");
		assertThat(stringList).isNotNull();
		assertThat(stringList).isNotEmpty();
		assertThat(stringList).contains("String-1", atIndex(0))
			.contains("String-2", atIndex(1))
			.contains("String-3", atIndex(2))
			.contains("String-4", atIndex(3));
		assertThat(stringList).containsAll(Arrays.asList("String-3", "String-4"));
	}

	public void assertThatOnTwoLists() {
		List<String> stringList = Arrays.asList("");
		List<String> stringList2 = Arrays.asList("String-1", "String-2", "String-3", "String-4");
		assertThat(stringList).isNotNull();
		assertThat(stringList).isNotEmpty();
		assertThat(stringList2).contains("String-1", atIndex(0)) //
			.contains("String-2", atIndex(1))
			.contains("String-3", atIndex(2))
			.contains("String-4", atIndex(3));
		assertThat(stringList2).containsAll(Arrays.asList("String-3", "String-4"));
	}
}

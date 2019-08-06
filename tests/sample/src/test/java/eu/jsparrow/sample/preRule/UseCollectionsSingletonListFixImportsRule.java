package eu.jsparrow.sample.preRule;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.List;
import java.util.*;

public class UseCollectionsSingletonListFixImportsRule {
	
	public void baseCase() {
		List<String> list = Arrays.asList("value");
		List<String> emptyList = Arrays.asList();
	}
	
	public void usingStaticImport() {
		List<String> list = asList("value");
		List<String> emptyList = asList();
	}
}

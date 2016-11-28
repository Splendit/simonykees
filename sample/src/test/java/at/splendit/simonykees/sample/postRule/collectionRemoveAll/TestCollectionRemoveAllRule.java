package at.splendit.simonykees.sample.postRule.collectionRemoveAll;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("nls")
public class TestCollectionRemoveAllRule {
	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";")); //$NON-NLS-1$
	}

	public String testIfCollectionIsEmpty(String input){
		List<String> resultList = generateList(input);
		
		resultList.removeAll(resultList);
		
		StringBuilder sb = new StringBuilder();
		
		resultList.stream().forEach((s)->sb.append(s));
		
		return sb.toString();
	}
}

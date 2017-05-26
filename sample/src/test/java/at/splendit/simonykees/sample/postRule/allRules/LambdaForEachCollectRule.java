package at.splendit.simonykees.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings({ "nls" })
public class LambdaForEachCollectRule {

	public String convertForEachToCollect(String input) {
		List<String> objectList = new ArrayList<>();

		List<String> oStrings = objectList.stream().map(o -> StringUtils.substring(o, 0)).collect(Collectors.toList());

		return oStrings.stream().collect(Collectors.joining(","));
	}

	public String convertForEachExpressionToCollect(String input) {
		List<String> objectList = new ArrayList<>();

		List<String> oStrings = objectList.stream().map(o -> o).collect(Collectors.toList());

		return oStrings.stream().collect(Collectors.joining(","));
	}

	public String nonEmptyCollection(String input) {
		List<String> oStrings = new ArrayList<>();
		List<String> objectList = new ArrayList<>();

		oStrings.add("");
		objectList.stream().map(o -> StringUtils.substring(o, 0)).forEach(oStrings::add);

		return oStrings.stream().collect(Collectors.joining(","));
	}

	public String addAfterForEach(String input) {
		List<String> objectList = new ArrayList<>();

		List<String> oStrings = objectList.stream().map(o -> StringUtils.substring(o, 0)).collect(Collectors.toList());

		oStrings.add("-");
		return oStrings.stream().collect(Collectors.joining(","));
	}

	public String blockBody(String input) {
		List<String> objectList = new ArrayList<>();

		List<String> oStrings = objectList.stream().map(o -> StringUtils.substring(o, 0)).collect(Collectors.toList());

		return oStrings.stream().collect(Collectors.joining(","));
	}

	public String multipleBodyStatements(String input) {
		List<String> oStrings = new ArrayList<>();
		List<String> objectList = new ArrayList<>();

		objectList.stream().map(o -> StringUtils.substring(o, 0)).forEach((String oString) -> {
			oStrings.add(oString);
			if (StringUtils.isEmpty(oString)) {
				oStrings.add(input);
			}
		});

		return oStrings.stream().collect(Collectors.joining(","));
	}

	public String referencingOtherLocalVars(String input) {
		List<String> objectList = new ArrayList<>();
		String s = "";
		List<String> oStrings = objectList.stream().filter(oString -> oString.equals(s))
				.map(o -> StringUtils.substring(o, 0)).collect(Collectors.toList());

		return oStrings.stream().collect(Collectors.joining(","));
	}
}

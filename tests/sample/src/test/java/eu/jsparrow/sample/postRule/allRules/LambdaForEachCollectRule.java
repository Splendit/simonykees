package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings({ "nls", "unused" })
public class LambdaForEachCollectRule {

	List<String> listField = new ArrayList<>();

	public String convertForEachToCollect(String input) {
		final List<String> oStrings = new ArrayList<>();
		final List<String> objectList = new ArrayList<>();

		/* lambda param comment */
		/* lambda body */
		// save me
		// comment after oStrings
		oStrings.addAll(objectList.stream()
			.map(o -> StringUtils.substring(o, 0)) // comment after map
			.collect(Collectors.toList()));

		return oStrings.stream()
			.collect(Collectors.joining(","));
	}

	public String convertForEachExpressionToCollect(String input) {
		final List<String> oStrings = new ArrayList<>();
		final List<String> objectList = new ArrayList<>();

		oStrings.addAll(objectList.stream()
			.map(o -> o)
			.collect(Collectors.toList()));

		return oStrings.stream()
			.collect(Collectors.joining(","));
	}

	public String multiDeclarationFragment(String input) {
		final List<String> oStrings = new ArrayList<>();
		final List<String> ostrings2;
		final List<String> ostrings3 = new ArrayList<>();
		final List<String> objectList = new ArrayList<>();

		oStrings.addAll(objectList.stream()
			.map(o -> StringUtils.substring(o, 0))
			.collect(Collectors.toList()));

		return oStrings.stream()
			.collect(Collectors.joining(","));
	}

	public String nonEmptyCollection(String input) {
		final List<String> oStrings = new ArrayList<>();
		final List<String> objectList = new ArrayList<>();

		oStrings.add("");
		oStrings.addAll(objectList.stream()
			.map(o -> StringUtils.substring(o, 0))
			.collect(Collectors.toList()));

		return oStrings.stream()
			.collect(Collectors.joining(","));
	}

	public String addAfterForEach(String input) {
		final List<String> oStrings = new ArrayList<>();
		final List<String> objectList = new ArrayList<>();

		oStrings.addAll(objectList.stream()
			.map(o -> StringUtils.substring(o, 0))
			.collect(Collectors.toList()));

		oStrings.add("-");
		return oStrings.stream()
			.collect(Collectors.joining(","));
	}

	public String blockBody(String input) {
		final List<String> oStrings = new ArrayList<>();
		final List<String> objectList = new ArrayList<>();

		oStrings.addAll(objectList.stream()
			.map(o -> StringUtils.substring(o, 0))
			.collect(Collectors.toList()));

		return oStrings.stream()
			.collect(Collectors.joining(","));
	}

	public String multipleBodyStatements(String input) {
		final List<String> oStrings = new ArrayList<>();
		final List<String> objectList = new ArrayList<>();

		objectList.stream()
			.map(o -> StringUtils.substring(o, 0))
			.forEach((String oString) -> {
				oStrings.add(oString);
				if (StringUtils.isEmpty(oString)) {
					oStrings.add(input);
				}
			});

		return oStrings.stream()
			.collect(Collectors.joining(","));
	}

	public String referencingOtherLocalVars(String input) {
		final List<String> oStrings = new ArrayList<>();
		final List<String> objectList = new ArrayList<>();
		final String s = "";
		oStrings.addAll(objectList.stream()
			.filter(oString -> oString.equals(s))
			.map(o -> StringUtils.substring(o, 0))
			.collect(Collectors.toList()));

		return oStrings.stream()
			.collect(Collectors.joining(","));
	}

	public String nestedBlocks(String input) {
		final List<String> oStrings = new ArrayList<>();
		{
			final List<String> objectList = new ArrayList<>();
			final String s = "";
			if (StringUtils.isEmpty(s)) {
				oStrings.addAll(objectList.stream()
					.filter(oString -> oString.equals(s))
					.map(o -> StringUtils.substring(o, 0))
					.collect(Collectors.toList()));
			}
		}

		return oStrings.stream()
			.collect(Collectors.joining(","));
	}

	public String addingToFieldMethod(String input) {
		final List<String> objectList = new ArrayList<>();
		final String s = "";
		listField.addAll(objectList.stream()
			.filter(oString -> oString.equals(s))
			.map(o -> StringUtils.substring(o, 0))
			.collect(Collectors.toList()));

		return listField.stream()
			.collect(Collectors.joining(","));
	}

	public String thisAddInvocation(String input) {
		final List<String> objectList = new ArrayList<>();
		final String s = "";
		objectList.stream()
			.filter(oString -> oString.equals(s))
			.map(o -> StringUtils.substring(o, 0))
			.forEach(this::add);

		return listField.stream()
			.collect(Collectors.joining(","));
	}

	private void add(String string) {
		listField.add(string);
	}

	public String collectRawList(String input) {
		final List raw = Collections.singletonList(input);
		final List<Object> typedList = new ArrayList<>();
		raw.stream()
			.forEach(typedList::add);

		return typedList.stream()
			.map(Object::toString)
			.collect(Collectors.joining());
	}
}

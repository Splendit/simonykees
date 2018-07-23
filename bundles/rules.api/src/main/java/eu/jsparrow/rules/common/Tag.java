package eu.jsparrow.rules.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tag object for filtering rules
 * 
 * @author Martin Huter, Andreja Sambolec
 * @since 1.2
 *
 */
@SuppressWarnings("nls")
public enum Tag {

	LOOP("loop"),
	JAVA_0_9("0.9"),
	JAVA_1_1("1.1", "1"),
	JAVA_1_2("1.2", "2"),
	JAVA_1_3("1.3", "3"),
	JAVA_1_4("1.4", "4"),
	JAVA_1_5("1.5", "5"),
	JAVA_1_6("1.6", "6"),
	JAVA_1_7("1.7", "7"),
	JAVA_1_8("1.8", "8"),
	JAVA_1_10("1.10", "10"),
	// not yet arrived! JAVA_1_9("1.9","9"),
	EMPTY(),
	STRING_MANIPULATION("string-manipulation"),
	FORMATTING("format", "organize"),
	CODING_CONVENTIONS("conventions"),
	PERFORMANCE("performance"),
	READABILITY("readability"),
	OLD_LANGUAGE_CONSTRUCTS("outdated constructs"),
	LAMBDA("lambda");

	private List<String> tagName;

	private Tag(String... tagName) {
		this.tagName = Arrays.asList(tagName);
	}

	public List<String> getTagNames() {
		return tagName;
	}

	public static Tag getTageForName(String name) {
		return Arrays.stream(values())
			.filter(tag -> tag.getTagNames()
				.contains(name))
			.findFirst()
			.orElse(null);
	}

	public static String[] getAllTags() {
		return Arrays.stream(values())
			.map(Tag::getTagNames)
			.flatMap(List::stream)
			.collect(Collectors.toList())
			.stream()
			.toArray(String[]::new);
	}

}

package eu.jsparrow.rules.java16;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to determine an optional escaped value for a
 * {@link org.eclipse.jdt.core.dom.TextBlock}.
 * <p>
 * This class is a proposal in connection with several corner cses and is tested
 * but not used yet.
 *
 */
public class TextBlockContentAnalyzer {
	private static final Pattern PATTERN_LINE_SEPARATORS = Pattern.compile("([\r][\n]?)|([\n])"); //$NON-NLS-1$
	private static final int MINIMAL_LINE_COUNT = 4;
	private static final String SYSTEM_LS = System.lineSeparator();
	private static final String TEXT_BLOCK_TRIPLE_QUOTES = "\"\"\""; //$NON-NLS-1$

	public static Optional<String> findValidEscapedValue(String text) {

		Matcher matcher = PATTERN_LINE_SEPARATORS.matcher(text);

		while (matcher.find()) {

			String lineSeparatorFound = matcher.group();
			if (!SYSTEM_LS.equals(lineSeparatorFound)) {
				return Optional.empty();
			}
		}

		String[] lines = PATTERN_LINE_SEPARATORS.split(text);
		
		for(String line : lines) {
			
			int length = line.length();
			if(length > 0) {
				char lastCharacter = line.charAt(length - 1);
				if(Character.isWhitespace(lastCharacter)) {
					return Optional.empty();
				}
			}
		}

		if (lines.length < MINIMAL_LINE_COUNT) {
			return Optional.empty();
		}

		StringBuilder sbEscapedValue = new StringBuilder();
		sbEscapedValue.append(TEXT_BLOCK_TRIPLE_QUOTES);
		sbEscapedValue.append(SYSTEM_LS);
		sbEscapedValue.append(text);
		if (!text.endsWith(SYSTEM_LS)) {
			sbEscapedValue.append('\\');
			sbEscapedValue.append(SYSTEM_LS);
		}

		sbEscapedValue.append(TEXT_BLOCK_TRIPLE_QUOTES);
		return Optional.of(sbEscapedValue.toString());

	}

	private TextBlockContentAnalyzer() {

	}

}

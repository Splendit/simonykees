package eu.jsparrow.rules.java16;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.TextBlock;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.java16.textblock.ConcatenationComponentsCollector;

/**
 * 
 * This visitor looks for String concatenation expressions which can be
 * transformed to Text Block literals introduced in Java 15.
 * 
 * Example:
 * 
 * <pre>
 * String html = "" +
 * 		"              &lt;html&gt;\n" +
 * 		"                  &lt;body&gt;\n" +
 * 		"                      &lt;p&gt;Hello, world&lt;/p&gt;\n" +
 * 		"                  &lt;/body&gt;\n" +
 * 		"              &lt;/html&gt;\n";
 * </pre>
 * 
 * is transformed to
 * 
 * <pre>
 *	String html = """
 *		      &lt;html&gt;
 *		          &lt;body&gt;
 *		              &lt;p&gt;Hello, world&lt;/p&gt;
 *		          &lt;/body&gt;
 *		      &lt;/html&gt;
 *	""";
 * </pre>
 * 
 * 
 * @since 4.3.0
 * 
 */
public class UseTextBlockASTVisitor extends AbstractASTRewriteASTVisitor {

	static final String TEXT_BLOCK_TRIPLE_QUOTES = "\"\"\""; //$NON-NLS-1$
	static final String ESCAPE_TEXTBLOCK_TRIPLE_QUOTES = "\"\"\\\""; //$NON-NLS-1$
	private static final Pattern PATTERN_LINE_SEPARATORS = Pattern.compile("([\r][\n]?)|([\n])"); //$NON-NLS-1$
	private static final int MINIMAL_LINE_COUNT = 3;
	private static final String SYSTEM_LS = System.lineSeparator();

	@Override
	public boolean visit(InfixExpression infixExpresssion) {

		findValidEscapedValueForTextBlock(infixExpresssion).ifPresent(escapedValue -> {
			TextBlock textBlock = astRewrite.getAST()
				.newTextBlock();
			
			textBlock.setEscapedValue(escapedValue);

			astRewrite.replace(infixExpresssion, textBlock, null);
			onRewrite();
		});
		return false;
	}

	private Optional<String> findValidEscapedValueForTextBlock(InfixExpression infixExpresssion) {

		List<String> components = new ConcatenationComponentsCollector()
			.collectConcatenationComponents(infixExpresssion);
		if (components.size() < 3) {
			return Optional.empty();
		}

		String text = String.join("", components); //$NON-NLS-1$

		if (text.trim()
			.isEmpty()) {
			return Optional.empty();
		}

		if (text.endsWith(TEXT_BLOCK_TRIPLE_QUOTES)) {
			return Optional.empty();
		}

		Matcher matcher = PATTERN_LINE_SEPARATORS.matcher(text);

		int lineCount = 0;
		while (matcher.find()) {
			lineCount++;
		}

		if (lineCount < MINIMAL_LINE_COUNT) {
			return Optional.empty();
		}

		String[] lines = PATTERN_LINE_SEPARATORS.split(text);

		for (String line : lines) {
			int length = line.length();
			if (length > 0) {
				char lastCharacter = line.charAt(length - 1);
				if (Character.isWhitespace(lastCharacter)) {
					return Optional.empty();
				}
			}
		}

		text = text.replace(TEXT_BLOCK_TRIPLE_QUOTES, ESCAPE_TEXTBLOCK_TRIPLE_QUOTES);

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
}

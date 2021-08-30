package eu.jsparrow.rules.java16;

import java.util.List;

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

	/**
	 * TODO either move both {@link #TEXT_BLOCK_TRIPLE_QUOTES } and
	 * {@link #ESCAPE_TEXTBLOCK_TRIPLE_QUOTES } to
	 * {@link TextBlockContentAnalyzer} as soon as
	 * {@link TextBlockContentAnalyzer} is used or move all functionalities of
	 * {@link TextBlockContentAnalyzer} to {@link UseTextBlockASTVisitor} and
	 * make the constants private.
	 */
	static final String TEXT_BLOCK_TRIPLE_QUOTES = "\"\"\""; //$NON-NLS-1$
	static final String ESCAPE_TEXTBLOCK_TRIPLE_QUOTES = "\"\"\\\""; //$NON-NLS-1$

	@Override
	public boolean visit(InfixExpression infixExpresssion) {

		ConcatenationComponentsCollector componentsCollector = new ConcatenationComponentsCollector();

		List<String> components = componentsCollector.collectConcatenationComponents(infixExpresssion);
		if (!components.isEmpty()) {

			String escapedValue = createEscapedValue(components);

			TextBlock textBlock = astRewrite.getAST()
				.newTextBlock();

			textBlock.setEscapedValue(escapedValue);

			astRewrite.replace(infixExpresssion, textBlock, null);
			onRewrite();
		}
		return false;
	}

	private String createEscapedValue(List<String> components) {

		String content = String.join("", components); //$NON-NLS-1$
		content = content.replace(TEXT_BLOCK_TRIPLE_QUOTES, ESCAPE_TEXTBLOCK_TRIPLE_QUOTES);
		
		String lineSeparator = "\n"; //$NON-NLS-1$
		if (!content.endsWith(lineSeparator)) {
			content = content + '\\' + lineSeparator;
		}

		return TEXT_BLOCK_TRIPLE_QUOTES + lineSeparator +
				content +
				TEXT_BLOCK_TRIPLE_QUOTES;
	}
}

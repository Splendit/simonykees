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

	private static final String TEXT_BLOCK_TRIPLE_QUOTES = "\"\"\""; //$NON-NLS-1$

	/**
	 * Prototype without any validation.
	 */
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

		StringBuilder sbContent = new StringBuilder();

		components.forEach(sbContent::append);

		int lastComponentIndex = components.size() - 1;
		String lastComponent = components.get(lastComponentIndex);
		if (!lastComponent.endsWith("\n")) { //$NON-NLS-1$
			sbContent.append('\\');
			sbContent.append('\n');
		}

		return TEXT_BLOCK_TRIPLE_QUOTES + '\n' +
				sbContent.toString() +
				TEXT_BLOCK_TRIPLE_QUOTES;
	}
}

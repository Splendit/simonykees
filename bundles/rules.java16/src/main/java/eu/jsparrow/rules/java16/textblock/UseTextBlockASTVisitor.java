package eu.jsparrow.rules.java16.textblock;

import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TextBlock;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

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
		StringConcatenationComponentsStore componentStore = new StringConcatenationComponentsStore();

		componentStore.storeComponents(infixExpresssion);

		StringBuilder sb = new StringBuilder();
		sb.append(TEXT_BLOCK_TRIPLE_QUOTES);
		sb.append('\n');
		componentStore.getComponents()
			.stream()
			.filter(StringLiteral.class::isInstance)
			.map(StringLiteral.class::cast)
			.map(StringLiteral::getLiteralValue)
			.forEach(sb::append);
		sb.append(TEXT_BLOCK_TRIPLE_QUOTES);

		TextBlock textBlock = astRewrite.getAST()
			.newTextBlock();
		String escapedValue = sb.toString();

		textBlock.setEscapedValue(escapedValue);

		astRewrite.replace(infixExpresssion, textBlock, null);
		onRewrite();
		return false;
	}
}

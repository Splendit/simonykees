package eu.jsparrow.rules.java16.textblock;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
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

		List<Expression> components = componentStore.collectComponents(infixExpresssion);
		String textBlockContent = findValidTextBlockContent(components).orElse(null);

		if (textBlockContent == null) {
			return true;
		}

		TextBlock textBlock = astRewrite.getAST()
			.newTextBlock();

		textBlock.setEscapedValue(textBlockContent);

		astRewrite.replace(infixExpresssion, textBlock, null);
		onRewrite();
		return false;
	}

	private Optional<String> findValidTextBlockContent(List<Expression> components) {

		StringBuilder sb = new StringBuilder();
		sb.append(TEXT_BLOCK_TRIPLE_QUOTES);
		sb.append('\n');

		for (Expression component : components) {
			if (component.getNodeType() == ASTNode.STRING_LITERAL) {
				StringLiteral stringLiteral = (StringLiteral) component;
				sb.append(stringLiteral.getLiteralValue());
			} else if (component.getNodeType() == ASTNode.NUMBER_LITERAL) {
				NumberLiteral numberLiteral = (NumberLiteral) component;
				sb.append(numberLiteral.getToken());
			} else if (component.getNodeType() == ASTNode.CHARACTER_LITERAL) {
				CharacterLiteral characterLiteral = (CharacterLiteral) component;
				sb.append(characterLiteral.charValue());
			} else if (component.getNodeType() == ASTNode.BOOLEAN_LITERAL) {
				BooleanLiteral booleanLiteral = (BooleanLiteral) component;
				sb.append(booleanLiteral.booleanValue());
			} else if (component.getNodeType() == ASTNode.NULL_LITERAL) {
				sb.append("null"); //$NON-NLS-1$
			}
		}

		sb.append(TEXT_BLOCK_TRIPLE_QUOTES);
		return Optional.of(sb.toString());
	}

}

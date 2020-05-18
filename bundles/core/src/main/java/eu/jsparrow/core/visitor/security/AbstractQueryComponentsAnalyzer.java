package eu.jsparrow.core.visitor.security;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * Intended to be extended by classes which analyze the components of dynamic
 * queries.
 * 
 * @since 3.17.0
 *
 */
public abstract class AbstractQueryComponentsAnalyzer {

	protected List<Expression> components;

	AbstractQueryComponentsAnalyzer(List<Expression> components) {
		this.components = components;
	}

	/**
	 * @return A StringLiteral starting with {@code "'"} if such a StringLiteralit is
	 *         the component following the component specified by the index,
	 *         otherwise {@code null}.
	 */
	protected StringLiteral findNext(int index) {
		int nextIndex = index + 1;
		if (components.size() <= nextIndex) {
			return null;
		}
		Expression next = components.get(nextIndex);
		if (next.getNodeType() != ASTNode.STRING_LITERAL) {
			return null;
		}
		StringLiteral literal = (StringLiteral) next;
		String value = literal.getLiteralValue();
		return value.startsWith("'") ? literal : null; //$NON-NLS-1$
	}

	/**
	 * @return A StringLiteral ending with {@code "'"} if such a StringLiteralit is
	 *         the component preceding the component specified by the index,
	 *         otherwise {@code null}.
	 */
	protected StringLiteral findPrevious(int index) {
		int previousIndex = index - 1;
		if (previousIndex < 0) {
			return null;
		}
		Expression previous = components.get(previousIndex);
		if (previous.getNodeType() != ASTNode.STRING_LITERAL) {
			return null;
		}
		StringLiteral stringLiteral = (StringLiteral) previous;
		String value = stringLiteral.getLiteralValue();
		return value.endsWith("'") ? stringLiteral : null; //$NON-NLS-1$
	}

}

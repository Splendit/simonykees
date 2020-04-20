package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * Finds the components of a dynamic query that can be escaped.
 * 
 * @since 3.16.0
 *
 */
public class QueryComponentsAnalyzerForEscaping {

	private List<Expression> components;
	private List<Expression> expressionsToEscape = new ArrayList<>();

	public QueryComponentsAnalyzerForEscaping(List<Expression> components) {
		this.components = components;
	}

	/**
	 * Constructs a list of {@link ReplaceableParameter}s out of the
	 * {@link #components} of the query.
	 * 
	 * @return
	 */
	public void analyze() {
		List<Expression> nonLiteralComponents = components.stream()
			.filter(component -> component.getNodeType() != ASTNode.STRING_LITERAL)
			.collect(Collectors.toList());

		for (Expression component : nonLiteralComponents) {
			int index = components.indexOf(component);
			StringLiteral previous = findPrevious(index);
			if (previous != null) {
				StringLiteral next = findNext(index);
				if (next != null) {
					this.expressionsToEscape.add(component);
				}
			}
		}
	}

	// the same as in QueryComponentsAnalyzer
	private StringLiteral findNext(int index) {
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

	// the same as in QueryComponentsAnalyzer
	private StringLiteral findPrevious(int index) {
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

	/**
	 * @return the list of {@link Expression}s constructed by
	 *         {@link #analyze()}.
	 */
	public List<Expression> getExpressionsToEscape() {
		return this.expressionsToEscape;
	}

}

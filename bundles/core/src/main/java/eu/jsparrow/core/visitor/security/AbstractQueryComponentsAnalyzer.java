package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

	protected static final String SIMPLE_QUOTATION_MARK = "'"; //$NON-NLS-1$

	protected List<Expression> components;
	private int indexOffset;

	AbstractQueryComponentsAnalyzer(List<Expression> components, int indexOffset) {
		this.components = components;
		this.indexOffset = indexOffset;
	}
	
	AbstractQueryComponentsAnalyzer(List<Expression> components) {
		this(components, 1);
	}

	/**
	 * @return true if the given {@link StringLiteral} starts with {@code "'"},
	 *         otherwise false.
	 */
	protected boolean isValidNext(StringLiteral literal) {
		return literal.getLiteralValue()
			.startsWith(SIMPLE_QUOTATION_MARK);
	}

	/**
	 * @return true if the given {@link StringLiteral} ends with {@code "'"},
	 *         otherwise false.
	 */
	protected boolean isValidPrevious(StringLiteral literal) {
		return literal.getLiteralValue()
			.endsWith(SIMPLE_QUOTATION_MARK);
	}

	/**
	 * @return A {@link StringLiteral} following the component at the specified
	 *         index and fulfilling the conditions checked by
	 *         {@link #isValidNext(StringLiteral)}, or null if no such
	 *         {@link StringLiteral} can be found.
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
		return isValidNext(literal) ? literal : null;
	}

	/**
	 * @return A {@link StringLiteral} preceding the component at the specified
	 *         index and fulfilling the conditions checked by
	 *         {@link #isValidPrevious(StringLiteral)}, or null if no such
	 *         {@link StringLiteral} can be found.
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
		StringLiteral literal = (StringLiteral) previous;
		return isValidPrevious(literal) ? literal : null;
	}

	/**
	 * Constructs a list of {@link ReplaceableParameter}s out of the
	 * {@link #components} of the query.
	 * 
	 * @return
	 */
	public List<ReplaceableParameter> createReplaceableParameterList() {
		List<Expression> nonLiteralComponents = components.stream()
			.filter(component1 -> component1.getNodeType() != ASTNode.STRING_LITERAL)
			.collect(Collectors.toList());

		List<ReplaceableParameter> parameters = new ArrayList<>();

		int parameterPosition = indexOffset;
		for (Expression component : nonLiteralComponents) {
			int componentIndex = components.indexOf(component);
			ReplaceableParameter parameter = createReplaceableParameter(componentIndex, parameterPosition);
			if (parameter != null) {
				parameters.add(parameter);
				parameterPosition++;
			}
		}
		return parameters;
	}

	protected abstract ReplaceableParameter createReplaceableParameter(int componentIndex, int parameterPosition);

}

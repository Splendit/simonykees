package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * Finds the components of a dynamic query that can be replaced with parameters
 * of a prepared statement. Additionally, computes which setter method should be
 * used for the corresponding parameter.
 * 
 * @since 3.16.0
 *
 */
public class JPAQueryComponentsAnalyzer extends AbstractQueryComponentsAnalyzer {

	private static final String SETTER_NAME = "setParameter"; //$NON-NLS-1$

	private List<ReplaceableParameter> parameters = new ArrayList<>();

	private final int whereKeywordPosition;

	JPAQueryComponentsAnalyzer(List<Expression> components) {
		super(components);
		whereKeywordPosition = findWhereKeywordPosition();
	}

	/**
	 * Constructs a list of {@link ReplaceableParameter}s out of the
	 * {@link #components} of the query.
	 * 
	 * @return
	 */
	public void analyze() {
		if (whereKeywordPosition < 0) {
			return;
		}
		List<Expression> nonLiteralComponents = collectNonLiteralComponents();
		int position = 1;
		for (Expression component : nonLiteralComponents) {
			int index = components.indexOf(component);
			if (index > whereKeywordPosition) {
				StringLiteral previous = findPrevious(index);
				if (previous != null) {
					StringLiteral next = findNext(index);
					if (next != null || index == components.size() - 1) {
						this.parameters
							.add(new ReplaceableParameter(previous, next, component, SETTER_NAME, position));
						position++;
					}
				}
			}
		}
	}

	private int findWhereKeywordPosition() {
		// find last String literal containing WHERE keyword
		for (int i = components.size() - 1; i >= 0; i--) {
			Expression expression = components.get(i);
			if (expression.getNodeType() == ASTNode.STRING_LITERAL) {
				StringLiteral stringLiteral = (StringLiteral) expression;
				String value = stringLiteral.getLiteralValue()
					.replace('\t', ' ')
					.replace('\n', ' ')
					.replace('\r', ' ')
					.toUpperCase();
				if (value.contains(" WHERE ")) { //$NON-NLS-1$
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * @return true if the given {@link StringLiteral} does not start with
	 *         {@code "'"}, otherwise false.
	 */
	@Override
	protected boolean isValidNext(StringLiteral literal) {
		return !literal.getLiteralValue()
			.trim()
			.startsWith(SIMPLE_QUOTATION_MARK);
	}

	/**
	 * @return true if the given {@link StringLiteral} ends with {@code "="} and
	 *         optional subsequent white spaces, otherwise false.
	 */
	@Override
	protected boolean isValidPrevious(StringLiteral literal) {
		return literal.getLiteralValue()
			.trim()
			.endsWith("="); //$NON-NLS-1$
	}

	/**
	 * @return the list of {@link ReplaceableParameter}s constructed by
	 *         {@link #analyze()}.
	 */
	public List<ReplaceableParameter> getReplaceableParameters() {
		return this.parameters;
	}

}

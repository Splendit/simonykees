package eu.jsparrow.core.visitor.security;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * Finds the components of a dynamic query that can be replaced with parameters
 * of a prepared statement. Additionally, computes which setter method should be
 * used for the corresponding parameter.
 * 
 * @since 3.16.0
 *
 */
public class QueryComponentsAnalyzer {

	private List<Expression> components;
	private List<ReplaceableParameter> parameters = new ArrayList<>();

	public QueryComponentsAnalyzer(List<Expression> components) {
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

		int position = 1;
		for (Expression component : nonLiteralComponents) {
			int index = components.indexOf(component);
			StringLiteral previous = findPrevious(index);
			if (previous != null) {
				StringLiteral next = findNext(index);
				if (next != null) {
					String setterName = findSetterName(component);
					if (setterName != null) {
						this.parameters.add(new ReplaceableParameter(previous, next, component, setterName, position));
						position++;
					}
				}
			}
		}
	}

	@SuppressWarnings("nls")
	private String findSetterName(Expression component) {
		ITypeBinding type = component.resolveTypeBinding();

		if (isContentOfType(type, java.sql.Array.class.getName())) {
			return "setArray";
		} else if (isContentOfType(type, java.math.BigDecimal.class.getName())) {
			return "setBigDecimal";
		} else if (isContentOfType(type, java.sql.Blob.class.getName())) {
			return "setBlob";
		} else if (isContentOfType(type, java.sql.Clob.class.getName())) {
			return "setClob";
		} else if (isContentOfType(type, java.sql.Date.class.getName())) {
			return "setDate";
		} else if (isContentOfType(type, java.lang.Object.class.getName())) {
			return "setObject";
		} else if (isContentOfType(type, java.sql.Ref.class.getName())) {
			return "setRef";
		} else if (isContentOfType(type, java.lang.String.class.getName())) {
			return "setString";
		} else if (isContentOfType(type, java.sql.Time.class.getName())) {
			return "setTime";
		} else if (isContentOfType(type, java.sql.Timestamp.class.getName())) {
			return "setTimestamp";
		} else if (isContentOfType(type, java.net.URL.class.getName())) {
			return "setURL";
		} else if (type.isArray() && "byte".equals(type.getComponentType()
			.getName())) {
			return "setBytes";
		} else if ("boolean".equals(type.getName()) || isContentOfType(type, java.lang.Boolean.class.getName())) {
			return "setBoolean";
		} else if ("byte".equals(type.getName()) || isContentOfType(type, java.lang.Byte.class.getName())) {
			return "setByte";
		} else if ("double".equals(type.getName()) || isContentOfType(type, java.lang.Double.class.getName())) {
			return "setDouble";
		} else if ("float".equals(type.getName()) || isContentOfType(type, java.lang.Float.class.getName())) {
			return "setFloat";
		} else if ("int".equals(type.getName()) || isContentOfType(type, java.lang.Integer.class.getName())) {
			return "setInt";
		} else if ("long".equals(type.getName()) || isContentOfType(type, java.lang.Long.class.getName())) {
			return "setLong";
		} else if ("short".equals(type.getName()) || isContentOfType(type, java.lang.Short.class.getName())) {
			return "setShort";
		}
		return null;
	}

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
	 * @return the list of {@link ReplaceableParameter}s constructed by {@link #analyze()}.
	 */
	public List<ReplaceableParameter> getReplaceableParameters() {
		return this.parameters;
	}

}

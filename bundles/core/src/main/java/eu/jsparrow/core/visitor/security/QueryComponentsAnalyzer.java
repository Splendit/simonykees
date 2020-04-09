package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		String key = computeSetterKey(type);
		Map<String, String> settersMap = new HashMap<>();
		settersMap.put(java.sql.Array.class.getName(), "setArray");
		settersMap.put(java.math.BigDecimal.class.getName(), "setBigDecimal");
		settersMap.put(java.sql.Blob.class.getName(), "setBlob");
		settersMap.put(java.sql.Clob.class.getName(), "setClob");
		settersMap.put(java.sql.Date.class.getName(), "setDate");
		settersMap.put(java.lang.Object.class.getName(), "setObject");
		settersMap.put(java.sql.Ref.class.getName(), "setRef");
		settersMap.put(java.lang.String.class.getName(), "setString");
		settersMap.put(java.sql.Time.class.getName(), "setTime");
		settersMap.put(java.sql.Timestamp.class.getName(), "setTimestamp");
		settersMap.put(java.net.URL.class.getName(), "setURL");

		settersMap.put("bytes", "setBytes");

		settersMap.put("boolean", "setBoolean");
		settersMap.put(java.lang.Boolean.class.getName(), "setBoolean");
		settersMap.put("byte", "setByte");
		settersMap.put(java.lang.Byte.class.getName(), "setByte");
		settersMap.put("double", "setDouble");
		settersMap.put(java.lang.Double.class.getName(), "setDouble");
		settersMap.put("float", "setFloat");
		settersMap.put(java.lang.Float.class.getName(), "setFloat");
		settersMap.put("int", "setInt");
		settersMap.put(java.lang.Integer.class.getName(), "setInt");
		settersMap.put("long", "setLong");
		settersMap.put(java.lang.Long.class.getName(), "setLong");
		settersMap.put("short", "setShort");
		settersMap.put(java.lang.Short.class.getName(), "setShort");

		return settersMap.get(key);
	}

	private String computeSetterKey(ITypeBinding type) {
		if (type.isPrimitive()) {
			return type.getName();
		}

		if (type.isArray() && "byte".equals(type.getComponentType() //$NON-NLS-1$
			.getName())) {
			return "bytes"; //$NON-NLS-1$
		}
		return type.getErasure()
			.getQualifiedName();
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
	 * @return the list of {@link ReplaceableParameter}s constructed by
	 *         {@link #analyze()}.
	 */
	public List<ReplaceableParameter> getReplaceableParameters() {
		return this.parameters;
	}

}

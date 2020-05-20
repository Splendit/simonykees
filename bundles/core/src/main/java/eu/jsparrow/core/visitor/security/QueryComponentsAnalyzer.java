package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.Collections;
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
public class QueryComponentsAnalyzer extends AbstractQueryComponentsAnalyzer {


	private List<ReplaceableParameter> parameters = new ArrayList<>();

	@SuppressWarnings("nls")
	private static final Map<String, String> SETTERS_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {
		private static final long serialVersionUID = 572293158475249398L;
		{
			put(java.sql.Array.class.getName(), "setArray");
			put(java.math.BigDecimal.class.getName(), "setBigDecimal");
			put(java.sql.Blob.class.getName(), "setBlob");
			put(java.sql.Clob.class.getName(), "setClob");
			put(java.sql.Date.class.getName(), "setDate");
			put(java.lang.Object.class.getName(), "setObject");
			put(java.sql.Ref.class.getName(), "setRef");
			put(java.lang.String.class.getName(), "setString");
			put(java.sql.Time.class.getName(), "setTime");
			put(java.sql.Timestamp.class.getName(), "setTimestamp");
			put(java.net.URL.class.getName(), "setURL");

			put("bytes", "setBytes");

			put("boolean", "setBoolean");
			put(java.lang.Boolean.class.getName(), "setBoolean");
			put("byte", "setByte");
			put(java.lang.Byte.class.getName(), "setByte");
			put("double", "setDouble");
			put(java.lang.Double.class.getName(), "setDouble");
			put("float", "setFloat");
			put(java.lang.Float.class.getName(), "setFloat");
			put("int", "setInt");
			put(java.lang.Integer.class.getName(), "setInt");
			put("long", "setLong");
			put(java.lang.Long.class.getName(), "setLong");
			put("short", "setShort");
			put(java.lang.Short.class.getName(), "setShort");
		}
	});

	QueryComponentsAnalyzer(List<Expression> components) {
		super(components);
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

	private String findSetterName(Expression component) {
		ITypeBinding type = component.resolveTypeBinding();
		String key = computeSetterKey(type);
		return SETTERS_MAP.get(key);
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

	/**
	 * @return the list of {@link ReplaceableParameter}s constructed by
	 *         {@link #analyze()}.
	 */
	public List<ReplaceableParameter> getReplaceableParameters() {
		return this.parameters;
	}

}

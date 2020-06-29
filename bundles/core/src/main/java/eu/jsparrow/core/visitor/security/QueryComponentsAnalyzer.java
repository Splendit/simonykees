package eu.jsparrow.core.visitor.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	@Override
	protected ReplaceableParameter createReplaceableParameter(int componentIndex, int parameterPosition) {
		StringLiteral previous = findPrevious(componentIndex);
		if (previous == null) {
			return null;
		}
		StringLiteral next = findNext(componentIndex);
		if (next == null) {
			return null;
		}
		Expression nonLiteralComponent = components.get(componentIndex);
		String setterName = findSetterName(nonLiteralComponent);
		if (setterName == null) {
			return null;
		}
		return new ReplaceableParameter(previous, next, nonLiteralComponent, setterName, parameterPosition);
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

}

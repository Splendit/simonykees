package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * @since 3.19.0
 *
 */
public class SQLQueryReplaceableParameterCollector extends UserSuppliedInputCollector {

	private static final int ONE_BASED_INDEX_OFFSET = 1;

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

	protected static final String SIMPLE_QUOTATION_MARK = "'"; //$NON-NLS-1$

	public SQLQueryReplaceableParameterCollector() {
		super(true, s -> s.endsWith(SIMPLE_QUOTATION_MARK), s -> s.startsWith(SIMPLE_QUOTATION_MARK));
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

	private String findSetterName(Expression component) {
		ITypeBinding type = component.resolveTypeBinding();
		String key = computeSetterKey(type);
		return SETTERS_MAP.get(key);
	}

	protected ReplaceableParameter createReplaceableParameter(UserSuppliedInput userSuppliedInput,
			int parameterPosition) {

		Expression nonLiteralComponent = userSuppliedInput.getInput();
		String setterName = findSetterName(nonLiteralComponent);
		if (setterName == null) {
			return null;
		}
		return new ReplaceableParameter(userSuppliedInput.getPrevious(), userSuppliedInput.getNext(),
				nonLiteralComponent, setterName, parameterPosition);
	}	

	private List<ReplaceableParameter> createReplaceableParameterList(List<UserSuppliedInput> userSuppliedInputList) {
		List<ReplaceableParameter> parameters = new ArrayList<>();
		int parameterPosition = getIndexOffset();
		for (UserSuppliedInput userSuppliedInput : userSuppliedInputList) {
			ReplaceableParameter parameter = createReplaceableParameter(userSuppliedInput, parameterPosition);
			if (parameter != null) {
				parameters.add(parameter);
				parameterPosition++;
			}
		}
		return parameters;
	}
	
	List<ReplaceableParameter> createReplaceableParameterList(SqlVariableAnalyzerVisitor sqlVariableVisitor){
		List<Expression> queryComponents = sqlVariableVisitor.getDynamicQueryComponents();
		List<UserSuppliedInput> userSuppliedInputList = collectUserSuppliedInput(queryComponents);
		return createReplaceableParameterList(userSuppliedInputList);
	}

	private int getIndexOffset() {
		return ONE_BASED_INDEX_OFFSET;
	}

}

package eu.jsparrow.core.visitor.security;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.StringLiteral;

public class QueryComponentsAnalyzer {

	private List<Expression> components;
	private List<ReplaceableParameter> parameters = new ArrayList<>();

	public QueryComponentsAnalyzer(List<Expression> components) {
		this.components = components;
	}

	public boolean analyze() {
		List<Expression> nonLiteralComponents = components.stream()
			.filter(component -> component.getNodeType() != ASTNode.STRING_LITERAL)
			.collect(Collectors.toList());

		/*
		 * 1. If it is final variable then skip it. 2.Prepare a list of
		 * parameters and the affected literals.
		 */
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

		return true;
	}

	private String findSetterName(Expression component) {
		ITypeBinding type = component.resolveTypeBinding();

		if (isContentOfType(type, java.sql.Array.class.getName())) {
			return "setArray"; //$NON-NLS-1$
		} else if (isContentOfType(type, java.math.BigDecimal.class.getName())) {
			return "setBigDecimal"; //$NON-NLS-1$
		} else if (isContentOfType(type, java.sql.Blob.class.getName())) {
			return "setBlob"; //$NON-NLS-1$
		} else if (isContentOfType(type, java.sql.Clob.class.getName())) {
			return "setClob"; //$NON-NLS-1$
		} else if (isContentOfType(type, java.sql.Date.class.getName())) {
			return "setDate"; //$NON-NLS-1$
		} else if (isContentOfType(type, java.lang.Object.class.getName())) {
			return "setObject"; //$NON-NLS-1$
		} else if (isContentOfType(type, java.sql.Ref.class.getName())) {
			return "setRef"; //$NON-NLS-1$
		} else if (isContentOfType(type, java.lang.String.class.getName())) {
			return "setString"; //$NON-NLS-1$
		} else if (isContentOfType(type, java.sql.Time.class.getName())) {
			return "setTime"; //$NON-NLS-1$
		} else if (isContentOfType(type, java.sql.Timestamp.class.getName())) {
			return "setTimestamp"; //$NON-NLS-1$
		} else if (isContentOfType(type, java.net.URL.class.getName())) {
			return "setURL"; //$NON-NLS-1$
		} else if (type.isArray() && "byte".equals(type.getComponentType() //$NON-NLS-1$
			.getName())) {
			return "setBytes";//$NON-NLS-1$
		} else if ("boolean".equals(type.getComponentType() //$NON-NLS-1$
			.getName()) || isContentOfType(type, java.lang.Boolean.class.getName())) {
			return "setBoolean";//$NON-NLS-1$
		} else if ("byte".equals(type.getComponentType() //$NON-NLS-1$
			.getName()) || isContentOfType(type, java.lang.Byte.class.getName())) {
			return "setByte";//$NON-NLS-1$
		} else if ("double".equals(type.getComponentType() //$NON-NLS-1$
			.getName()) || isContentOfType(type, java.lang.Double.class.getName())) {
			return "setDouble";//$NON-NLS-1$
		} else if ("float".equals(type.getComponentType() //$NON-NLS-1$
			.getName()) || isContentOfType(type, java.lang.Float.class.getName())) {
			return "setFloat";//$NON-NLS-1$
		} else if ("int".equals(type.getComponentType() //$NON-NLS-1$
			.getName()) || isContentOfType(type, java.lang.Integer.class.getName())) {
			return "setInt";//$NON-NLS-1$
		} else if ("long".equals(type.getComponentType() //$NON-NLS-1$
			.getName()) || isContentOfType(type, java.lang.Long.class.getName())) {
			return "setLong";//$NON-NLS-1$
		} else if ("short".equals(type.getComponentType() //$NON-NLS-1$
			.getName()) || isContentOfType(type, java.lang.Short.class.getName())) {
			return "setShort";//$NON-NLS-1$
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

	public Map<Expression, Expression> getReplacements() {
		return new HashMap<>();
	}

	public List<ReplaceableParameter> getReplaceableParameters() {
		return this.parameters;
	}

}

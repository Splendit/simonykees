package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Finds the components of a dynamic query that can be escaped.
 * 
 * @since 3.17.0
 *
 */
public class QueryComponentsAnalyzerForEscaping extends AbstractQueryComponentsAnalyzer {

	private List<Expression> expressionsToEscape = new ArrayList<>();

	public QueryComponentsAnalyzerForEscaping(List<Expression> components) {
		super(components);
	}

	/**
	 * Constructs a list of {@link ReplaceableParameter}s out of the
	 * {@link #components} of the query.
	 * 
	 * @return
	 */
	public void analyze() {
		List<Expression> nonLiteralComponents = collectNonLiteralComponents();
		for (Expression component : nonLiteralComponents) {
			if (isComponentToEscape(component)) {
				this.expressionsToEscape.add(component);
			}
		}
	}

	private boolean isComponentToEscape(Expression component) {
		int index = components.indexOf(component);
		StringLiteral stringLiteralBefore = findPrevious(index);
		StringLiteral stringLiteralAfter = findNext(index);
		if (stringLiteralBefore == null || stringLiteralAfter == null) {
			return false;
		}

		ITypeBinding expressionTypeBinding = component.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(expressionTypeBinding, java.lang.String.class.getName())) {
			return false;
		}

		if (component.getNodeType() == ASTNode.METHOD_INVOCATION) {
			return !isEncodeForSQLInvocation((MethodInvocation) component);
		}

		IVariableBinding variableBinding = null;
		if (component.getNodeType() == ASTNode.FIELD_ACCESS) {
			variableBinding = ((FieldAccess) component).resolveFieldBinding();

		} else if (component.getNodeType() == ASTNode.SIMPLE_NAME) {
			IBinding simpleNameBinding = ((SimpleName) component).resolveBinding();
			if (simpleNameBinding.getKind() == IBinding.VARIABLE) {
				variableBinding = (IVariableBinding) simpleNameBinding;
			}
		}
		return variableBinding != null && !Modifier.isFinal(variableBinding.getModifiers());
	}

	private boolean isEncodeForSQLInvocation(MethodInvocation methodInvocation) {
		if (!methodInvocation.getName()
			.getIdentifier()
			.equals("encodeForSQL")) { //$NON-NLS-1$
			return false;
		}

		Expression encodeMethodExpression = methodInvocation.getExpression();

		ITypeBinding expressionTypeBinding = encodeMethodExpression.resolveTypeBinding();
		if (expressionTypeBinding == null) {
			return false;
		}
		String encoderBaseType = "org.owasp.esapi.Encoder"; //$NON-NLS-1$
		List<String> encoderBaseTypeList = Collections.singletonList(encoderBaseType);

		return ClassRelationUtil.isContentOfTypes(expressionTypeBinding, encoderBaseTypeList) ||
				ClassRelationUtil.isInheritingContentOfTypes(expressionTypeBinding, encoderBaseTypeList);

	}

	/**
	 * @return the list of {@link Expression}s constructed by
	 *         {@link #analyze()}.
	 */
	public List<Expression> getExpressionsToEscape() {
		return this.expressionsToEscape;
	}

}

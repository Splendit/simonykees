package eu.jsparrow.core.visitor.security;

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

	public QueryComponentsAnalyzerForEscaping(List<Expression> components) {
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

		ITypeBinding expressionTypeBinding = nonLiteralComponent.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(expressionTypeBinding, java.lang.String.class.getName())) {
			return null;
		}

		if (nonLiteralComponent.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation invocation = (MethodInvocation) nonLiteralComponent;
			if (isEncodeForSQLInvocation(invocation)) {
				return null;
			}
		} else {
			IVariableBinding variableBinding = null;
			if (nonLiteralComponent.getNodeType() == ASTNode.FIELD_ACCESS) {
				variableBinding = ((FieldAccess) nonLiteralComponent).resolveFieldBinding();

			} else if (nonLiteralComponent.getNodeType() == ASTNode.SIMPLE_NAME) {
				IBinding simpleNameBinding = ((SimpleName) nonLiteralComponent).resolveBinding();
				if (simpleNameBinding.getKind() == IBinding.VARIABLE) {
					variableBinding = (IVariableBinding) simpleNameBinding;
				}
			}
			if (variableBinding == null) {
				return null;
			}

			if (Modifier.isFinal(variableBinding.getModifiers())) {
				return null;
			}
		}
		return new ReplaceableParameter(previous, next, nonLiteralComponent, null, parameterPosition);
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
}

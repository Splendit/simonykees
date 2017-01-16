package at.splendit.simonykees.core.visitor;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.StringLiteral;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * Every usage of the function {@link Object#toString()} on a Java Object is
 * removed, if it is used on an element with the type String
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class RemoveToStringOnStringASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static Integer STRING_KEY = 1;
	private static String STRING_FULLY_QUALLIFIED_NAME = "java.lang.String"; //$NON-NLS-1$

	public RemoveToStringOnStringASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(STRING_KEY, generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME));
	}

	@Override
	public boolean visit(MethodInvocation node) {

		/*
		 * Checks if method invocation is toString. The invocation needs to have
		 * zero arguments. The expressions type where the toString is used on
		 * needs to be a String or a StringLiteral
		 */
		Expression variableExpression = node.getExpression();
		if (StringUtils.equals("toString", node.getName().getFullyQualifiedName()) //$NON-NLS-1$
				&& node.typeArguments().isEmpty()
				&& (node.getExpression() != null && ClassRelationUtil
						.isContentOfRegistertITypes(variableExpression.resolveTypeBinding(), iTypeMap.get(STRING_KEY))
						|| variableExpression instanceof StringLiteral)) {
			if (variableExpression instanceof ParenthesizedExpression) {
				variableExpression = ASTNodeUtil.unwrapParenthesizedExpression(variableExpression);
			}
			astRewrite.replace(node, (Expression) astRewrite.createMoveTarget(variableExpression), null);
		}

		return true;
	}
}

package at.splendit.simonykees.core.visitor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * Looks for occurrences of equals(..) that refer to an Enumeration.
 * <p>
 * Those occurrences are then replaced with ==
 * <ul>
 * <li>Enum: since 1.5, ex.: myEnumInstance.equals(MyEnum.ITEM) ->
 * myEnumInstance == MyEnum.ITEM</li>
 * </ul>
 * 
 * @author Hans-Jörg Schrödl
 * @since 2.1.1
 */
public class EnumsWithoutEqualsASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String EQUALS = "equals"; //$NON-NLS-1$

	public boolean visit(MethodInvocation methodInvocation) {
		if (methodInvocation.arguments().isEmpty() || methodInvocation.getExpression() == null) {
			return false;
		}
		boolean isEquals = StringUtils.equals(EQUALS, methodInvocation.getName().getFullyQualifiedName());
		if (!isEquals) {
			return false;
		}
		Expression expression = methodInvocation.getExpression();
		if (!expression.resolveTypeBinding().isEnum()) {
			return false;
		}
		if(methodInvocation.arguments().size() != 1){
			return false;
		}
		Expression argument = (Expression) methodInvocation.arguments().get(0);
		if(!argument.resolveTypeBinding().isEnum()){
			return false;
		}
		
		Expression left = (Expression) astRewrite.createMoveTarget(expression);
		Expression right = (Expression) astRewrite.createMoveTarget(argument);
		Expression replacementNode = NodeBuilder.newInfixExpression(methodInvocation.getAST(), InfixExpression.Operator.EQUALS,
				left, right);
		astRewrite.replace(methodInvocation, replacementNode, null);
		return false;
	}

}

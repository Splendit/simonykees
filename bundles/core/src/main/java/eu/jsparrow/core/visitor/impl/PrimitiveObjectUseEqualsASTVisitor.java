package eu.jsparrow.core.visitor.impl;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.core.markers.common.PrimitiveObjectUseEqualsEvent;
import eu.jsparrow.core.rule.impl.PrimitiveObjectUseEqualsRule;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * Looks for occurrences of ==, != comparing two primitive objects, such as
 * java.lang.Integer or java.lang.Boolean. The full list of primitives can be
 * found <a href=
 * "https://en.wikibooks.org/wiki/Java_Programming/Primitive_Types">here</a>.
 * 
 * Those occurrences should be replaced by equals(). Using == compares object
 * references, which is often not what you want and can lead to bugs.
 * 
 * Used in PrimitiveObjectUseEqualsRule.
 * 
 * @see PrimitiveObjectUseEqualsRule
 * @author Hans-Jörg Schrödl
 * @since 2.1.1
 */
public class PrimitiveObjectUseEqualsASTVisitor extends AbstractASTRewriteASTVisitor implements PrimitiveObjectUseEqualsEvent {

	private static final String BYTE_FULLY_QUALIFIED_NAME = java.lang.Byte.class.getName();
	private static final String CHAR_FULLY_QUALIFIED_NAME = java.lang.Character.class.getName();
	private static final String SHORT_FULLY_QUALIFIED_NAME = java.lang.Short.class.getName();
	private static final String INTEGER_FULLY_QUALIFIED_NAME = java.lang.Integer.class.getName();
	private static final String LONG_FULLY_QUALIFIED_NAME = java.lang.Long.class.getName();
	private static final String FLOAT_FULLY_QUALIFIED_NAME = java.lang.Float.class.getName();
	private static final String DOUBLE_FULLY_QUALIFIED_NAME = java.lang.Double.class.getName();
	private static final String BOOLEAN_FULLY_QUALIFIED_NAME = java.lang.Boolean.class.getName();
	private static final String STRING_FULLY_QUALIFIED_NAME = java.lang.String.class.getName();

	private static final String EQUALS = "equals"; //$NON-NLS-1$

	@Override
	public boolean visit(InfixExpression infixExpression) {
		boolean isEqualsOrNotEqualsInfix = InfixExpression.Operator.EQUALS == infixExpression.getOperator()
				|| InfixExpression.Operator.NOT_EQUALS == infixExpression.getOperator();
		if (!isEqualsOrNotEqualsInfix) {
			return true;
		}
		if (!infixExpression.extendedOperands()
			.isEmpty()) {
			return true;
		}

		if (!onPrimitiveObjects(infixExpression)) {
			return true;
		}

		Expression replaceNode = createReplacementNode(infixExpression);
		astRewrite.replace(infixExpression, replaceNode, null);
		saveComments(infixExpression);
		onRewrite();
		addMarkerEvent(infixExpression);
		return true;
	}

	protected void saveComments(InfixExpression infixExpression) {
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Comment> relatedComments = commentRewriter.findLeadingComments(infixExpression);
		Statement parentStm = ASTNodeUtil.getSpecificAncestor(infixExpression, Statement.class);
		commentRewriter.saveBeforeStatement(parentStm, relatedComments);
	}

	private Expression createReplacementNode(InfixExpression infixExpression) {
		Expression left = createOperand(infixExpression, infixExpression.getLeftOperand());
		Expression right = (Expression) astRewrite.createMoveTarget(infixExpression.getRightOperand());
		SimpleName simpleName = NodeBuilder.newSimpleName(infixExpression.getAST(), EQUALS);
		Expression replacementNode = NodeBuilder.newMethodInvocation(infixExpression.getAST(), left, simpleName,
				Arrays.asList(right));
		if (infixExpression.getOperator() == InfixExpression.Operator.NOT_EQUALS) {
			replacementNode = NodeBuilder.newPrefixExpression(infixExpression.getAST(), PrefixExpression.Operator.NOT,
					replacementNode);
		}

		return replacementNode;
	}

	private Expression createOperand(InfixExpression infixExpression, Expression left) {
		Expression newOperand = (Expression) astRewrite.createMoveTarget(left);
		if (left.getNodeType() == ASTNode.CAST_EXPRESSION) {
			ParenthesizedExpression para = infixExpression.getAST()
				.newParenthesizedExpression();
			para.setExpression(newOperand);
			newOperand = para;
		}
		return newOperand;
	}

	private boolean onPrimitiveObjects(InfixExpression infixExpression) {
		Expression leftOperand = infixExpression.getLeftOperand();
		Expression rightOperand = infixExpression.getRightOperand();

		List<String> allowedTypes = Arrays.asList(BYTE_FULLY_QUALIFIED_NAME, CHAR_FULLY_QUALIFIED_NAME,
				SHORT_FULLY_QUALIFIED_NAME, INTEGER_FULLY_QUALIFIED_NAME, LONG_FULLY_QUALIFIED_NAME,
				FLOAT_FULLY_QUALIFIED_NAME, DOUBLE_FULLY_QUALIFIED_NAME, BOOLEAN_FULLY_QUALIFIED_NAME,
				STRING_FULLY_QUALIFIED_NAME);

		ITypeBinding leftOperandType = leftOperand.resolveTypeBinding();
		ITypeBinding rightOperandType = rightOperand.resolveTypeBinding();

		boolean isValidType = ClassRelationUtil.isContentOfTypes(leftOperandType, allowedTypes)
				&& ClassRelationUtil.isContentOfTypes(rightOperandType, allowedTypes);
		if (!isValidType) {
			return false;
		}

		// Do not refactor if these literals are involved, for example
		// 'c'.equals('d') doesn't work
		List<Integer> forbiddenNodeTypes = Arrays.asList(ASTNode.NUMBER_LITERAL, ASTNode.BOOLEAN_LITERAL,
				ASTNode.CHARACTER_LITERAL);
		return !forbiddenNodeTypes.stream()
			.anyMatch(x -> x == leftOperand.getNodeType() || x == rightOperand.getNodeType());
	}

}

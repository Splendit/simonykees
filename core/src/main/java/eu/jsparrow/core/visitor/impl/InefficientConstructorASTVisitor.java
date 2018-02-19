package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.core.builder.NodeBuilder;
import eu.jsparrow.core.constants.ReservedNames;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.core.visitor.CommentRewriter;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Primitives should not use the constructor for construction of new Variables.
 * Instead the .valueOf(..) should be used
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class InefficientConstructorASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String STRING_FULLY_QUALLIFIED_NAME = java.lang.String.class.getName();

	@Override
	public boolean visit(MethodInvocation node) {

		/*
		 * Boolean.valueOf(true); -> true, Boolean.valueOf("true"); -> true
		 * Boolean.valueOf(false); -> false, Boolean.valueOf("false"); -> false
		 * Boolean.valueOf("anyOtherString"); -> false Boolean/boolean b = ...
		 * Boolean.valueOf(b); -> b String s = ...; Boolean.valueOf(s); ->
		 * ignore
		 */
		if (node.getExpression() == null) {
			return true;
		}

		if (ASTNode.METHOD_INVOCATION == node.getParent()
			.getNodeType()) {
			return true;
		}

		if (StringUtils.equals(ReservedNames.MI_VALUE_OF, node.getName()
			.getFullyQualifiedName()) && null != node.getExpression() && ASTNode.SIMPLE_NAME == node.getExpression()
				.getNodeType() && 1 == node.arguments()
					.size()) {
			SimpleName refactorPrimitiveType = (SimpleName) node.getExpression();
			ITypeBinding refactorPrimitiveTypeBinding = refactorPrimitiveType.resolveTypeBinding();
			Expression refactorCandidateParameter = (Expression) node.arguments()
				.get(0);

			Expression replaceParameter;

			if (null != refactorPrimitiveTypeBinding && isBooleanClass(refactorPrimitiveTypeBinding.getName())
					&& ASTNode.STRING_LITERAL == refactorCandidateParameter.getNodeType()) {
				StringLiteral stringParameter = (StringLiteral) refactorCandidateParameter;
				if (ReservedNames.BOOLEAN_TRUE.equals(stringParameter.getLiteralValue())) {
					replaceParameter = node.getAST()
						.newBooleanLiteral(true);
				} else {
					replaceParameter = node.getAST()
						.newBooleanLiteral(false);
				}
				astRewrite.replace(refactorCandidateParameter, replaceParameter, null);
				getCommentRewriter().saveCommentsInParentStatement(node);
				onRewrite();
			}
		}
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (ASTNode.SIMPLE_TYPE == node.getType()
			.getNodeType()
				&& ASTNode.SIMPLE_NAME == ((SimpleType) node.getType()).getName()
					.getNodeType()
				&& 1 == node.arguments()
					.size()) {
			SimpleName refactorPrimitiveType = (SimpleName) ((SimpleType) node.getType()).getName();
			ITypeBinding refactorPrimitiveTypeBinding = refactorPrimitiveType.resolveTypeBinding();
			Expression refactorCandidateParameter = (Expression) node.arguments()
				.get(0);
			ITypeBinding refactorCandidateTypeBinding = refactorCandidateParameter.resolveTypeBinding();
			Expression replacement = null;
			List<Comment> relatedComments = new ArrayList<>();
			CommentRewriter commentRewriter = getCommentRewriter(); 

			if (null == refactorCandidateTypeBinding || null == refactorPrimitiveTypeBinding) {
				return true;
			}

			/*
			 * boolean case
			 */
			if (isBooleanClass(refactorPrimitiveTypeBinding.getName())) {
				// boolean wrapIfParentMethodInvocation = false

				/*
				 * all string-literals transformed to its boolean counterpart
				 */
				if (ASTNode.STRING_LITERAL == refactorCandidateParameter.getNodeType()) {
					StringLiteral stringParameter = (StringLiteral) refactorCandidateParameter;
					// wrapIfParentMethodInvocation = true
					if (ReservedNames.BOOLEAN_TRUE.equals(stringParameter.getLiteralValue())) {
						replacement = node.getAST()
							.newBooleanLiteral(true);
					} else {
						replacement = node.getAST()
							.newBooleanLiteral(false);
					}
					relatedComments = commentRewriter.findRelatedComments(node);
				}

				/* wrapping string variables into Boolean.valueOf(...) */
				else if (ClassRelationUtil.isContentOfTypes(refactorCandidateTypeBinding,
						generateFullyQualifiedNameList(STRING_FULLY_QUALLIFIED_NAME))) {
					replacement = (Expression) astRewrite.createMoveTarget(refactorCandidateParameter);
					relatedComments = findRelatedComments(node, refactorCandidateParameter);
				}

				/* primitive booleans */
				else if (isBooleanClass(refactorCandidateTypeBinding.getName())) {
					replacement = (Expression) astRewrite.createMoveTarget(refactorCandidateParameter);
					relatedComments = findRelatedComments(node, refactorCandidateParameter);
				}

				/* wrap object */
				SimpleName valueOfInvocation = NodeBuilder.newSimpleName(node.getAST(), ReservedNames.MI_VALUE_OF);
				replacement = NodeBuilder.newMethodInvocation(node.getAST(),
						(SimpleName) astRewrite.createMoveTarget(refactorPrimitiveType), valueOfInvocation,
						replacement);
			}

			/*
			 * primitive types
			 */
			else if (isPrimitiveTypeClass(refactorPrimitiveTypeBinding.getName())) {

				/*
				 * new Float(4D) is not transformable to Float.valueOf(4D)
				 * because valueOf only allows primitives that are implicit
				 * cast-able to float. doubles do not have this property
				 */
				Predicate<ITypeBinding> isDoubleVariable = binding -> (binding != null
						&& (StringUtils.contains(binding.getName(), ReservedNames.DOUBLE_PRIMITIVE)
								|| (StringUtils.contains(binding.getName(), ReservedNames.DOUBLE))));

				if (ReservedNames.FLOAT.equals(refactorPrimitiveType.getIdentifier())
						&& isDoubleVariable.test(refactorCandidateTypeBinding)) {
					return true;
				}

				/*
				 * wrapping string and primitive input parameter into
				 * PrimitiveType.valueOf(...)
				 */
				if (ClassRelationUtil.isContentOfTypes(refactorCandidateTypeBinding,
						generateFullyQualifiedNameList(STRING_FULLY_QUALLIFIED_NAME))
						|| isPrimitiveTypeClass(refactorCandidateTypeBinding.getName())) {
					SimpleName valueOfInvocation = NodeBuilder.newSimpleName(node.getAST(), ReservedNames.MI_VALUE_OF);
					replacement = NodeBuilder.newMethodInvocation(node.getAST(),
							(SimpleName) astRewrite.createMoveTarget(refactorPrimitiveType), valueOfInvocation,
							(Expression) astRewrite.createMoveTarget(refactorCandidateParameter));
					relatedComments = findRelatedComments(node, refactorCandidateParameter);
				}
			}
			if (replacement != null) {
				astRewrite.replace(node, replacement, null);
				commentRewriter.saveBeforeStatement(ASTNodeUtil.getSpecificAncestor(node, Statement.class), relatedComments);
				onRewrite();
			}
		}
		return true;
	}
	
	private List<Comment> findRelatedComments(ClassInstanceCreation node, Expression parameter) {
		CommentRewriter cr = getCommentRewriter();
		List<Comment> relatedComments = cr.findRelatedComments(node);
		relatedComments.removeAll(cr.findRelatedComments(parameter));
		return relatedComments;
	}

	private boolean isPrimitiveTypeClass(String simpleName) {
		switch (simpleName) {
		case ReservedNames.INTEGER:
		case ReservedNames.FLOAT:
		case ReservedNames.DOUBLE:
		case ReservedNames.LONG:
		case ReservedNames.SHORT:
		case ReservedNames.CHARACTER:
		case ReservedNames.BYTE:
		case ReservedNames.INTEGER_PRIMITIVE:
		case ReservedNames.FLOAT_PRIMITIVE:
		case ReservedNames.DOUBLE_PRIMITIVE:
		case ReservedNames.LONG_PRIMITIVE:
		case ReservedNames.SHORT_PRIMITIVE:
		case ReservedNames.CHARACTER_PRIMITIVE:
		case ReservedNames.BYTE_PRIMITIVE:
			return true;
		default:
			return false;
		}
	}

	private boolean isBooleanClass(String simpleName) {
		switch (simpleName) {
		case ReservedNames.BOOLEAN:
		case ReservedNames.BOOLEAN_PRIMITIVE:
			return true;
		default:
			return false;
		}
	}
}

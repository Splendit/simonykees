package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfTypes;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.isInheritingContentOfTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.markers.common.MapGetOrDefaultEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.util.OperatorUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * Replaces {@code Map.get([key])} by
 * {@code Map.getOrDefault([key], [defaultValue])}.
 * 
 * @since 3.4.0
 *
 */
public class MapGetOrDefaultASTVisitor extends AbstractASTRewriteASTVisitor implements MapGetOrDefaultEvent {

	private static final List<String> MAPS_FORBIDING_NULL_VALUES = Arrays.asList(
			java.util.jar.Attributes.class.getName(), java.util.concurrent.ConcurrentHashMap.class.getName(),
			java.util.concurrent.ConcurrentSkipListMap.class.getName(), java.util.Hashtable.class.getName(),
			java.util.Properties.class.getName());

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName name = methodInvocation.getName();
		if (!"get".equals(name.getIdentifier())) { //$NON-NLS-1$
			return true;
		}
		Expression expression = methodInvocation.getExpression();
		if (expression == null) {
			return true;
		}

		ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
		if (!(isContentOfTypes(expressionTypeBinding, MAPS_FORBIDING_NULL_VALUES)
				|| isInheritingContentOfTypes(expressionTypeBinding, MAPS_FORBIDING_NULL_VALUES))) {
			return true;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return true;
		}

		if (!ClassRelationUtil.isJavaUtilMethod(methodInvocation)) {
			/*
			 * We do not want to support user defined maps.
			 */
			return false;
		}

		SimpleName assignedVariable = findAssignedVariable(methodInvocation);
		if (assignedVariable == null) {
			return true;
		}

		Statement followingStatement = findFollowingStatement(methodInvocation);
		if (followingStatement == null || followingStatement.getNodeType() != ASTNode.IF_STATEMENT) {
			return true;
		}

		Expression defaultValue = findDefaultValueAssignment((IfStatement) followingStatement, assignedVariable);
		if (defaultValue == null) {
			return true;
		}

		if (!isTypeCompatible(defaultValue, expression)) {
			return true;
		}

		replace(methodInvocation, arguments.get(0), defaultValue, followingStatement);
		return true;
	}

	private boolean isTypeCompatible(Expression defaultValue, Expression map) {
		ITypeBinding mapTypeBinding = map.resolveTypeBinding();
		if (!mapTypeBinding.isParameterizedType()) {
			return false;
		}

		ITypeBinding[] typeArguments = mapTypeBinding.getTypeArguments();
		if (typeArguments.length != 2) {
			return false;
		}

		ITypeBinding mapValueType = typeArguments[1];
		ITypeBinding defaultValueType = defaultValue.resolveTypeBinding();

		return defaultValueType.isAssignmentCompatible(mapValueType);
	}

	private void replace(MethodInvocation methodInvocation, Expression key, Expression defaultValue,
			Statement followingStatement) {
		AST ast = methodInvocation.getAST();
		MethodInvocation getOrDefault = ast.newMethodInvocation();
		getOrDefault.setExpression((Expression) astRewrite.createCopyTarget(methodInvocation.getExpression()));
		getOrDefault.setName(ast.newSimpleName("getOrDefault")); //$NON-NLS-1$

		Expression keyCopy = (Expression) astRewrite.createCopyTarget(key);
		Expression defaultCopy = (Expression) astRewrite.createCopyTarget(defaultValue);
		@SuppressWarnings("unchecked")
		List<Expression> getOrDefaultArgumetns = getOrDefault.arguments();
		getOrDefaultArgumetns.add(keyCopy);
		getOrDefaultArgumetns.add(defaultCopy);
		astRewrite.replace(methodInvocation, getOrDefault, null);
		astRewrite.remove(followingStatement, null);
		addMarkerEvent(methodInvocation, key, defaultValue);
		onRewrite();

		/*
		 * Handle comments
		 */
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Comment> comments = new ArrayList<>();
		comments.addAll(commentRewriter.findRelatedComments(followingStatement));
		comments.removeAll(commentRewriter.findRelatedComments(defaultValue));
		commentRewriter.saveBeforeStatement(followingStatement, comments);
		comments.clear();
		comments.addAll(commentRewriter.findRelatedComments(methodInvocation));
		comments.removeAll(commentRewriter.findRelatedComments(key));
		comments.removeAll(commentRewriter.findRelatedComments(methodInvocation.getExpression()));
		Statement parentStatement = ASTNodeUtil.getSpecificAncestor(methodInvocation, Statement.class);
		commentRewriter.saveBeforeStatement(parentStatement, comments);
	}

	private Expression findDefaultValueAssignment(IfStatement ifStatement, SimpleName assignedVariableName) {

		if (ifStatement.getElseStatement() != null) {
			return null;
		}

		Expression condition = ifStatement.getExpression();
		if (!OperatorUtil.isNullCheck(assignedVariableName, condition, InfixExpression.Operator.EQUALS)) {
			return null;
		}

		ExpressionStatement singleBodyStatement = null;
		Statement thenStatement = ifStatement.getThenStatement();
		if (thenStatement.getNodeType() == ASTNode.BLOCK) {
			Block block = (Block) thenStatement;
			List<ExpressionStatement> bodyStatements = ASTNodeUtil.returnTypedList(block.statements(),
					ExpressionStatement.class);
			if (bodyStatements.size() == 1) {
				singleBodyStatement = bodyStatements.get(0);
			}
		} else if (thenStatement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
			singleBodyStatement = (ExpressionStatement) thenStatement;
		}

		if (singleBodyStatement == null) {
			return null;
		}

		Expression bodyExpression = singleBodyStatement.getExpression();
		if (bodyExpression.getNodeType() != ASTNode.ASSIGNMENT) {
			return null;
		}

		Assignment bodyAssignment = (Assignment) bodyExpression;
		Expression assignmentLhs = bodyAssignment.getLeftHandSide();
		if (assignmentLhs.getNodeType() != ASTNode.SIMPLE_NAME) {
			return null;
		}
		SimpleName assignmentLhsName = (SimpleName) assignmentLhs;
		if (!assignmentLhsName.getIdentifier()
			.equals(assignedVariableName.getIdentifier())) {
			return null;
		}
		return bodyAssignment.getRightHandSide();
	}

	private Statement findFollowingStatement(MethodInvocation mapGetInvocation) {

		Statement parentStatement = ASTNodeUtil.getSpecificAncestor(mapGetInvocation, Statement.class);
		ASTNode parent = parentStatement.getParent();
		if (parent.getNodeType() != ASTNode.BLOCK) {
			return null;
		}
		Block enclosingBlock = (Block) parent;
		List<Statement> enclosingBlockStatements = ASTNodeUtil.convertToTypedList(enclosingBlock.statements(),
				Statement.class);
		int index = enclosingBlockStatements.indexOf(parentStatement);
		if (index < 0 || index >= enclosingBlockStatements.size() - 1) {
			return null;
		}

		return enclosingBlockStatements.get(index + 1);
	}

	private boolean isExpressionStatementAssignmentRHS(MethodInvocation methodInvocation) {
		if (methodInvocation.getLocationInParent() != Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			return false;
		}
		Assignment assignment = (Assignment) methodInvocation.getParent();
		return assignment.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY;
	}

	private boolean isInitializerInDeclarationFragment(MethodInvocation methodInvocation) {
		if (methodInvocation.getLocationInParent() != VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			return false;
		}
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) methodInvocation.getParent();
		return fragment.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY;
	}

	private SimpleName findAssignedVariable(MethodInvocation mapGetInvocation) {
		if (isInitializerInDeclarationFragment(mapGetInvocation)) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) mapGetInvocation.getParent();
			SimpleName fragmentName = fragment.getName();
			ASTNode parent = fragment.getParent();
			LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(fragmentName);
			parent.accept(visitor);
			/*
			 * The variable should not be used in other fragments.
			 */
			List<SimpleName> references = visitor.getUsages();
			if (references.size() == 1) {
				return fragmentName;
			}
		}

		if (isExpressionStatementAssignmentRHS(mapGetInvocation)) {
			Assignment assignment = (Assignment) mapGetInvocation.getParent();
			Expression lhs = assignment.getLeftHandSide();
			if (lhs.getNodeType() == ASTNode.SIMPLE_NAME) {
				return (SimpleName) lhs;
			}
		}
		return null;
	}
}

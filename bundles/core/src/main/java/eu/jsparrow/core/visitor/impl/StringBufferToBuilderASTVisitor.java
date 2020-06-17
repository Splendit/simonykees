package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

/**
 * This rule replaces {@link StringBuffer} with {@link StringBuilder}. This is
 * only done for local variables which don't occur in {@link ReturnStatement}s
 * or {@link MethodInvocation}s.
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class StringBufferToBuilderASTVisitor extends AbstractASTRewriteASTVisitor {

	/*** FIELDS FOR TYPE CHECK ***/

	private static final String JAVA_LANG_STRINGBUFFER = java.lang.StringBuffer.class.getName();
	private static final String JAVA_LANG_STRINGBUILDER = java.lang.StringBuilder.class.getName();
	private static final String JAVA_LANG_STRINGBUILDER_SIMPLENAME = java.lang.StringBuilder.class.getSimpleName();

	private static final List<String> STRINGBUFFER_TYPE_LIST = Collections.singletonList(JAVA_LANG_STRINGBUFFER);
	private static final List<String> STRINGBUILDER_TYPE_LIST = Collections.singletonList(JAVA_LANG_STRINGBUILDER);

	/*** FIELDS ***/

	private List<VariableDeclarationStatement> stringBufferDeclarations = new LinkedList<>();
	private List<Assignment> stringBufferAssignmetns = new LinkedList<>();
	private ReturnStatement stringBufferReturnStatement = null;
	private List<String> stringBufferMethodInvocationArgs = new LinkedList<>();

	/*** VISITORS ***/

	/**
	 * collect {@link VariableDeclarationStatement}s of type
	 * {@link StringBuffer}.
	 */
	@Override
	public boolean visit(VariableDeclarationStatement variableDeclarationStatementNode) {
		ITypeBinding declarationTypeBinding = variableDeclarationStatementNode.getType()
			.resolveBinding();
		if (ClassRelationUtil.isContentOfTypes(declarationTypeBinding, STRINGBUFFER_TYPE_LIST)) {
			stringBufferDeclarations.add(variableDeclarationStatementNode);
		}

		return false;
	}

	/**
	 * collects {@link Assignment}s, where the {@link Assignment.Operator} is
	 * {@link Assignment.Operator#ASSIGN} and the left part of the Assignment is
	 * of type {@link StringBuffer} .
	 */
	@Override
	public boolean visit(Assignment assignmentNode) {
		if (Assignment.Operator.ASSIGN == assignmentNode.getOperator()) {
			Expression leftExpression = assignmentNode.getLeftHandSide();
			ITypeBinding leftExpressionTypeBinding = leftExpression.resolveTypeBinding();
			if (ClassRelationUtil.isContentOfTypes(leftExpressionTypeBinding, STRINGBUFFER_TYPE_LIST)) {
				stringBufferAssignmetns.add(assignmentNode);
			}
		}

		return false;
	}

	/**
	 * collects the method's {@link ReturnStatement}, if its expression is of
	 * type {@link StringBuffer}.
	 */
	@Override
	public boolean visit(ReturnStatement returnStatementNode) {
		Expression expression = returnStatementNode.getExpression();
		if (expression != null) {
			ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
			if (ClassRelationUtil.isContentOfTypes(expressionTypeBinding, STRINGBUFFER_TYPE_LIST)) {
				stringBufferReturnStatement = returnStatementNode;
			}
		}

		return false;
	}

	/**
	 * collects {@link SimpleName}s of method arguments, if their type is
	 * {@link StringBuffer}.
	 */
	@Override
	public boolean visit(MethodInvocation methodInvocationNode) {
		if (methodInvocationNode.arguments() != null && !methodInvocationNode.arguments()
			.isEmpty()) {
			List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocationNode.arguments(),
					Expression.class);
			arguments.stream()
				.filter(argument -> ASTNode.SIMPLE_NAME == argument.getNodeType())
				.map(argument -> (SimpleName) argument)
				.forEach(argumentSimpleName -> {
					ITypeBinding argumentTypeBinding = argumentSimpleName.resolveTypeBinding();
					if (ClassRelationUtil.isContentOfTypes(argumentTypeBinding, STRINGBUFFER_TYPE_LIST)) {
						stringBufferMethodInvocationArgs.add(argumentSimpleName.getIdentifier());
					}
				});
		}

		return false;
	}

	/**
	 * carries out some checks and does the actual transformation.
	 */
	@Override
	public void endVisit(MethodDeclaration node) {
		stringBufferDeclarations.forEach(declaration -> {
			List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(declaration.fragments(),
					VariableDeclarationFragment.class);
			List<String> declarationFragmentNames = fragments.stream()
				.map(fragment -> fragment.getName()
					.getIdentifier())
				.collect(Collectors.toList());

			List<String> occuringFragmentNames = declarationFragmentNames.stream()
				.filter(stringBufferMethodInvocationArgs::contains)
				.collect(Collectors.toList());

			if (isUsedAsBufferedReaderAssignment(declaration)) {
				return;
			}

			if (occuringFragmentNames != null && occuringFragmentNames.isEmpty()) {
				String returnStatementName = getReturnStatementExpressionSimpleName(stringBufferReturnStatement);
				if (!declarationFragmentNames.contains(returnStatementName)) {

					Optional<List<Assignment>> validAssignments = getValidAssignments(declarationFragmentNames);

					if (validAssignments.isPresent() && isFragmentsValid(fragments)) {

						validAssignments.get()
							.stream()
							.filter(assignment -> ASTNode.CLASS_INSTANCE_CREATION == assignment.getRightHandSide()
								.getNodeType())
							.map(assignment -> (ClassInstanceCreation) assignment.getRightHandSide())
							.forEach(creation -> {
								ClassInstanceCreation newCreation = createClassInstanceCreation(creation);
								astRewrite.replace(creation, newCreation, null);
							});

						VariableDeclarationStatement newDeclaration = createVariableDeclarationStatement(fragments);

						astRewrite.replace(declaration, newDeclaration, null);
						getCommentRewriter().saveRelatedComments(declaration);
						onRewrite();
					}
				}
			}
		});

		stringBufferAssignmetns.clear();
		stringBufferDeclarations.clear();
		stringBufferReturnStatement = null;
	}

	/*** VALIDATORS ***/

	private boolean isUsedAsBufferedReaderAssignment(VariableDeclarationStatement declaration) {
		List<SimpleName> fragmentNames = ASTNodeUtil
			.convertToTypedList(declaration.fragments(), VariableDeclarationFragment.class)
			.stream()
			.map(VariableDeclarationFragment::getName)
			.collect(Collectors.toList());
		ASTNode declarationParent = declaration.getParent();
		for (SimpleName stringBuffername : fragmentNames) {
			LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(stringBuffername);
			declarationParent.accept(visitor);
			List<SimpleName> references = visitor.getUsages();
			for (SimpleName reference : references) {
				if (isExpressionOf(reference, java.lang.StringBuffer.class.getName(),
						Assignment.RIGHT_HAND_SIDE_PROPERTY)) {
					return true;
				}

				if (isExpressionOf(reference, java.lang.StringBuffer.class.getName(),
						VariableDeclarationFragment.INITIALIZER_PROPERTY)) {
					return true;
				}
			}

		}
		return false;
	}

	private static boolean isExpressionOf(Expression astNode, String assignedType,
			ChildPropertyDescriptor locationInAncestor) {
		if (astNode.getLocationInParent() == locationInAncestor) {
			Expression expression = (Expression) astNode;
			ITypeBinding typeBinding = expression.resolveTypeBinding();
			return typeBinding == null || ClassRelationUtil.isContentOfType(typeBinding, assignedType);
		}

		ASTNode parent = astNode.getParent();
		if (parent instanceof Expression) {
			return isExpressionOf((Expression) parent, assignedType, locationInAncestor);
		}
		return false;
	}

	/**
	 * gets the {@link SimpleName} of the given {@link ReturnStatement}
	 * 
	 * @param returnStatement
	 * @return the {@link SimpleName} of the {@link ReturnStatement}s
	 *         {@link Expression} or null, if the {@link Expression} is not a
	 *         {@link SimpleName}.
	 */
	private String getReturnStatementExpressionSimpleName(ReturnStatement returnStatement) {
		if (returnStatement != null) {
			Expression expression = returnStatement.getExpression();

			if (expression.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
				expression = ASTNodeUtil.unwrapParenthesizedExpression(expression);
			}

			if (expression == null) {
				return null;
			}
			if (ASTNode.SIMPLE_NAME == expression.getNodeType()) {
				SimpleName simpleName = (SimpleName) expression;
				return simpleName.getIdentifier();
			}

			if (ASTNode.METHOD_INVOCATION == expression.getNodeType()) {
				SimpleName methodInvocationExpression = findMethodChainExpression((MethodInvocation) expression);
				if (methodInvocationExpression != null) {
					return methodInvocationExpression.getIdentifier();
				}
			}
		}

		return null;
	}

	private SimpleName findMethodChainExpression(MethodInvocation methodInvocation) {
		Expression expression = methodInvocation.getExpression();
		if (expression == null) {
			return null;
		}

		if (expression.getNodeType() == ASTNode.SIMPLE_NAME) {
			return (SimpleName) expression;
		}

		if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
			return findMethodChainExpression((MethodInvocation) expression);
		}
		return null;
	}

	/**
	 * checks if the {@link Assignment}s are valid and transformable to
	 * {@link StringBuilder}.
	 * 
	 * @param declarationFragmentNames
	 * @return a {@link List} of valid {@link Assignment}s (possibly empty) if
	 *         the transformation can occur, null otherwise
	 */
	private Optional<List<Assignment>> getValidAssignments(List<String> declarationFragmentNames) {
		List<Assignment> validAssignments = new LinkedList<>();
		int totalAssignmentCount = 0;
		for (Assignment assignment : stringBufferAssignmetns) {
			if (ASTNode.SIMPLE_NAME == assignment.getLeftHandSide()
				.getNodeType()
					&& declarationFragmentNames.contains(((SimpleName) assignment.getLeftHandSide()).getIdentifier())) {
				totalAssignmentCount++;
				if (ASTNode.CLASS_INSTANCE_CREATION == assignment.getRightHandSide()
					.getNodeType()) {
					validAssignments.add(assignment);
				} else {
					ITypeBinding expressionTypeBinding = assignment.getRightHandSide()
						.resolveTypeBinding();
					if (ClassRelationUtil.isContentOfTypes(expressionTypeBinding, STRINGBUILDER_TYPE_LIST)) {
						validAssignments.add(assignment);
					} else {
						break;
					}
				}
			}
		}

		if (validAssignments.size() == totalAssignmentCount) {
			return Optional.of(validAssignments);
		}

		return Optional.empty();
	}

	/**
	 * checks if the {@link VariableDeclarationFragment}s of the current
	 * {@link VariableDeclarationStatement} are all transformable to
	 * {@link StringBuilder}.
	 * 
	 * @param fragments
	 * @return true, if the transformation is possible, false otherwise.
	 */
	private boolean isFragmentsValid(List<VariableDeclarationFragment> fragments) {
		List<VariableDeclarationFragment> validFragments = new LinkedList<>();
		fragments.forEach(fragment -> {
			Expression initializer = fragment.getInitializer();
			if (initializer != null) {
				if (ASTNode.CLASS_INSTANCE_CREATION == initializer.getNodeType()
						|| ASTNode.NULL_LITERAL == initializer.getNodeType()) {
					validFragments.add(fragment);
				} else {
					ITypeBinding initializerTypeBinding = initializer.resolveTypeBinding();
					if (ClassRelationUtil.isContentOfTypes(initializerTypeBinding, STRINGBUILDER_TYPE_LIST)) {
						validFragments.add(fragment);
					}
				}
			} else {
				validFragments.add(fragment);
			}
		});

		return validFragments.size() == fragments.size();
	}

	/*** CREATORS ***/

	/**
	 * creates a new {@link VariableDeclarationStatement} of type
	 * {@link StringBuilder} with the given
	 * {@link VariableDeclarationFragment}s.
	 * 
	 * @param fragments
	 * @return a new {@link StringBuilder} {@link VariableDeclarationStatement}
	 *         or null, if an error occurred.
	 */
	private VariableDeclarationStatement createVariableDeclarationStatement(
			List<VariableDeclarationFragment> fragments) {
		VariableDeclarationStatement newDeclaration = null;
		VariableDeclarationFragment firstFragment = fragments.get(0);

		VariableDeclarationFragment firstFragmentCopy = createStringBuilderFragmentCopy(firstFragment);

		if (firstFragmentCopy != null) {
			newDeclaration = astRewrite.getAST()
				.newVariableDeclarationStatement(firstFragmentCopy);

			SimpleName stringBuilderTypeName = astRewrite.getAST()
				.newSimpleName(JAVA_LANG_STRINGBUILDER_SIMPLENAME);
			SimpleType stringBuilderType = astRewrite.getAST()
				.newSimpleType(stringBuilderTypeName);
			newDeclaration.setType(stringBuilderType);

			ListRewrite newDeclarationFragments = astRewrite.getListRewrite(newDeclaration,
					VariableDeclarationStatement.FRAGMENTS_PROPERTY);
			for (int i = 1; i < fragments.size(); i++) {
				VariableDeclarationFragment fragment = fragments.get(i);

				VariableDeclarationFragment newFragmentCopy = createStringBuilderFragmentCopy(fragment);
				if (newFragmentCopy != null) {
					newDeclarationFragments.insertLast(newFragmentCopy, null);
				}
			}
		}

		return newDeclaration;
	}

	/**
	 * modifies the given {@link VariableDeclarationFragment} if necessary and
	 * creates a copy target of it.
	 * 
	 * @param fragment
	 * @return a copy target of the given {@link VariableDeclarationFragment} or
	 *         null, if it doesn't meet the criteria.
	 */
	private VariableDeclarationFragment createStringBuilderFragmentCopy(VariableDeclarationFragment fragment) {
		VariableDeclarationFragment fragmentCopy = null;

		Expression initializer = fragment.getInitializer();
		if (initializer != null) {
			ITypeBinding initializerTypeBinding = initializer.resolveTypeBinding();
			if (ASTNode.CLASS_INSTANCE_CREATION == initializer.getNodeType()) {
				ClassInstanceCreation creation = (ClassInstanceCreation) initializer;
				ClassInstanceCreation newCreation = createClassInstanceCreation(creation);
				astRewrite.replace(creation, newCreation, null);
				fragmentCopy = (VariableDeclarationFragment) astRewrite.createCopyTarget(fragment);
			} else if (ASTNode.NULL_LITERAL == initializer.getNodeType()
					|| ClassRelationUtil.isContentOfTypes(initializerTypeBinding, STRINGBUILDER_TYPE_LIST)) {
				fragmentCopy = (VariableDeclarationFragment) astRewrite.createCopyTarget(fragment);
			}
		} else {
			fragmentCopy = (VariableDeclarationFragment) astRewrite.createCopyTarget(fragment);
		}

		return fragmentCopy;
	}

	/**
	 * creates a new {@link ClassInstanceCreation} of type
	 * {@link StringBuilder}.
	 * 
	 * @param oldCreation
	 * @return a new {@link ClassInstanceCreation} of type
	 *         {@link StringBuilder}.
	 */
	private ClassInstanceCreation createClassInstanceCreation(ClassInstanceCreation oldCreation) {
		SimpleName stringBuilderName = astRewrite.getAST()
			.newSimpleName(JAVA_LANG_STRINGBUILDER_SIMPLENAME);
		SimpleType stringBuilderType = astRewrite.getAST()
			.newSimpleType(stringBuilderName);

		ClassInstanceCreation newCreation = astRewrite.getAST()
			.newClassInstanceCreation();
		newCreation.setType(stringBuilderType);

		if (!oldCreation.arguments()
			.isEmpty()) {
			ListRewrite newCreationArguments = astRewrite.getListRewrite(newCreation,
					ClassInstanceCreation.ARGUMENTS_PROPERTY);
			ASTNodeUtil.convertToTypedList(oldCreation.arguments(), Expression.class)
				.forEach(argument -> newCreationArguments.insertLast(astRewrite.createCopyTarget((ASTNode) argument),
						null));
		}

		return newCreation;
	}
}

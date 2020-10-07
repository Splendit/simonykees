package eu.jsparrow.core.visitor.lambdaforeach;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Replaces {@link Stream#forEach(java.util.function.Consumer)} with
 * {@link Stream#collect(Collector)} and introduces a new method invocation
 * expression {@link List#addAll(Collection)} for adding for adding the result
 * of the {@link Stream#collect(Collector)} to the target list.
 * 
 * For example, the following code:
 * 
 * <pre>
 * <code>{@code
 * List<String> oStrings = new ArrayList<>();
 * List<String> objectList = new ArrayList<>();
 * }
 * objectList.stream().map(o -> o.substring(0)).forEach(oString -> {
 * 	oStrings.add(oString);
 * });
 * </code>
 * </pre>
 * 
 * is transformed to the following:
 * 
 * <pre>
 * <code>{@code 
 * List<String> oStrings = new ArrayList<>();
 * List<String> objectList = new ArrayList<>();
 * }
 * oStrings.addAll(objectList.stream().map(o -> StringUtils.substring(o, 0)).collect(Collectors.toList()));
 * </code>
 * </pre>
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class LambdaForEachCollectASTVisitor extends AbstractLambdaForEachASTVisitor {

	private static final String ADD_METHOD_NAME = "add"; //$NON-NLS-1$
	private static final String JAVA_UTIL_STREAM_COLLECTORS = java.util.stream.Collectors.class.getName();
	private static final String JAVA_UTIL_LIST = java.util.List.class.getName();
	private static final String TO_LIST = "toList"; //$NON-NLS-1$
	private static final String COLLECT = "collect"; //$NON-NLS-1$
	private static final String ADD_ALL = "addAll"; //$NON-NLS-1$

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if(continueVisiting) {
			verifyImport(compilationUnit, JAVA_UTIL_STREAM_COLLECTORS);
		}

		return continueVisiting;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		// if the method name matches with 'Stream::forEach' ...
		if (isStreamForEachInvocation(methodInvocation) && !isRawMethodExpression(methodInvocation)) {

			// and if the parameter of 'forEach' is a lambda expression ...
			List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
			if (arguments.size() == 1 && ASTNode.LAMBDA_EXPRESSION == arguments.get(0)
				.getNodeType()) {
				/*
				 * the lambda expression must have only one parameter and its
				 * body must contain only one expression invoking the
				 * 'List::add' method.
				 */
				LambdaExpression lambdaExpression = (LambdaExpression) arguments.get(0);
				SimpleName parameter = extractSingleParameter(lambdaExpression);
				MethodInvocation bodyExpression = extractSingleBodyExpression(lambdaExpression);

				if (parameter != null && bodyExpression != null && isListAddInvocation(bodyExpression, parameter)) {
					Expression collectionExpression = bodyExpression.getExpression();
					if (ASTNode.SIMPLE_NAME == collectionExpression.getNodeType()) {
						SimpleName collection = (SimpleName) collectionExpression;

						/*
						 * Replace forEach with collect(Collectors.toList()) and
						 * use the result as parameter in Collection::addAll
						 * method.
						 */
						Expression targetDecl = createTargetExpression(methodInvocation, collection);
						astRewrite.replace(methodInvocation, targetDecl, null);
						getCommentRewriter().saveCommentsInParentStatement(lambdaExpression);
						
						onRewrite();
					}
				}

			}
		}

		return true;
	}

	/**
	 * Checks if the expression of the given method binding is a raw type.
	 * 
	 * @param methodInvocation
	 *            method invocation to be checked
	 * @return {@code true} if the method invocation has an expression which
	 *         resolves to a raw type, or {@code false} otherwise.
	 */
	private boolean isRawMethodExpression(MethodInvocation methodInvocation) {
		Expression expression = methodInvocation.getExpression();
		if (expression != null) {
			ITypeBinding expressionBinding = expression.resolveTypeBinding();
			if (expressionBinding != null && expressionBinding.isRawType()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates a new method invocation expression of the form:
	 * 
	 * <pre>
	 * {@code collection.addAll(streamObject.steam(). ... .collect(Collectors.toList()))}
	 * 
	 * <pre>
	 * 
	 * @param methodInvocation
	 *            method invocation of
	 *            {@link Stream#forEach(java.util.function.Consumer)}
	 * @param collection
	 *            target list name
	 * 
	 * @return new method invocation expression
	 */
	private Expression createTargetExpression(MethodInvocation methodInvocation, SimpleName collection) {

		AST ast = methodInvocation.getAST();
		MethodInvocation collect = ast.newMethodInvocation();
		collect.setName(ast.newSimpleName(COLLECT));

		ListRewrite listRewirte = astRewrite.getListRewrite(collect, MethodInvocation.ARGUMENTS_PROPERTY);
		MethodInvocation collectorsToList = collect.getAST()
			.newMethodInvocation();
		collectorsToList.setName(collect.getAST()
			.newSimpleName(TO_LIST));
		Name streamCollectorsTypeName = addImport(JAVA_UTIL_STREAM_COLLECTORS);
		collectorsToList.setExpression(streamCollectorsTypeName);
		listRewirte.insertFirst(collectorsToList, null);
		

		collect.setExpression((Expression) astRewrite.createCopyTarget(methodInvocation.getExpression()));

		MethodInvocation addAllInvocation = ast.newMethodInvocation();
		addAllInvocation.setName(ast.newSimpleName(ADD_ALL));
		addAllInvocation.setExpression((SimpleName) astRewrite.createCopyTarget(collection));
		listRewirte = astRewrite.getListRewrite(addAllInvocation, MethodInvocation.ARGUMENTS_PROPERTY);
		listRewirte.insertFirst(collect, null);

		return addAllInvocation;
	}

	/**
	 * Checks whether the given method invocation is an occurrence of
	 * {@link List#add(Object)}
	 * 
	 * @param methodInvocation
	 *            a node representing the method invocation to look for.
	 * @return {@code true} if the method invocation is an
	 *         {@link List#add(Object)} or {@code false} otherwise.
	 */
	private boolean isListAddInvocation(MethodInvocation methodInvocation, SimpleName parameter) {
		SimpleName name = methodInvocation.getName();
		if (ADD_METHOD_NAME.equals(name.getIdentifier())) {
			List<Expression> arguments = ASTNodeUtil.returnTypedList(methodInvocation.arguments(), Expression.class);
			if (arguments.size() == 1) {
				Expression argument = arguments.get(0);
				if (ASTNode.SIMPLE_NAME == argument.getNodeType() && ((SimpleName) argument).getIdentifier()
					.equals(parameter.getIdentifier())) {
					Expression expression = methodInvocation.getExpression();
					if (expression != null && ClassRelationUtil.isContentOfTypes(expression.resolveTypeBinding(),
							Collections.singletonList(JAVA_UTIL_LIST))) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Checks whether the body of the lambda expression consist of a single
	 * method invocation.
	 * 
	 * @param lambdaExpression
	 *            a node representing a lambda expression
	 * 
	 * @return a {@link MethodInvocation} consisting the body of the lambda
	 *         expression or {@code null} if the body lambda expression is not a
	 *         single method invocation or contains more than one statements.
	 */
	private MethodInvocation extractSingleBodyExpression(LambdaExpression lambdaExpression) {
		MethodInvocation methodInvocation = null;
		ASTNode body = lambdaExpression.getBody();
		if (ASTNode.BLOCK == body.getNodeType()) {
			Block block = (Block) body;
			List<ExpressionStatement> statements = ASTNodeUtil.returnTypedList(block.statements(),
					ExpressionStatement.class);
			if (statements.size() == 1) {
				ExpressionStatement statement = statements.get(0);
				Expression expression = statement.getExpression();
				if (ASTNode.METHOD_INVOCATION == expression.getNodeType()) {
					methodInvocation = (MethodInvocation) expression;
				}

			}
		} else if (ASTNode.METHOD_INVOCATION == body.getNodeType()) {
			methodInvocation = (MethodInvocation) body;
		}

		return methodInvocation;
	}
}

package at.splendit.simonykees.core.visitor;

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
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * Replaces {@link Stream#forEach(java.util.function.Consumer)} with
 * {@link Stream#collect(Collector)} and introduces a 
 * new method invocation expression {@link List#addAll(Collection)} for adding
 * for adding the result of the {@link Stream#collect(Collector)} to 
 * the target list. 
 * <pre>
 * For example, the following code:
 * <pre>
 * {@code 
 * 		List<String> oStrings = new ArrayList<>();
 * 		List<String> objectList = new ArrayList<>();
 * 		objectList.stream().map(o -> o.substring(0))
 * 		.forEach( oString -> { 
 * 			oStrings.add(oString);
 * 		});}
 * <pre>
 * is transformed to the following:
 * <pre>
 * {@code 
 * 		List<String> oStrings = new ArrayList<>();
 * 		List<String> objectList = new ArrayList<>();
 * 		oStrings.addAll(objectList.stream().map(o -> StringUtils.substring(o, 0)).collect(Collectors.toList()));
 * }
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class LambdaForEachCollectASTVisitor extends AbstractAddImportASTVisitor {

	private static final String FOR_EACH_METHOD_NAME = "forEach"; //$NON-NLS-1$
	private static final String ADD_METHOD_NAME = "add"; //$NON-NLS-1$
	private static final String JAVA_UTIL_STREAM = java.util.stream.Stream.class.getName();
	private static final String JAVA_UTIL_STREAM_COLLECTORS = java.util.stream.Collectors.class.getName();
	private static final String JAVA_UTIL_STREAM_COLLECTORS_SIMPLE_NAME = java.util.stream.Collectors.class
			.getSimpleName();
	private static final String JAVA_UTIL_LIST = java.util.List.class.getName();
	private static final String TO_LIST = "toList"; //$NON-NLS-1$
	private static final String COLLECT = "collect"; //$NON-NLS-1$
	private static final String ADD_ALL = "addAll"; //$NON-NLS-1$

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean safeToAddImport = ASTNodeUtil.convertToTypedList(compilationUnit.imports(), ImportDeclaration.class)
				.stream().map(ImportDeclaration::getName).filter(Name::isQualifiedName)
				.map(name -> ((QualifiedName) name).getName())
				.noneMatch(JAVA_UTIL_STREAM_COLLECTORS_SIMPLE_NAME::equals);

		return safeToAddImport && super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		SimpleName methodName = methodInvocation.getName();
		// if the method name matches with 'java.util.stream.Stream::forEach'
		// ...
		if (FOR_EACH_METHOD_NAME.equals(methodName.getIdentifier())
				&& ASTNode.EXPRESSION_STATEMENT == methodInvocation.getParent().getNodeType()) {
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			if (methodBinding != null && ClassRelationUtil.isContentOfTypes(methodBinding.getDeclaringClass(),
					Collections.singletonList(JAVA_UTIL_STREAM))) {
				// and if the parameter of 'forEach' is a lambda expression ...
				List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
						Expression.class);
				if (arguments.size() == 1 && ASTNode.LAMBDA_EXPRESSION == arguments.get(0).getNodeType()) {
					/*
					 * the lambda expression must have only one parameter and
					 * its body must contain only one expression invoking the
					 * 'List::add' method.
					 */
					LambdaExpression lambdaExpression = (LambdaExpression) arguments.get(0);
					SimpleName parameter = extractSingleParameter(lambdaExpression);
					MethodInvocation bodyExpression = extractSingleBodyExpression(lambdaExpression);

					if (parameter != null && bodyExpression != null && isListAddInvocation(bodyExpression)) {
						Expression collectionExpression = bodyExpression.getExpression();
						if (ASTNode.SIMPLE_NAME == collectionExpression.getNodeType()) {
							SimpleName collection = (SimpleName) collectionExpression;

							/*
							 * Replace forEach with collect(Collectors.toList())
							 * and use the result as parameter in
							 * Collection::addAll method.
							 */
							Expression targetDecl = createTargetExpression(methodInvocation, collection);
							astRewrite.replace(methodInvocation, targetDecl, null);
						}
					}

				}
			}
		}

		return true;
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
		MethodInvocation collectorsToList = collect.getAST().newMethodInvocation();
		collectorsToList.setName(collect.getAST().newSimpleName(TO_LIST));
		collectorsToList.setExpression(collect.getAST().newSimpleName(JAVA_UTIL_STREAM_COLLECTORS_SIMPLE_NAME));
		listRewirte.insertFirst(collectorsToList, null);
		this.addImports.add(JAVA_UTIL_STREAM_COLLECTORS);

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
	private boolean isListAddInvocation(MethodInvocation methodInvocation) {
		boolean isAddInvocation = false;
		SimpleName name = methodInvocation.getName();
		if (ADD_METHOD_NAME.equals(name.getIdentifier())) {
			Expression expression = methodInvocation.getExpression();
			if (ClassRelationUtil.isContentOfTypes(expression.resolveTypeBinding(),
					Collections.singletonList(JAVA_UTIL_LIST))) {
				isAddInvocation = true;
			}
		}
		return isAddInvocation;
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

	/**
	 * Checks whether a lambda expression has a single parameter.
	 * 
	 * @param lambdaExpression
	 *            lambda expression to check for.
	 * @return the name of the parameter or {@code null} if the lambda
	 *         expression has more than one ore zero parameters.
	 */
	private SimpleName extractSingleParameter(LambdaExpression lambdaExpression) {
		SimpleName parameter = null;
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(lambdaExpression.parameters(),
				VariableDeclarationFragment.class);
		if (fragments.size() == 1) {
			VariableDeclarationFragment fragment = fragments.get(0);
			parameter = fragment.getName();
		} else {
			List<SingleVariableDeclaration> declarations = ASTNodeUtil.returnTypedList(lambdaExpression.parameters(),
					SingleVariableDeclaration.class);
			if (declarations.size() == 1) {
				SingleVariableDeclaration declaration = declarations.get(0);
				parameter = declaration.getName();
			}
		}
		return parameter;
	}
}

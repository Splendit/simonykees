package eu.jsparrow.core.visitor.loop.stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * this rule visits all enhanced for loops and checks if the corresponding loop
 * body is applicable for lambda expressions as parameter for
 * {@link java.util.stream.Stream#forEach(java.util.function.Consumer)}. Each
 * loop body is checked separately by the
 * {@link StreamForEachCheckValidStatementASTVisitor}
 * 
 * @author Matthias Webhofer, Ardit Ymeri
 * @since 1.2
 */
public class EnhancedForLoopToStreamForEachASTVisitor extends AbstractEnhancedForLoopToStreamASTVisitor {

	private static final List<String> TYPE_BINDING_CHECK_LIST = Collections.singletonList(JAVA_UTIL_COLLECTION);

	private CompilationUnit compilationUnit;

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		return super.visit(compilationUnit);
	}

	@Override
	public void endVisit(EnhancedForStatement enhancedForStatementNode) {
		SingleVariableDeclaration parameter = enhancedForStatementNode.getParameter();
		Type parameterType = parameter.getType();
		Expression expression = enhancedForStatementNode.getExpression();
		Statement statement = enhancedForStatementNode.getBody();
		SimpleName parameterName = parameter.getName();
		ITypeBinding parameterTypeBinding = parameterName.resolveTypeBinding();

		if (isGeneratedNode(parameterType)) {
			return;
		}

		if (isConditionalExpression(expression)) {
			return;
		}

		// expression must be of type java.util.Collection
		ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
		if (expressionTypeBinding == null || expressionTypeBinding.isRawType()) {
			return;
		}
		if ((ClassRelationUtil.isInheritingContentOfTypes(expressionTypeBinding, TYPE_BINDING_CHECK_LIST)
				|| ClassRelationUtil.isContentOfTypes(expressionTypeBinding, TYPE_BINDING_CHECK_LIST))
				&& isTypeSafe(parameterTypeBinding)) {

			ASTNode approvedStatement = getApprovedStatement(statement, parameterName);

			if (approvedStatement != null) {

				/*
				 * create method invocation java.util.Collection::stream on the
				 * expression of the enhanced for loop with no parameters
				 */
				Expression expressionCopy = createExpressionForStreamMethodInvocation(expression);

				if (parameterType.isPrimitiveType()) {
					MethodInvocation mapToPrimitive = calcMappingMethod((PrimitiveType) parameterType, expressionCopy);
					if (mapToPrimitive != null) {
						expressionCopy = mapToPrimitive;
					}
				}

				/*
				 * create lambda expression, which will be used as the only
				 * parameter of the forEach method. The parameter and the body
				 * of the enhanced for loop will be used for the corresponding
				 * parts of the lambda expression.
				 */
				SimpleName parameterNameCopy = (SimpleName) astRewrite.createCopyTarget(parameterName);

				ASTNode lambdaParameter;
				List<IExtendedModifier> modifiers = ASTNodeUtil.returnTypedList(parameter.modifiers(),
						IExtendedModifier.class);
				if (modifiers.isEmpty() && isTypeSafe(expressionTypeBinding)) {
					VariableDeclarationFragment temp = astRewrite.getAST()
						.newVariableDeclarationFragment();
					temp.setName(parameterNameCopy);
					lambdaParameter = temp;
				} else {
					Type parameterTypeCopy = (Type) astRewrite.createCopyTarget(parameterType);
					SingleVariableDeclaration temp = NodeBuilder.newSingleVariableDeclaration(astRewrite.getAST(),
							parameterNameCopy, parameterTypeCopy);
					ListRewrite lambdaExpressionParameterListRewrite = astRewrite.getListRewrite(temp,
							SingleVariableDeclaration.MODIFIERS2_PROPERTY);
					for (IExtendedModifier mod : modifiers) {
						lambdaExpressionParameterListRewrite.insertLast(astRewrite.createCopyTarget((ASTNode) mod),
								null);
					}
					lambdaParameter = temp;
				}

				ASTNode statementCopy = astRewrite.createCopyTarget(approvedStatement);

				LambdaExpression lambdaExpression = astRewrite.getAST()
					.newLambdaExpression();
				lambdaExpression.setParentheses(false);
				ListRewrite lambdaExpressionParameterListRewrite = astRewrite.getListRewrite(lambdaExpression,
						LambdaExpression.PARAMETERS_PROPERTY);
				lambdaExpressionParameterListRewrite.insertFirst(lambdaParameter, null);
				lambdaExpression.setBody(statementCopy);

				/*
				 * create method invocation java.util.stream.Stream::forEach on
				 * the previously created stream method invocation with a single
				 * lambda expression as parameter
				 */
				SimpleName forEachMethodName = astRewrite.getAST()
					.newSimpleName("forEach"); //$NON-NLS-1$

				MethodInvocation forEachMethodInvocation = astRewrite.getAST()
					.newMethodInvocation();
				forEachMethodInvocation.setExpression(expressionCopy);
				forEachMethodInvocation.setName(forEachMethodName);
				ListRewrite forEachMethodInvocationArgumentsListRewrite = astRewrite
					.getListRewrite(forEachMethodInvocation, MethodInvocation.ARGUMENTS_PROPERTY);
				forEachMethodInvocationArgumentsListRewrite.insertFirst(lambdaExpression, null);

				/*
				 * replace enhanced for loop with newly created forEach method
				 * call, wrapped in an expression statement
				 */
				ExpressionStatement expressionStatement = astRewrite.getAST()
					.newExpressionStatement(forEachMethodInvocation);
				astRewrite.replace(enhancedForStatementNode, expressionStatement, null);
				saveComments(enhancedForStatementNode);
				addMarkerEvent(enhancedForStatementNode);
				onRewrite();
			}
		}
	}

	protected void saveComments(EnhancedForStatement node) {
		CommentRewriter commRewriter = getCommentRewriter();
		commRewriter.saveLeadingComment(node);
		commRewriter.saveCommentsInParentStatement(node.getExpression());
	}

	/**
	 * this method starts an instance of
	 * {@link StreamForEachCheckValidStatementASTVisitor} on the loop block and
	 * checks its validity.
	 * 
	 * @param statement
	 *            the body of the enhanced for loop
	 * @param parameter
	 *            the parameter of the enhanced for loop
	 * @return an {@link ASTNode} if the block is valid, null otherwise
	 */
	private ASTNode getApprovedStatement(Statement statement, SimpleName parameter) {
		if (ASTNode.BLOCK == statement.getNodeType()) {
			if (isStatementValid(statement, parameter)) {
				return statement;
			}
		} else if (ASTNode.EXPRESSION_STATEMENT == statement.getNodeType() && isStatementValid(statement, parameter)) {
			return ((ExpressionStatement) statement).getExpression();
		}

		return null;
	}

	/**
	 * Creates a method invocation for mapping to any of the predefined streams:
	 * 
	 * <ul>
	 * <li>{@link java.util.stream.IntStream}</li>
	 * <li>{@link java.util.stream.LongStream}</li>
	 * <li>{@link java.util.stream.DoubleStream}</li>
	 * </ul>
	 * 
	 * if the given primitive type is either an {@code int}, {@code long} or
	 * {@code double}.
	 * 
	 * @param parameterType
	 *            represents the type of iterating index of the loop.
	 * @param expression
	 *            represents the collection which is being iterated.
	 * 
	 * @return {@code expression.stream().mapToInt/Long/Doulbe(Integer/Long/Doulbe::valueOf)}
	 *         or {@code null} if the given type is neither {@code int},
	 *         {@code long} nor {@code double}.
	 */
	private MethodInvocation calcMappingMethod(PrimitiveType parameterType, Expression expression) {
		ITypeBinding binding = parameterType.resolveBinding();
		MethodInvocation methodInvocation = null;
		ExpressionMethodReference expMethodReference = null;

		if (binding == null) {
			return null;
		}

		String methodName;
		String primitiveName = binding.getName();
		String expMethRefName = VALUE_OF;
		Class<? extends Number> boxedClass;

		switch (primitiveName) {
		case "int": //$NON-NLS-1$
			methodName = MAP_TO_INT;
			boxedClass = Integer.class;
			break;
		case "long": //$NON-NLS-1$
			methodName = MAP_TO_LONG;
			boxedClass = Long.class;
			break;
		case "double": //$NON-NLS-1$
			methodName = MAP_TO_DOUBLE;
			boxedClass = Double.class;
			break;
		default:
			methodName = ""; //$NON-NLS-1$
			boxedClass = null;
		}

		if (StringUtils.isEmpty(methodName) || boxedClass == null) {
			return null;
		}

		AST ast = astRewrite.getAST();

		MethodInvocation streamMethodInvocation = astRewrite.getAST()
			.newMethodInvocation();
		streamMethodInvocation.setName(ast.newSimpleName(STREAM));
		streamMethodInvocation.setExpression(expression);

		methodInvocation = ast.newMethodInvocation();
		methodInvocation.setName(ast.newSimpleName(methodName));
		methodInvocation.setExpression(streamMethodInvocation);

		expMethodReference = ast.newExpressionMethodReference();
		expMethodReference.setName(ast.newSimpleName(expMethRefName));
		expMethodReference.setExpression(ast.newSimpleName(boxedClass.getSimpleName()));

		addRequiredImport(boxedClass.getName());

		ListRewrite miRewrite = astRewrite.getListRewrite(methodInvocation, MethodInvocation.ARGUMENTS_PROPERTY);
		miRewrite.insertFirst(expMethodReference, null);

		return methodInvocation;
	}

	/**
	 * Makes use of {@link ImportRewrite} to check whether an import statement
	 * is needed for the given qualified name.
	 * 
	 * @param qualifiedName
	 *            a string representing a qualified name.
	 */
	private void addRequiredImport(String qualifiedName) {
		ImportRewrite importRewrite = ImportRewrite.create(compilationUnit, true);
		importRewrite.addImport(qualifiedName);
		String[] addedImpots = importRewrite.getAddedImports();
		super.addAlreadyVerifiedImports(Arrays.asList(addedImpots));
	}

	/**
	 * @see {@link EnhancedForLoopToStreamForEachASTVisitor#getApprovedStatement(Statement, SimpleName)}
	 * 
	 * @param statement
	 * @param parameter
	 * @return
	 */
	private boolean isStatementValid(Statement statement, SimpleName parameter) {
		StreamForEachCheckValidStatementASTVisitor statementVisitor = new StreamForEachCheckValidStatementASTVisitor(
				statement.getParent(), parameter);
		statement.accept(statementVisitor);
		return statementVisitor.isStatementsValid();
	}
}

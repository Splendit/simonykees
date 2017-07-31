package at.splendit.simonykees.core.visitor.lambdaForEach;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.sub.LocalVariableUsagesASTVisitor;

/**
 * Extracts, if possible, a part of the body of the lambda expression occurring
 * as a consumer of a {@link Stream#forEach(Consumer)} and handles it by using a
 * {@link Stream#map(Function)} instead. For example, the following code:
 * 
 * <pre>
 * <code>
 * 		list.stream().filter(s -> !s.isEmpty()).forEach(s -> {
 *			int i = 10;
 *			String subString = s.substring(1) + i;
 * 			String lower = subString.toLowerCase();
 * 			sb.append(lower);
 * 		});
 * </code>
 * </pre>
 * 
 * will be transformed into
 * 
 * <pre>
 * <code> 
 * 		list.stream().filter(s -> !s.isEmpty()).map((s) -> {
 * 			int i = 10;
 * 			return s.substring(1) + i;
 * 		}).
 * 		forEach(subString -> {
 *			String lower = subString.toLowerCase();
 *			sb.append(lower);
 *		});
 * </code>
 * </pre>
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class LambdaForEachMapASTVisitor extends AbstractLambdaForEachASTVisitor {

	private static final String STREAM_MAP_METHOD_NAME = "map"; //$NON-NLS-1$
	private static final String STREAM_MAP_TO_INT_METHOD_NAME = "mapToInt"; //$NON-NLS-1$
	private static final String STREAM_MAP_TO_LONG_METHOD_NAME = "mapToLong"; //$NON-NLS-1$
	private static final String STREAM_MAP_TO_DOUBLE_METHOD_NAME = "mapToDouble"; //$NON-NLS-1$
	private static final String COLLECTION_FULLY_QUALIFIED_NAME = java.util.Collection.class.getName();
	private static final String STREAM_METHOD_NAME = "stream"; //$NON-NLS-1$
	private static final String PARALLEL_STREAM_METHOD_NAME = "parallelStream"; //$NON-NLS-1$

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (isStreamForEachInvocation(methodInvocation) && !isStreamOfRawList(methodInvocation)) {
			List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
			if (arguments.size() == 1 && ASTNode.LAMBDA_EXPRESSION == arguments.get(0).getNodeType()) {
				LambdaExpression lambdaExpression = (LambdaExpression) arguments.get(0);
				SimpleName parameter = extractSingleParameter(lambdaExpression);
				Block body = extractLambdaExpressionBlockBody(lambdaExpression);
				if (body != null) {
					/*
					 * use the analyzer for checking for extractable part in the
					 * forEach
					 */
					ForEachBodyAnalyzer analyzer = new ForEachBodyAnalyzer(parameter, body);
					if (analyzer.foundExtractableMapStatement()) {
						// get the extractable information from analyzer
						ASTNode extractableBlock = analyzer.getExtractableBlock();
						ASTNode remainingBlock = analyzer.getRemainingBlock();
						SimpleName newForEachParamName = analyzer.getNewForEachParameterName();

						// introduce a Stream::map
						Expression streamExpression = methodInvocation.getExpression();
						AST ast = methodInvocation.getAST();
						MethodInvocation mapInvocation = ast.newMethodInvocation();
						mapInvocation.setName(ast.newSimpleName(analyzer.getMappingMethodName()));
						mapInvocation.setExpression((Expression) astRewrite.createCopyTarget(streamExpression));

						ListRewrite argumentsPropertyRewriter = astRewrite.getListRewrite(mapInvocation,
								MethodInvocation.ARGUMENTS_PROPERTY);
						LambdaExpression mapExpression = genereateLambdaExpression(ast, parameter, extractableBlock,
								lambdaExpression);
						argumentsPropertyRewriter.insertFirst(mapExpression, null);

						/*
						 * replace the existing stream expression with the new
						 * one having the introduced map method in the tail
						 */
						astRewrite.replace(streamExpression, mapInvocation, null);

						// replace the body of the forEach with the new body
						astRewrite.replace(body, remainingBlock, null);

						/*
						 * replace the parameter of the forEach lambda
						 * expression
						 */
						astRewrite.replace(parameter, newForEachParamName, null);

						/*
						 * Replace the type of the parameter if any
						 */
						Type type = extractSingleParameterType(lambdaExpression);
						if (type != null) {
							Type newType = analyzer.getNewForEachParameterType();
							if (newType.isPrimitiveType()) {
								/*
								 * implicit boxing! primitives are not allowed
								 * in forEach
								 */
								astRewrite.replace((ASTNode) lambdaExpression.parameters().get(0), newForEachParamName,
										null);
							} else {
								astRewrite.replace(type, newType, null);
								Modifier modifier = analyzer.getNewForEachParameterModifier();
								insertModifier(lambdaExpression, modifier);
							}
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Inserts the modifier to the parameter of the lambda expression if it has
	 * only one parameter represented with a {@link SingleVariableDeclaration}.
	 * 
	 * @param lambdaExpression
	 *            a node representing a lambda expression
	 * @param modifier
	 *            the modifier to be inserted
	 */
	private void insertModifier(LambdaExpression lambdaExpression, Modifier modifier) {
		if (modifier != null) {
			List<SingleVariableDeclaration> params = ASTNodeUtil.convertToTypedList(lambdaExpression.parameters(),
					SingleVariableDeclaration.class);
			if (params.size() == 1) {
				SingleVariableDeclaration param = params.get(0);
				ListRewrite paramRewriter = astRewrite.getListRewrite(param,
						SingleVariableDeclaration.MODIFIERS2_PROPERTY);
				paramRewriter.insertFirst(astRewrite.createCopyTarget(modifier), null);
			}
		}
	}

	/**
	 * Checks whether the expression of the method invocation is a stream
	 * generated from a raw collection.
	 * 
	 * @param methodInvocation
	 *            to be checked
	 * 
	 * @return {@code true} the expression of the method invocation is a stream
	 *         generated from a raw collection, or {@code false} otherwise.
	 */
	private boolean isStreamOfRawList(MethodInvocation methodInvocation) {
		Expression expression = methodInvocation.getExpression();
		StreamInvocationVisitor visitor = new StreamInvocationVisitor();
		expression.accept(visitor);
		MethodInvocation streamInvocation = visitor.getStreamInvocation();
		if (streamInvocation != null) {
			Expression iterableExpression = streamInvocation.getExpression();
			if (iterableExpression != null) {
				ITypeBinding iterableTypeBinding = iterableExpression.resolveTypeBinding();
				if (iterableTypeBinding.isRawType()) {
					return true;
				}

			}
		}
		return false;
	}

	/**
	 * Creates a new lambda expression with the given parameter name and the
	 * body.
	 * 
	 * @param ast
	 *            the ast where the new lambda expression belongs to
	 * @param paramName
	 *            name of the parameter
	 * @param body
	 *            the body of the new lambda expression
	 * 
	 * @return the generated {@link LambdaExpression}.
	 */
	private LambdaExpression genereateLambdaExpression(AST ast, SimpleName paramName, ASTNode body,
			LambdaExpression original) {
		/*
		 * A workaround for keeping the formatting of the original lambda
		 * expression.
		 */
		LambdaExpression lambdaExpression = (LambdaExpression) ASTNode.copySubtree(ast, original);
		lambdaExpression.setBody(body);
		return lambdaExpression;
	}

	/**
	 * Checks if the body of the lambda expression is a block, and extracts it.
	 * 
	 * @param lambdaExpression
	 *            lambda expression to check for.
	 * 
	 * @return the {@link Block} representing the body of the lambda expression,
	 *         or {@code null} if its is not a block.
	 */
	private Block extractLambdaExpressionBlockBody(LambdaExpression lambdaExpression) {
		ASTNode body = lambdaExpression.getBody();
		if (ASTNode.BLOCK == body.getNodeType()) {
			return (Block) body;
		}
		return null;
	}

	/**
	 * Extracts the {@link Type} of the parameter of the lambda expression, if
	 * any.
	 * 
	 * @param lambdaExpression
	 *            lambda expression to be checked
	 * 
	 * @return the {@link Type} of the parameter if the lambda expression has
	 *         only one parameter expressed as a
	 *         {@link SingleVariableDeclaration}, or {@code null} otherwise.
	 */
	private Type extractSingleParameterType(LambdaExpression lambdaExpression) {
		Type parameter = null;

		List<SingleVariableDeclaration> declarations = ASTNodeUtil.returnTypedList(lambdaExpression.parameters(),
				SingleVariableDeclaration.class);
		if (declarations.size() == 1) {
			SingleVariableDeclaration declaration = declarations.get(0);
			parameter = declaration.getType();
		}

		return parameter;
	}

	/**
	 * A helper class for analyzing the block body of a lambda expression.
	 * 
	 * @author Ardit Ymeri
	 * @since 1.2
	 *
	 */
	class ForEachBodyAnalyzer {

		private ASTNode extractableBlock;
		private ASTNode remainingBlock;
		private List<SimpleName> declaredNames = new ArrayList<>();
		private List<Statement> extractableStatements = new ArrayList<>();
		private List<Statement> remainingStatements = new ArrayList<>();
		private Expression mapExpression = null;
		private SimpleName newForEachVarName = null;
		private Type parameterType = null;
		private Modifier modifier;
		private boolean primitiveTarget = false;
		private String mappingMethodName = STREAM_MAP_METHOD_NAME;

		public ForEachBodyAnalyzer(SimpleName parameter, Block block) {
			List<Statement> statements = ASTNodeUtil.returnTypedList(block.statements(), Statement.class);
			boolean mapVariableFound = false;
			AST ast = block.getAST();

			for (Statement statement : statements) {

				if (!mapVariableFound) {

					// search for a map variable
					if (ASTNode.VARIABLE_DECLARATION_STATEMENT == statement.getNodeType()) {
						/*
						 * only variable declaration statements can introduce a
						 * map variable
						 */
						VariableDeclarationStatement declStatement = (VariableDeclarationStatement) statement;

						// skip the variable declarations having an annotation
						if (ASTNodeUtil.convertToTypedList(declStatement.modifiers(), Annotation.class).isEmpty()) {
							List<VariableDeclarationFragment> fragments = ASTNodeUtil
									.convertToTypedList(declStatement.fragments(), VariableDeclarationFragment.class);
							Type type = declStatement.getType();

							if (!involvesUndefinedTypes(type.resolveBinding()) && !declStatement.getType().isArrayType()
									&& referencesName(declStatement, parameter)) {
								if (fragments.size() == 1) {
									/*
									 * a map variable is found. store its name
									 * and its initializer
									 */
									VariableDeclarationFragment fragment = fragments.get(0);
									SimpleName fragmentName = fragment.getName();
									Expression initializer = fragment.getInitializer();
									/*
									 * FIXME: SIM-521
									 */
									if (!isDerivableInitializerType(initializer)) {
										ITypeBinding mappingVarBinding = type.resolveBinding();
										if (mappingVarBinding != null && mappingVarBinding.isPrimitive()) {
											this.primitiveTarget = true;
											this.mappingMethodName = calcMappingMethodName(mappingVarBinding);

										}
										mapVariableFound = true;
										newForEachVarName = fragmentName;
										parameterType = declStatement.getType();
										mapExpression = initializer;

										List<Modifier> modifiers = ASTNodeUtil
												.convertToTypedList(declStatement.modifiers(), Modifier.class);
										if (modifiers.size() == 1) {
											this.modifier = modifiers.get(0);
										}
									}

								} else {
									/*
									 * if the parameter is not referenced, then
									 * store the declared name it will be
									 * checked for references after the map
									 * variable is found.
									 */
									extractableStatements.add(statement);
									for (VariableDeclarationFragment fragment : fragments) {
										SimpleName fragmentName = fragment.getName();
										declaredNames.add(fragmentName);
									}
								}
							} else {
								/*
								 * store the declared name. It will be checked
								 * for references after the map variable is
								 * found.
								 */
								extractableStatements.add(statement);
								for (VariableDeclarationFragment fragment : fragments) {
									SimpleName fragmentName = fragment.getName();
									declaredNames.add(fragmentName);
								}
							}
						}
					} else {
						ReturnStatementVisitor visitor = new ReturnStatementVisitor();
						statement.accept(visitor);
						if (visitor.hasReturnStatement()) {
							clearParameters();
							return;
						}
						extractableStatements.add(statement);
					}
				} else {
					if (referencesName(statement, parameter) || referencesNames(statement, declaredNames)) {
						clearParameters();
						return;
					}
					remainingStatements.add((Statement) statement);
				}
			}

			prepareRemainingBlock(ast);
			prepareExtractableBlock(ast);
		}

		/**
		 * Checks if the given type binding corresponds to either of the
		 * primitives: {@code int}, {@code long} or {@code double}, and if yes
		 * returns the corresponding method name which returns the respective
		 * stream type.
		 * 
		 * @param initializerBinding
		 *            type binding of the resulting stream type.
		 * 
		 * @return {@value #STREAM_MAP_METHOD_NAME} if the given type is not any
		 *         of the aforementioned types, or any of the following:
		 *         {@value #STREAM_MAP_TO_INT_METHOD_NAME},
		 *         {@value #STREAM_MAP_TO_DOUBLE_METHOD_NAME} or
		 *         {@value #STREAM_MAP_TO_LONG_METHOD_NAME} respectively for
		 *         {@code int}, {@code double} or {@code long} primitves.
		 */
		private String calcMappingMethodName(ITypeBinding initializerBinding) {
			if (initializerBinding.isPrimitive()) {
				String typeName = initializerBinding.getQualifiedName();
				switch (typeName) {
				case "int": //$NON-NLS-1$
					return STREAM_MAP_TO_INT_METHOD_NAME;
				case "double": //$NON-NLS-1$
					return STREAM_MAP_TO_DOUBLE_METHOD_NAME;
				case "long": //$NON-NLS-1$
					return STREAM_MAP_TO_LONG_METHOD_NAME;
				default:
					return STREAM_MAP_METHOD_NAME;
				}
			}
			return STREAM_MAP_METHOD_NAME;
		}

		/**
		 * Checks if the expression is a generic method invocation without type
		 * arguments.
		 * 
		 * @param expression
		 *            expression to be checked
		 * @return {@code true} if expression is a method invocation without
		 *         type arguments, {@code false} otherwise.
		 */
		private boolean isDerivableInitializerType(Expression expression) {
			if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
				MethodInvocation methodInvocation = (MethodInvocation) expression;
				IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
				if (methodBinding.isParameterizedMethod() && methodInvocation.typeArguments().isEmpty()) {
					return true;
				}
			}

			return false;
		}

		public Modifier getNewForEachParameterModifier() {
			return this.modifier;
		}

		private void clearParameters() {
			extractableBlock = null;
			remainingBlock = null;
			newForEachVarName = null;
		}

		public SimpleName getNewForEachParameterName() {
			return newForEachVarName;
		}

		public Type getNewForEachParameterType() {
			return this.parameterType;
		}

		/**
		 * An indicator for showing whether all parameters are found for
		 * extracting a part of the {@code Stream::forEach} to a
		 * {@code Stream::map}.
		 * 
		 * @return
		 */
		public boolean foundExtractableMapStatement() {
			return extractableBlock != null && remainingBlock != null && newForEachVarName != null
					&& mapExpression != null;
		}

		/**
		 * Creates the body of the lambda expression to be used in the
		 * introduced {@code Stream::map}
		 * 
		 * @param ast
		 *            either a {@link Block} or a {@link Expression}
		 */
		private void prepareExtractableBlock(AST ast) {
			if (!this.extractableStatements.isEmpty() && mapExpression != null) {
				Block block = ast.newBlock();
				ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);

				for (Statement statement : this.extractableStatements) {
					listRewrite.insertLast((Statement) astRewrite.createCopyTarget(statement), null);
				}

				ReturnStatement returnStatement = ast.newReturnStatement();
				returnStatement.setExpression((Expression) astRewrite.createCopyTarget(mapExpression));
				listRewrite.insertLast(returnStatement, null);

				this.extractableBlock = block;
			} else if (mapExpression != null) {
				this.extractableBlock = astRewrite.createCopyTarget(mapExpression);
			}
		}

		/**
		 * Creates a the body of the lambda expression in the forEach after
		 * extracting the part to be placed in {@code Stream::map}.
		 * 
		 * @param ast
		 *            either a {@link Block} or a single {@link Expression} if
		 *            the remaining block has only one expression.
		 */
		private void prepareRemainingBlock(AST ast) {
			ASTNode block;
			if (this.remainingStatements.size() == 1
					&& ASTNode.EXPRESSION_STATEMENT == remainingStatements.get(0).getNodeType()) {
				Expression expression = ((ExpressionStatement) remainingStatements.get(0)).getExpression();
				block = astRewrite.createCopyTarget(expression);

			} else {
				block = ast.newBlock();
				ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
				for (Statement statement : this.remainingStatements) {
					listRewrite.insertLast(astRewrite.createCopyTarget(statement), null);
				}
			}

			this.remainingBlock = block;
		}

		public Expression getMapExpression() {
			return this.mapExpression;
		}

		public ASTNode getExtractableBlock() {
			return this.extractableBlock;
		}

		public ASTNode getRemainingBlock() {
			return this.remainingBlock;
		}

		private boolean referencesNames(Statement statement, List<SimpleName> declaredNames2) {
			for (SimpleName simpleName : declaredNames2) {
				if (referencesName(statement, simpleName)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Checks whether there is a reference of the variable with the given
		 * simple name in the code represented by the given ast node.
		 * 
		 * @param node
		 *            node to look for
		 * @param simpleName
		 *            name of the variable to look for
		 * 
		 * @return {@code true} if there is a reference of the given simple name
		 *         in the node, and {@code false otherwise}
		 */
		private boolean referencesName(ASTNode node, SimpleName simpleName) {
			LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(simpleName);
			node.accept(visitor);
			return !visitor.getUsages().isEmpty();
		}

		public boolean isPrimitiveTarget() {
			return this.primitiveTarget;
		}

		public String getMappingMethodName() {
			return mappingMethodName;
		}
	}

	/**
	 * A visitor searching for return statements.
	 *
	 */
	class ReturnStatementVisitor extends ASTVisitor {
		ReturnStatement returnStatement = null;

		@Override
		public boolean visit(ReturnStatement returnStatement) {
			this.returnStatement = returnStatement;
			return true;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			// stop searching if a return statement is already found
			return returnStatement == null;
		}

		public boolean hasReturnStatement() {
			return this.returnStatement != null;
		}
	}

	/**
	 * 
	 * A visitor for finding the first invocation of {@link Collection#stream()}
	 * or {@link Collection#parallelStream()} method.
	 *
	 */
	class StreamInvocationVisitor extends ASTVisitor {

		private MethodInvocation streamInvocation = null;

		@Override
		public boolean preVisit2(ASTNode node) {
			return streamInvocation == null;
		}

		@Override
		public boolean visit(MethodInvocation methodInvocation) {
			if (methodInvocation.getName().getIdentifier().equals(STREAM_METHOD_NAME)
					|| methodInvocation.getName().getIdentifier().equals(PARALLEL_STREAM_METHOD_NAME)) {
				IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
				if (ClassRelationUtil.isContentOfTypes(methodBinding.getDeclaringClass(),
						Collections.singletonList(COLLECTION_FULLY_QUALIFIED_NAME))) {
					streamInvocation = methodInvocation;
				}
			}

			return true;
		}

		public MethodInvocation getStreamInvocation() {
			return streamInvocation;
		}
	}

	/**
	 * Checks if the given type is a type variable or involves a type variable
	 * as a parameter.
	 * 
	 * @param type
	 *            a type to be checked
	 * 
	 * @return {@code true} if the type involves a type variable, or
	 *         {@code false} otherwise.
	 */
	public boolean involvesUndefinedTypes(ITypeBinding type) {

		if (type.isParameterizedType()) {
			ITypeBinding[] arguments = type.getTypeArguments();
			for (ITypeBinding argument : arguments) {
				if (argument.isParameterizedType()) {
					// recursive call
					return involvesUndefinedTypes(argument);
				}

				ITypeBinding typeDeclaration = argument.getTypeDeclaration();
				if (typeDeclaration.isTypeVariable()) {
					return true;
				}

				if (argument.isRawType() || argument.isWildcardType() || argument.isCapture()) {
					return true;
				}
			}
		} else if (type.isTypeVariable() || type.isRawType() || type.isWildcardType() || type.isCapture()) {
			return true;
		}

		return false;
	}
}
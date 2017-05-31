package at.splendit.simonykees.core.visitor.lambdaForEach;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.visitor.sub.LocalVariableUsagesASTVisitor;

/**
 * Extracts, if possible, a part of the body of the lambda expression occurring as a
 * consumer of a {@link Stream#forEach(Consumer)} and handles it
 * by using a {@link Stream#map(Function)} instead. 
 * For example, the following code:
 * <pre>
 * <code> {@code 
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
 * <code> {@code 
 * 		list.stream().filter(s -> !s.isEmpty()).map((s)->{int i = 10;return s.substring(1) + i;}).forEach(subString -> {
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

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (isStreamForEachInvocation(methodInvocation)) {
			List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
			if (arguments.size() == 1 && ASTNode.LAMBDA_EXPRESSION == arguments.get(0).getNodeType()) {
				LambdaExpression lambdaExpression = (LambdaExpression) arguments.get(0);
				SimpleName parameter = extractSingleParameter(lambdaExpression);
				Block body = extractLambdaExpressionBlockBody(lambdaExpression);
				if (body != null) {
					// use the analyzer for checking for extractable part in the forEach
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
						mapInvocation.setName(ast.newSimpleName(STREAM_MAP_METHOD_NAME));
						mapInvocation.setExpression((Expression) astRewrite.createCopyTarget(streamExpression));
						
						ListRewrite argumentsPropertyRewriter = astRewrite.getListRewrite(mapInvocation,
								MethodInvocation.ARGUMENTS_PROPERTY);
						LambdaExpression mapExpression = genereateLambdaExpression(ast, parameter, extractableBlock);
						argumentsPropertyRewriter.insertFirst(mapExpression, null);

						/*
						 * replace the existing stream expression with the new one having the 
						 * introduced map method in the tail
						 */
						astRewrite.replace(streamExpression, mapInvocation, null);
						
						//replace the body of the forEach with the new body
						astRewrite.replace(body, remainingBlock, null);

						// replace the parameter of the forEach lambda expression
						astRewrite.replace(parameter, newForEachParamName, null);
					}
				}
			}
		}
		return true;
	}

	/**
	 * Creates a new lambda expression with the given parameter name and the body.
	 * 
	 * @param ast the ast where the new lambda expression belongs to
	 * @param paramName name of the parameter
	 * @param body the body of the new lambda expression
	 * 
	 * @return the generated {@link LambdaExpression}.
	 */
	private LambdaExpression genereateLambdaExpression(AST ast, SimpleName paramName, ASTNode body) {
		LambdaExpression lambdaExpression = ast.newLambdaExpression();
		lambdaExpression.setBody(body);
		ListRewrite listRewrite = astRewrite.getListRewrite(lambdaExpression, LambdaExpression.PARAMETERS_PROPERTY);
		listRewrite.insertFirst((SimpleName) astRewrite.createCopyTarget(paramName), null);

		return lambdaExpression;
	}

	private Block extractLambdaExpressionBlockBody(LambdaExpression lambdaExpression) {
		ASTNode body = lambdaExpression.getBody();
		if (ASTNode.BLOCK == body.getNodeType()) {
			return (Block) body;
		}
		return null;
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

		public ForEachBodyAnalyzer(SimpleName parameter, Block block) {
			List<Statement> statements = ASTNodeUtil.returnTypedList(block.statements(), Statement.class);
			boolean mapVariableFound = false;
			AST ast = block.getAST();

			for (Statement statement : statements) {
				
				if (!mapVariableFound) {
					//search for a map variable
					
					if (ASTNode.VARIABLE_DECLARATION_STATEMENT == statement.getNodeType()) {
						// only variable declaration statements can introduce a map variable
						VariableDeclarationStatement declStatement = (VariableDeclarationStatement) statement;
						List<VariableDeclarationFragment> fragments = ASTNodeUtil
								.convertToTypedList(declStatement.fragments(), VariableDeclarationFragment.class);
						if (referencesName(declStatement, parameter)) {
							if (fragments.size() == 1) {
								// a map variable is found. store its name and its initializer
								VariableDeclarationFragment fragment = fragments.get(0);
								SimpleName fragmentName = fragment.getName();
								Expression initializer = fragment.getInitializer();
								mapVariableFound = true;
								newForEachVarName = fragmentName;
								mapExpression = initializer;

							} else {
								/*
								 * only one fragment is allowed in the declaration of the map variable.
								 * As an improvement, the fragments can be split into separate declarations.
								 */
								clearParameters();
								return;
							}
						} else {
							/*
							 * if the parameter is not referenced, then just store the declared name
							 * it will be checked for references after the map variable is found.
							 */
							extractableStatements.add(statement);
							for (VariableDeclarationFragment fragment : fragments) {
								SimpleName fragmentName = fragment.getName();
								declaredNames.add(fragmentName);
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
					if (referencesNames(statement, declaredNames)) {
						clearParameters();
						return;
					}
					remainingStatements.add((Statement) statement);
				}
			}

			prepareRemainingBlock(ast);
			prepareExtractableBlock(ast);
		}

		private void clearParameters() {
			extractableBlock = null;
			remainingBlock = null;
			newForEachVarName = null;
		}

		public SimpleName getNewForEachParameterName() {
			return newForEachVarName;
		}

		/**
		 * An indicator for showing whether all parameters are found for
		 * extracting a part of the {@code Stream::forEach} to a {@code Stream::map}. 
		 * 
		 * @return
		 */
		public boolean foundExtractableMapStatement() {
			return extractableBlock != null && remainingBlock != null && newForEachVarName != null && mapExpression != null;
		}

		/**
		 * Creates the body of the lambda expression to be used in the introduced {@code Stream::map}
		 * 
		 * @param ast either a {@link Block} or a {@link Expression}
		 */ 
		private void prepareExtractableBlock(AST ast) {
			if (!this.extractableStatements.isEmpty() && mapExpression != null) {
				Block block = ast.newBlock();
				ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);

				for (Statement statement : this.extractableStatements) {
					listRewrite.insertLast((Statement)astRewrite.createCopyTarget(statement), null);
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
		 * Creates a the body of the lambda expression in the forEach after extracting 
		 * the part to be placed in {@code Stream::map}. 
		 * 
		 * @param ast either a {@link Block} or a single {@link Expression} if the remaining block
		 * has only one expression.
		 */
		private void prepareRemainingBlock(AST ast) {
			ASTNode block;
			if(this.remainingStatements.size() == 1 && ASTNode.EXPRESSION_STATEMENT == remainingStatements.get(0).getNodeType()) {
				Expression expression = ((ExpressionStatement)remainingStatements.get(0)).getExpression();
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
		 * Checks whether there is a reference of the variable with the given simple name
		 * in the code represented by the given ast node. 
		 * 
		 * @param node node to look for
		 * @param simpleName name of the variable to look for
		 * 
		 * @return {@code true} if there is a reference of the given simple name in the node, and {@code false otherwise}
		 */
		private boolean referencesName(ASTNode node, SimpleName simpleName) {
			LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(simpleName);
			node.accept(visitor);
			return !visitor.getUsages().isEmpty();
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

}
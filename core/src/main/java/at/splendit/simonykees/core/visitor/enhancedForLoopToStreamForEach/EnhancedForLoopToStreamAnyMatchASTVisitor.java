package at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * Analyzes the occurrences of {@link EnhancedForStatement}s the check whether a
 * transformation to {@link Stream#anyMatch(java.util.function.Predicate)} is
 * possible. Considers two cases:
 * 
 * <ul>
 * <li>The for loop is only used to for instantiating a {@code boolean}
 * variable. For example:
 * 
 * <pre>
 * <code>
 * 		boolean containsEmpty = false;
 *		for(String value : strings) {
 *			if(value.isEmpty()) {
 *				containsEmpty = true;
 *				break;
 *			}
 *		}
 * </code>
 * </pre>
 * 
 * is transformed into:
 * 
 * <pre>
 * {
 * 	&#64;code
 * 	boolean containsEmpty = strings.stream().anyMatch(value -> value.isEmpty());
 * }
 * </pre>
 * 
 * </li>
 * <li>The for loop is only used for calculating a {@code boolean} value to be
 * returned. For example:
 * 
 * <pre>
 * <code>
 *		for(String value : strings) {
 *			if(value.isEmpty()) {
 *				return true;
 *			}
 *		}
 *		return false;
 * </code>
 * </pre>
 * 
 * is transformed into:
 * 
 * <pre>
 * {@code
 * 	return strings.stream().anyMatch(value -> value.isEmpty());
 * }
 * </pre>
 * 
 * </li>
 * 
 * </ul>
 * 
 * @author Ardit Ymeri
 * @since 2.0.2
 *
 */
public class EnhancedForLoopToStreamAnyMatchASTVisitor extends AbstractEnhancedForLoopToStreamASTVisitor {

	private static final String ANY_MATCH = "anyMatch"; //$NON-NLS-1$

	@Override
	public boolean visit(EnhancedForStatement enhancedForStatement) {

		SingleVariableDeclaration enhancedForParameter = enhancedForStatement.getParameter();
		Expression enhancedForExp = enhancedForStatement.getExpression();
		ITypeBinding expressionBinding = enhancedForExp.resolveTypeBinding();
		List<String> expressionBindingList = Collections.singletonList(Collection.class.getName());
		// the expression of the loop should be a subtype of a collection
		if (expressionBinding == null
				|| (!ClassRelationUtil.isInheritingContentOfTypes(expressionBinding, expressionBindingList)
						&& !ClassRelationUtil.isContentOfTypes(expressionBinding, expressionBindingList))) {
			return true;
		}

		ITypeBinding parameterTypeBinding = enhancedForParameter.getType().resolveBinding();
		if (!isTypeSafe(parameterTypeBinding)) {
			return true;
		}
		
		List<Statement> bodyStatements = new ArrayList<>();

		/*
		 *  the body of the loop should either be a block or a single if statement
		 */
		Statement body = enhancedForStatement.getBody();
		if (ASTNode.BLOCK == body.getNodeType()) {
			bodyStatements = ASTNodeUtil.returnTypedList(((Block) body).statements(), Statement.class);
		} else if(ASTNode.IF_STATEMENT == body.getNodeType()) {
			bodyStatements.add(body);
		} else {
			return true;
		}


		// the loop body should consist of only one 'if' statement.
		if (bodyStatements.size() != 1) {
			return true;
		}

		Statement statement = bodyStatements.get(0);
		if (ASTNode.IF_STATEMENT != statement.getNodeType()) {
			return true;
		}

		IfStatement ifStatement = (IfStatement) statement;

		// the if statement should have no else branch
		if (ifStatement.getElseStatement() != null) {
			return true;
		}

		/*
		 * the condition expression should not contain non effectively final
		 * variables and should not throw any exception
		 */
		Expression ifCondition = ifStatement.getExpression();
		if (containsNonEffectivelyFinalVariable(ifCondition) || throwsException(ifCondition)) {
			return true;
		}
		
		

		Statement thenStatement = ifStatement.getThenStatement();
		VariableDeclarationFragment booleanDeclFragment;
		ReturnStatement returnStatement;

		if ((booleanDeclFragment = isAssignmentAndBreakBlock(thenStatement, enhancedForStatement)) != null) {
			/*
			 * replace initialization of the boolean variable with
			 * Stream::anyMatch
			 */
			MethodInvocation methodInvocation = createStreamAnymatchInitalizer(enhancedForExp, ifCondition,
					enhancedForParameter);
			astRewrite.replace(booleanDeclFragment.getInitializer(), methodInvocation, null);
			replaceLoopWithFragment(enhancedForStatement, booleanDeclFragment);

		} else if ((returnStatement = isAssignmentAndReturnBlock(thenStatement, enhancedForStatement)) != null) {
			// replace the return statement with a Stream::AnyMatch
			MethodInvocation methodInvocation = createStreamAnymatchInitalizer(enhancedForExp, ifCondition,
					enhancedForParameter);
			astRewrite.replace(returnStatement.getExpression(), methodInvocation, null);
			astRewrite.remove(enhancedForStatement, null);
		}

		return true;
	}

	private boolean throwsException(Expression ifCondition) {
		UnhandledExceptionVisitor visitor = new UnhandledExceptionVisitor();
		ifCondition.accept(visitor);
		return visitor.throwsException();
	}

	/**
	 * Checks whether a reference of a non effectively final variable is made on
	 * the code represented by the given node. Makes use of
	 * {@link EffectivelyFinalVisitor}.
	 * 
	 * @param astNode
	 *            a node representing a code snippet.
	 * @return {@code true} if the code references an non effectively final
	 *         variable or {@code false} otherwise.
	 */
	private boolean containsNonEffectivelyFinalVariable(ASTNode astNode) {
		EffectivelyFinalVisitor analyzer = new EffectivelyFinalVisitor();
		astNode.accept(analyzer);
		return analyzer.containsNonEffectivelyFinalVariable();
	}

	/**
	 * Replaces an {@link EnhancedForStatement} with a
	 * {@link VariableDeclarationStatement} containing a single
	 * {@link VariableDeclarationFragment}. The rest of the declaration
	 * fragments which may occur in the same statement as the given declaration
	 * fragment, are not changed.
	 * 
	 * @param enhancedForStatement
	 *            the statement to be replaced
	 * @param booleanDeclFragment
	 *            the fragment of the declaration statement to be used as a
	 *            replacer.
	 */
	private void replaceLoopWithFragment(EnhancedForStatement enhancedForStatement,
			VariableDeclarationFragment booleanDeclFragment) {
		ASTNode fragmentParent = booleanDeclFragment.getParent();
		if (ASTNode.VARIABLE_DECLARATION_STATEMENT == fragmentParent.getNodeType()) {
			VariableDeclarationStatement declStatement = (VariableDeclarationStatement) fragmentParent;
			List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(declStatement.fragments(),
					VariableDeclarationFragment.class);
			if (fragments.size() == 1) {
				astRewrite.replace(enhancedForStatement, astRewrite.createMoveTarget(declStatement), null);
			} else {
				AST ast = astRewrite.getAST();
				VariableDeclarationStatement newBoolDeclStatement = ast.newVariableDeclarationStatement(
						(VariableDeclarationFragment) astRewrite.createMoveTarget(booleanDeclFragment));
				newBoolDeclStatement.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
				astRewrite.replace(enhancedForStatement, newBoolDeclStatement, null);
			}
		}
	}

	/**
	 * Creates an invocation of
	 * {@link Stream#anyMatch(java.util.function.Predicate)} which is equivalent
	 * to a {@link EnhancedForStatement}.
	 * 
	 * @param enhancedForExp
	 *            the expression of a {@link EnhancedForStatement}.
	 * @param ifCondition
	 *            the expression of a {@link IfStatement} which is the only
	 *            statement occurring in the body of a
	 *            {@link EnhancedForStatement}.
	 * @param param
	 *            the variable declaration occurring as a parameter in the
	 *            {@link EnhancedForStatement}
	 * @return a method invocation of the form: <code>
	 *         enhancedForExp.stream().anyMatch(param -> ifCondition) <code>
	 */
	private MethodInvocation createStreamAnymatchInitalizer(Expression enhancedForExp, Expression ifCondition,
			SingleVariableDeclaration param) {
		AST ast = astRewrite.getAST();
		MethodInvocation stream = ast.newMethodInvocation();
		stream.setName(ast.newSimpleName(STREAM));
		stream.setExpression(createExpressionForStreamMethodInvocation(enhancedForExp));

		MethodInvocation anyMatch = ast.newMethodInvocation();
		anyMatch.setName(ast.newSimpleName(ANY_MATCH));
		anyMatch.setExpression(stream);

		LambdaExpression anyMatchCondition = ast.newLambdaExpression();
		anyMatchCondition.setBody(astRewrite.createMoveTarget(ifCondition));
		ListRewrite lambdaRewrite = astRewrite.getListRewrite(anyMatchCondition, LambdaExpression.PARAMETERS_PROPERTY);
		anyMatchCondition.setParentheses(false);
		VariableDeclarationFragment newDeclFragment = ast.newVariableDeclarationFragment();
		newDeclFragment.setName((SimpleName) astRewrite.createCopyTarget(param.getName()));
		lambdaRewrite.insertFirst(newDeclFragment, null);

		ListRewrite anyMatchParamRewrite = astRewrite.getListRewrite(anyMatch, MethodInvocation.ARGUMENTS_PROPERTY);
		anyMatchParamRewrite.insertFirst(anyMatchCondition, null);

		return anyMatch;
	}

	/**
	 * Checks whether the body of a <em>then statement</em> consists of a block of
	 * exactly two statements where the first one is an assignment to
	 * {@code true} of a boolean variable and the second one is a
	 * {@link BreakStatement}.
	 * 
	 * @param thenStatement
	 *            a node representing the 'then statement' of an
	 *            {@link IfStatement}.
	 * @param forNode
	 *            the enhanced for-loop containing the {@link IfStatement}.
	 * 
	 * @return the declaration fragment of the boolean variable which is
	 *         assigned in the given thenStatement or {@code null} if it is not
	 *         possible to transform the loop into an invocation of
	 *         {@link Stream#anyMatch(java.util.function.Predicate)}.
	 */
	private VariableDeclarationFragment isAssignmentAndBreakBlock(Statement thenStatement,
			EnhancedForStatement forNode) {
		if (ASTNode.BLOCK == thenStatement.getNodeType()) {
			List<Statement> thenBody = ASTNodeUtil.convertToTypedList(((Block) thenStatement).statements(),
					Statement.class);
			if (thenBody.size() == 2) {
				Statement stStatement = thenBody.get(0);
				Statement ndStatement = thenBody.get(1);
				if (ASTNode.BREAK_STATEMENT == ndStatement.getNodeType()
						&& ASTNode.EXPRESSION_STATEMENT == stStatement.getNodeType()) {
					ExpressionStatement expressionStatement = (ExpressionStatement) stStatement;
					if (ASTNode.ASSIGNMENT == expressionStatement.getExpression().getNodeType()) {
						Assignment assignment = (Assignment) expressionStatement.getExpression();
						Expression lhs = assignment.getLeftHandSide();
						Expression rhs = assignment.getRightHandSide();
						if (ASTNode.BOOLEAN_LITERAL == rhs.getNodeType() && ASTNode.SIMPLE_NAME == lhs.getNodeType()) {
							if (((BooleanLiteral) rhs).booleanValue()) {
								SimpleName boolVarName = (SimpleName) lhs;
								return findBoolDeclFragment(boolVarName, forNode);
							}
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Makes use of {@link LoopWithBreakStatementAnalyzeVisitor} for finding the
	 * declaration fragment of the boolean variable that is assigned in the body
	 * of the given {@link EnhancedForStatement}.
	 * 
	 * @param boolVarName
	 *            the name of the boolean variable
	 * @param forNode
	 *            the loop used for assigning the boolean variable
	 * @return the declaration fragment of the boolean variable with the given
	 *         name, or {@code null} if the fragment cannot be found.
	 */
	private VariableDeclarationFragment findBoolDeclFragment(SimpleName boolVarName, EnhancedForStatement forNode) {
		ASTNode loopParent = forNode.getParent();
		if (ASTNode.BLOCK == loopParent.getNodeType()) {
			Block parentBlock = (Block) loopParent;
			LoopWithBreakStatementAnalyzeVisitor analyzer = new LoopWithBreakStatementAnalyzeVisitor(parentBlock,
					forNode, boolVarName);
			parentBlock.accept(analyzer);
			return analyzer.getDeclarationBoolFragment();
		}

		return null;
	}

	/**
	 * Checks whether the given thenStatement consists of a single {@link ReturnStatement} which 
	 * returns a boolean {@code true} value  and whether the given enhanced for-loop is followed 
	 * by a {@link ReturnStatement} which returns a boolean {@code false} value. 
	 * 
	 * @param thenStatement a node representing the 'then statement' of a {@link IfStatement}.
	 * @param forNode a loop having the aforementioned if statement as the only statement in the body.
	 * @return the {@link ReturnStatement} following the the given loop, or {@code null} if the loop 
	 * is not followed by a return statement or if the transformation is not possible. 
	 */
	private ReturnStatement isAssignmentAndReturnBlock(Statement thenStatement, EnhancedForStatement forNode) {
		List<Statement> thenBody = new ArrayList<>();
		
		if (ASTNode.BLOCK == thenStatement.getNodeType()) {
			thenBody = ASTNodeUtil.convertToTypedList(((Block) thenStatement).statements(),
					Statement.class); 
		} else if(ASTNode.RETURN_STATEMENT == thenStatement.getNodeType()) {
			thenBody.add(thenStatement);
		}
		
		if (thenBody.size() == 1) {
			Statement stStatement = thenBody.get(0);
			if (ASTNode.RETURN_STATEMENT == stStatement.getNodeType()) {
				ReturnStatement returnStatement = (ReturnStatement) stStatement;
				Expression returnedExpression = returnStatement.getExpression();
				if (returnedExpression != null && ASTNode.BOOLEAN_LITERAL == returnedExpression.getNodeType()) {
					BooleanLiteral booleanLiteral = (BooleanLiteral) returnedExpression;
					if (booleanLiteral.booleanValue()) {
						return findFollowingReturnStatement(forNode);

					}
				}
			}
		}
		
		return null;
	}

	/**
	 * Finds the {@link ReturnStatement} which is placed immediately after the
	 * given {@link EnhancedForStatement} and which returns a {@code false}
	 * value.
	 * 
	 * @param forNode
	 *            represents an enhanced for loop which is expected to be
	 *            followed by a {@code return false;} statement.
	 * 
	 * @return the return statement following the given
	 *         {@link EnhancedForStatement} or {@code null} if the loop is not
	 *         followed by a return statement or the returned value is not
	 *         {@code false}
	 */
	private ReturnStatement findFollowingReturnStatement(EnhancedForStatement forNode) {
		ASTNode forNodeParent = forNode.getParent();
		if (ASTNode.BLOCK == forNodeParent.getNodeType()) {
			Block parentBlock = (Block) forNodeParent;
			List<Statement> statements = ASTNodeUtil.returnTypedList(parentBlock.statements(), Statement.class);
			for (int i = 0; i < statements.size(); i++) {
				Statement statement = statements.get(i);
				if (statement == forNode && i + 1 < statements.size()) {
					Statement nextStatement = statements.get(i + 1);
					if (ASTNode.RETURN_STATEMENT == nextStatement.getNodeType()) {
						ReturnStatement followingReturnSt = (ReturnStatement) nextStatement;
						Expression returnedExpression = followingReturnSt.getExpression();
						if (returnedExpression != null && ASTNode.BOOLEAN_LITERAL == returnedExpression.getNodeType()
								&& !((BooleanLiteral) returnedExpression).booleanValue()) {
							return followingReturnSt;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * A visitor for analyzing an {@link EnhancedForStatement} whether it 
	 * consists of the shape:
	 * 
	 * <pre>
	 * <code>
	 * 	boolean boolVarName = false;
	 * 	for(Object val : values) {
	 * 		if(condition(val)) {
	 * 			boolVarName = true;
	 * 			break;
	 * 		}
	 * 	}
	 * </code>
	 * </pre>
	 * 
	 * Furthermore, it finds the declaration fragment of the assigned boolean variable
	 * and checks whether it initial value is {@code false}.
	 * 
	 * @author Ardit Ymeri
	 * @since 2.0.2
	 *
	 */
	private class LoopWithBreakStatementAnalyzeVisitor extends ASTVisitor {

		private EnhancedForStatement forNode;
		private SimpleName boolVarName;
		private VariableDeclarationFragment boolDeclFragment;
		private Block parentBlock;

		private boolean beforeDeclFragment = true;
		private boolean afterDeclFragment = false;
		private boolean beforeLoop = true;
		private boolean terminate = false;

		public LoopWithBreakStatementAnalyzeVisitor(Block block, EnhancedForStatement forNode, SimpleName boolVarName) {
			this.forNode = forNode;
			this.boolVarName = boolVarName;
			this.parentBlock = block;
		}

		public VariableDeclarationFragment getDeclarationBoolFragment() {
			return boolDeclFragment;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			return !terminate;
		}

		@Override
		public boolean visit(Block block) {
			return this.parentBlock == block || afterDeclFragment;
		}

		@Override
		public boolean visit(EnhancedForStatement loop) {
			if (loop == this.forNode) {
				this.beforeLoop = false;
			}
			return true;
		}

		@Override
		public boolean visit(SimpleName simpleName) {
			IBinding binding = simpleName.resolveBinding();
			if (IBinding.VARIABLE != binding.getKind()) {
				/*
				 * The simple name doesn't represent a variable.
				 */
				return true;
			}
			if (beforeDeclFragment && simpleName.getIdentifier().equals(boolVarName.getIdentifier())) {
				ASTNode parent = simpleName.getParent();
				if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == parent.getNodeType()) {
					VariableDeclarationFragment boolDeclFragment = (VariableDeclarationFragment) parent;
					if (boolDeclFragment.getInitializer() != null) {
						Expression initializer = boolDeclFragment.getInitializer();
						if (ASTNode.BOOLEAN_LITERAL == initializer.getNodeType()) {
							BooleanLiteral fragmentInit = (BooleanLiteral) initializer;
							if (!fragmentInit.booleanValue()) {
								this.boolDeclFragment = boolDeclFragment;
								this.beforeDeclFragment = false;
								this.afterDeclFragment = true;
								return true;
							}
						}
					}
					terminate();
				}
			} else if (afterDeclFragment && beforeLoop
					&& simpleName.getIdentifier().equals(boolVarName.getIdentifier())) {
				/*
				 * The boolean variable is referenced sw between its declaration
				 * and the for loop
				 */
				terminate();
			}

			return true;
		}

		private void terminate() {
			boolDeclFragment = null;
			this.terminate = true;
		}
	}

	/**
	 * A visitor that checks for occurrences of variables that are not
	 * effectively final.
	 * 
	 * @author Ardit Ymeri
	 * @since 2.0.2
	 *
	 */
	class EffectivelyFinalVisitor extends ASTVisitor {

		private boolean containsNonfinalVar = false;

		@Override
		public boolean preVisit2(ASTNode node) {
			return !containsNonfinalVar;
		}

		/**
		 * 
		 * @return if the the visitor has found an occurrence of a variable
		 *         which is NOT effectively final.
		 */
		public boolean containsNonEffectivelyFinalVariable() {
			return this.containsNonfinalVar;
		}

		@Override
		public boolean visit(SimpleName simpleName) {
			IBinding binding = simpleName.resolveBinding();
			if (IBinding.VARIABLE == binding.getKind() && binding instanceof IVariableBinding
					&& !((IVariableBinding) binding).isEffectivelyFinal()) {
				this.containsNonfinalVar = true;
			}

			return true;
		}
	}
}

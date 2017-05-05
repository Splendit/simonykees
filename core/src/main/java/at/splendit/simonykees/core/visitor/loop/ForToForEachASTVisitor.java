package at.splendit.simonykees.core.visitor.loop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.core.visitor.sub.VariableDeclarationsVisitor;

/**
 * For loops with an iterator can be replaced with a forEach loop since 1.7
 * 
 * @author Martin Huter, Ardit Ymeri
 * @since 0.9.2
 */
public class ForToForEachASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String ITERATOR_FULLY_QUALLIFIED_NAME = "java.util.Iterator"; //$NON-NLS-1$
	private static final String ITERABLE_FULLY_QUALIFIED_NAME = Iterable.class.getName();
	private static final String SIZE = "size"; //$NON-NLS-1$
	private static final String DEFAULT_ITERATOR_NAME = "iterator"; //$NON-NLS-1$
	private static final String SMALLER_THAN = "<"; //$NON-NLS-1$

	private Map<ForStatement, LoopOptimizationASTVisior> replaceInformationASTVisitorList;
	private Map<String, Integer> multipleIteratorUse;

	public ForToForEachASTVisitor() {
		this.replaceInformationASTVisitorList = new HashMap<>();
		this.multipleIteratorUse = new HashMap<>();
	}

	@Override
	public boolean visit(ForStatement node) {

		SimpleName iteratorName = ASTNodeUtil.replaceableIteratorCondition(node.getExpression());
		if (iteratorName != null) {
			// Defined updaters are not allowed
			if (!node.updaters().isEmpty()) {
				return true;
			}
			if (ClassRelationUtil.isContentOfTypes(iteratorName.resolveTypeBinding(),
					generateFullyQuallifiedNameList(ITERATOR_FULLY_QUALLIFIED_NAME))) {
				Block parentNode = ASTNodeUtil.getSpecificAncestor(node, Block.class);
				if (parentNode == null) {
					// No surrounding parent block found
					// should not happen, because the Iterator has to be
					// defined in an parent block.
					return false;
				}
				LoopOptimizationASTVisior iteratorDefinitionAstVisior = new LoopOptimizationASTVisior(
						(SimpleName) iteratorName, node);
				iteratorDefinitionAstVisior.setAstRewrite(this.astRewrite);
				parentNode.accept(iteratorDefinitionAstVisior);

				if (iteratorDefinitionAstVisior.allParametersFound()) {
					replaceInformationASTVisitorList.put(node, iteratorDefinitionAstVisior);
				}
			}

		} else if (node.getExpression() != null && ASTNode.INFIX_EXPRESSION == node.getExpression().getNodeType()) {
			// if the condition of the for loop is an infix expression....
			InfixExpression infixExpression = (InfixExpression) node.getExpression();
			Expression rhs = infixExpression.getRightOperand();

			if (SMALLER_THAN.equals(infixExpression.getOperator().toString())
					&& ASTNode.METHOD_INVOCATION == rhs.getNodeType()) {
				MethodInvocation condition = (MethodInvocation) rhs;
				Expression conditionExpression = condition.getExpression();
				if (Expression.SIMPLE_NAME == conditionExpression.getNodeType()) {
					SimpleName iterableNode = (SimpleName) conditionExpression;
					ITypeBinding iterableTypeBinding = iterableNode.resolveTypeBinding();
					ITypeBinding iteratorType = findIteratorType(iterableTypeBinding);

					/*
					 * ...and the right hand side of the infix expression is an
					 * invocation of List::size in the iterable object
					 */
					if (iteratorType != null
							&& ClassRelationUtil.isInheritingContentOfTypes(iterableTypeBinding,
									Collections.singletonList(ITERABLE_FULLY_QUALIFIED_NAME))
							&& StringUtils.equals(SIZE, condition.getName().getIdentifier())
							&& condition.arguments().isEmpty()) {

						// ... and the left hand side is a simple name
						Expression lhs = infixExpression.getLeftOperand();
						if (Expression.SIMPLE_NAME == lhs.getNodeType()) {
							SimpleName index = (SimpleName) lhs;

							/*
							 * Initiate a visitor for investigating the
							 * replacement precondition and gathering the
							 * replacement information
							 */
							ForLoopIteratingIndexVisitor indexVisitor = new ForLoopIteratingIndexVisitor(index, iterableNode, node);
							Block outerBlock = ASTNodeUtil.getSpecificAncestor(node, Block.class);
							outerBlock.accept(indexVisitor);
							if (indexVisitor.checkTransformPrecondition()) {

								// invocations of List::get to be replaced with
								// the iterator object
								List<MethodInvocation> toBeReplaced = indexVisitor.getIteratorExpressions();
								List<ASTNode> toBeRemoved = indexVisitor.getNodesToBeRemoved();
								SimpleName firstIteratorName = indexVisitor.getIteratorName();
								Statement loopBody = node.getBody();
								// generate a safe iterator name
								Map<String, Boolean> nameMap = generateNewIteratorName(firstIteratorName, loopBody);
								String newIteratorIdentifier = nameMap.keySet().iterator().next();
								boolean eligiblePreferredName = nameMap.get(newIteratorIdentifier);
								if(eligiblePreferredName && indexVisitor.getPreferredNameFragment() != null) {
									toBeRemoved.add(indexVisitor.getPreferredNameFragment());
								}

								// remove the redundant nodes
								toBeRemoved.forEach(remove -> {
									if (remove
											.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
										VariableDeclarationStatement declStatement = (VariableDeclarationStatement) remove
												.getParent();
										if (declStatement.fragments().size() == 1) {
											astRewrite.remove(declStatement, null);
										}
									}
									astRewrite.remove(remove, null);
								});

								// replace the List::get invocations with the
								// new iterator
								toBeReplaced.forEach(target -> astRewrite.replace(target,
										astRewrite.getAST().newSimpleName(newIteratorIdentifier), null));

								// create a declaration of the new iterator
								SingleVariableDeclaration iteratorDecl = NodeBuilder.newSingleVariableDeclaration(
										loopBody.getAST(), astRewrite.getAST().newSimpleName(newIteratorIdentifier),
										astRewrite.getAST()
												.newSimpleType(astRewrite.getAST().newName(iteratorType.getName())));

								// create the new enhanced for loop
								EnhancedForStatement newFor = NodeBuilder.newEnhancedForStatement(loopBody.getAST(),
										(Statement) astRewrite.createCopyTarget(loopBody),
										(Expression) astRewrite.createCopyTarget(iterableNode), iteratorDecl);

								// replace the existing for loop with the new
								// one
								astRewrite.replace(node, newFor, null);

							}
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Finds the type binding of the new iterator object from the type of the
	 * iterable object. If the type is a wild card then gets its upper bound.
	 * 
	 * @param iterableNode
	 *            node expected to represent a parameterized type object
	 * @return type binding of the iterator
	 */
	private ITypeBinding findIteratorType(ITypeBinding iterableTypeBinding) {
		ITypeBinding iteratorType = null;
		if (iterableTypeBinding != null && iterableTypeBinding.isParameterizedType()) {
			iteratorType = iterableTypeBinding.getTypeArguments()[0];
			if (iteratorType.getTypeBounds().length > 0) {
				iteratorType = iteratorType.getTypeBounds()[0];
			}
		}
		return iteratorType;
	}

	/**
	 * Generates a unique name for the iterator of the enhanced for loop, by
	 * adding a suffix to the given preferred name if there is another variable
	 * with the same name declared in the scope of the body of the loop. Uses
	 * the {@value #DEFAULT_ITERATOR_NAME} if the given name is null.
	 * 
	 * @param preferedName
	 *            a preferred name for the iterator
	 * @param loopBody
	 *            the body of the loop
	 * @return a new name for the iterator.
	 */
	private Map<String, Boolean> generateNewIteratorName(SimpleName preferedName, Statement loopBody) {
		VariableDeclarationsVisitor loopBodyDeclarationsVisitor = new VariableDeclarationsVisitor();
		loopBody.accept(loopBodyDeclarationsVisitor);
		List<SimpleName> loobBodyDeclarations = loopBodyDeclarationsVisitor.getVariableDeclarationNames();
		List<String> declaredNames = loobBodyDeclarations.stream().filter(name -> name != preferedName)
				.map(SimpleName::getIdentifier).collect(Collectors.toList());
		

		String newName;
		Boolean allowedPreferedName;
		if (preferedName == null || declaredNames.contains(preferedName.getIdentifier())) {
			allowedPreferedName = false;
			int counter = 0;
			String suffix = ""; //$NON-NLS-1$
			ASTNode scope = findScopeOfLoop(loopBody);
			VariableDeclarationsVisitor loopScopeVisitor = new VariableDeclarationsVisitor();
			scope.accept(loopScopeVisitor);
			
			declaredNames = loobBodyDeclarations.stream().map(SimpleName::getIdentifier).collect(Collectors.toList());
			while (declaredNames.contains(DEFAULT_ITERATOR_NAME + suffix)) {
				counter++;
				suffix = Integer.toString(counter);
			}
			newName = DEFAULT_ITERATOR_NAME + suffix;
		} else {
			allowedPreferedName = true;
			newName = preferedName.getIdentifier();
		}

		Map<String, Boolean> nameMap = new HashMap<>();
		nameMap.put(newName, allowedPreferedName);
		
		return nameMap;
	}

	private ASTNode findScopeOfLoop(Statement loopBody) {
		ASTNode parent = loopBody.getParent();
		while(parent != null && 
				parent.getNodeType() != ASTNode.METHOD_DECLARATION && parent.getNodeType() == ASTNode.INITIALIZER) {
			parent = parent.getParent();
		}
		return parent;
	}

	@Override
	public void endVisit(ForStatement node) {
		// Do the replacement
		if (replaceInformationASTVisitorList.containsKey(node)) {
			LoopOptimizationASTVisior iteratorDefinitionAstVisior = replaceInformationASTVisitorList.remove(node);
			iteratorDefinitionAstVisior.replaceLoop(node, node.getBody(), multipleIteratorUse);

			// clear the variableIterator if no other loop is present
			if (replaceInformationASTVisitorList.isEmpty()) {
				multipleIteratorUse.clear();
			}
		}
	}
}

/**
 * A visitor for investigating the replace precondition of a for loop with 
 * an enhanced for loop. 
 * 
 * @author Ardit Ymeri
 * @since 1.2
 */
class ForLoopIteratingIndexVisitor extends ASTVisitor {

	private static final String OUTSIDE_LOOP_INDEX_DECLARATION = "outside-declaration-fragment"; //$NON-NLS-1$
	private static final String LOOP_INITIALIZER = "loop-initializer"; //$NON-NLS-1$
	private static final String LOOP_UPDATER = "loop-updater"; //$NON-NLS-1$
	private static final String INTERNAL_INDEX_UPDATER = "internal-index-updater"; //$NON-NLS-1$

	private static final String PLUS_PLUS = "++"; //$NON-NLS-1$
	private static final String PLUS = "+"; //$NON-NLS-1$
	private static final String ONE = "1"; //$NON-NLS-1$
	private static final String ZERO = "0"; //$NON-NLS-1$
	private static final String GET = "get"; //$NON-NLS-1$

	private SimpleName iteratingIndexName;
	private SimpleName newIteratorName;
	private SimpleName iterableName;
	private ForStatement forStatement;

	private Map<String, ASTNode> indexInitializer;
	private Map<String, ASTNode> indexUpdater;

	private boolean insideLoop = false;
	private boolean beforeLoop = true;
	private boolean afterLoop = false;

	private boolean indexReferencedOutsideLoop = false;
	private boolean indexReferencedInsideLoop = false;
	private boolean multipleLoopInits = false;
	private boolean multipleLoopUpdaters = false;
	private boolean hasEmptyStatement = false;

	private List<MethodInvocation> iteratingObjectInitializers;
	private List<ASTNode> nodesToBeRemoved;
	private VariableDeclarationFragment preferredNameFragment;

	public ForLoopIteratingIndexVisitor(SimpleName iteratingIndexName, SimpleName iterableName, ForStatement forStatement) {
		this.iteratingIndexName = iteratingIndexName;
		this.iterableName = iterableName;
		this.forStatement = forStatement;
		this.iteratingObjectInitializers = new ArrayList<>();
		this.indexInitializer = new HashMap<>();
		this.indexUpdater = new HashMap<>();
		this.nodesToBeRemoved = new ArrayList<>();

		List<Expression> initializers = ASTNodeUtil.returnTypedList(forStatement.initializers(), Expression.class);
		if (initializers.size() == 1) {
			indexInitializer.put(LOOP_INITIALIZER, initializers.get(0));
		} else if (initializers.size() > 0) {
			multipleLoopInits = true;
		}

		List<Expression> updaters = ASTNodeUtil.returnTypedList(forStatement.updaters(), Expression.class);
		if (updaters.size() == 1) {
			indexUpdater.put(LOOP_UPDATER, updaters.get(0));
		} else if (updaters.size() > 1) {
			multipleLoopUpdaters = true;
		}

		List<Statement> statements = ASTNodeUtil.returnTypedList(((Block) forStatement.getBody()).statements(),
				Statement.class);
		if (!statements.isEmpty()) {
			Statement lastStatement = statements.get(statements.size() - 1);
			if (isValidIncrementStatement(lastStatement, iteratingIndexName)) {
				indexUpdater.put(INTERNAL_INDEX_UPDATER, lastStatement);
				nodesToBeRemoved.add(lastStatement);
			}
		}
	}
	
	public VariableDeclarationFragment getPreferredNameFragment() {
		return preferredNameFragment;
	}

	public List<ASTNode> getNodesToBeRemoved() {
		return this.nodesToBeRemoved;
	}

	public SimpleName getIteratorName() {
		return this.newIteratorName;
	}

	public List<MethodInvocation> getIteratorExpressions() {
		return iteratingObjectInitializers;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !multipleLoopInits && !multipleLoopUpdaters;
	}

	@Override
	public boolean visit(ForStatement node) {
		if (node == forStatement) {
			insideLoop = true;
			beforeLoop = false;
		}
		return true;
	}

	@Override
	public void endVisit(ForStatement node) {
		if (node == forStatement) {
			insideLoop = false;
			afterLoop = true;
		}
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding resolvedBinding = simpleName.resolveBinding();
		if (resolvedBinding != null && IBinding.VARIABLE == resolvedBinding.getKind()
				&& simpleName.getIdentifier().equals(iteratingIndexName.getIdentifier())) {

			ASTNode parent = simpleName.getParent();
			if (beforeLoop) {
				if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == parent.getNodeType()) {
					VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) parent;
					indexInitializer.put(OUTSIDE_LOOP_INDEX_DECLARATION, declarationFragment);
					nodesToBeRemoved.add(declarationFragment);

				} else {
					indexReferencedOutsideLoop = true;
				}
			} else if (insideLoop
					&& (parent != indexInitializer.get(LOOP_INITIALIZER)
							|| parent.getParent() != indexInitializer.get(LOOP_INITIALIZER))
					&& parent != forStatement.getExpression() && (parent != indexInitializer.get(LOOP_UPDATER)
							|| parent.getParent() != indexInitializer.get(LOOP_UPDATER))) {

				if (ASTNode.METHOD_INVOCATION == parent.getNodeType()) {
					MethodInvocation methodInvocation = (MethodInvocation) parent;
					Expression methodExpression = methodInvocation.getExpression();

					if (ASTNode.EXPRESSION_STATEMENT == methodInvocation.getParent().getNodeType()) {
						this.hasEmptyStatement = true;
					} else if (GET.equals(methodInvocation.getName().getIdentifier())
							&& methodInvocation.arguments().size() == 1 && methodExpression != null
							&& methodExpression.getNodeType() == ASTNode.SIMPLE_NAME
							&& ((SimpleName) methodExpression).getIdentifier().equals(iterableName.getIdentifier())) {
						iteratingObjectInitializers.add(methodInvocation);

						
						if (newIteratorName == null
								&& VariableDeclarationFragment.INITIALIZER_PROPERTY == methodInvocation
										.getLocationInParent()) {
							VariableDeclarationFragment fragment = (VariableDeclarationFragment) methodInvocation
									.getParent();
							this.newIteratorName = fragment.getName();
							this.preferredNameFragment =fragment;
						}
						
					} else {
						this.indexReferencedInsideLoop = true;
					}
				} else if (parent.getLocationInParent() != ForStatement.UPDATERS_PROPERTY
						&& parent.getParent().getLocationInParent() != ForStatement.UPDATERS_PROPERTY
						&& parent.getLocationInParent() != ForStatement.INITIALIZERS_PROPERTY
						&& parent.getParent().getLocationInParent() != ForStatement.INITIALIZERS_PROPERTY) {

					this.indexReferencedInsideLoop = true;
				}
			} else if (afterLoop) {
				indexReferencedOutsideLoop = true;
			}
		}

		return true;
	}

	/**
	 * Checks if the given statement is an increment statement of the operand.
	 * The following three cases are considered:
	 * <ul>
	 * <li>{@code operand++;}</li>
	 * <li>{@code ++operand;}</li>
	 * <li>{@code operand = operand + 1;}</li>
	 * </ul>
	 * 
	 * @param statement
	 *            statement to be checked
	 * @param operandName
	 *            operand name
	 * @return if the statement is an increment statement.
	 */
	private boolean isValidIncrementStatement(Statement statement, SimpleName operandName) {
		boolean isIncrement = false;
		if (ASTNode.EXPRESSION_STATEMENT == statement.getNodeType()) {
			ExpressionStatement expressionStatement = (ExpressionStatement) statement;
			Expression expression = expressionStatement.getExpression();
			int expressionType = expression.getNodeType();

			if (ASTNode.POSTFIX_EXPRESSION == expressionType) {
				// covers the case: operand++;
				PostfixExpression postfixExpression = (PostfixExpression) expression;
				Expression operand = postfixExpression.getOperand();
				if (ASTNode.SIMPLE_NAME == operand.getNodeType()
						&& ((SimpleName) operand).getIdentifier().equals(operandName.getIdentifier())
						&& PLUS_PLUS.equals(postfixExpression.getOperator())) {

					isIncrement = true;
				}
			} else if (ASTNode.ASSIGNMENT == expressionType) {
				Assignment assignmentExpression = (Assignment) expression;
				Expression lhs = assignmentExpression.getLeftHandSide();
				Expression rhs = assignmentExpression.getRightHandSide();

				if (ASTNode.SIMPLE_NAME == lhs.getNodeType()
						&& ((SimpleName) lhs).getIdentifier().equals(operandName.getIdentifier())
						&& ASTNode.INFIX_EXPRESSION == rhs.getNodeType()) {
					// covers the case: operand = operand +1;

					InfixExpression infixExpression = (InfixExpression) rhs;
					Expression leftOperand = infixExpression.getLeftOperand();
					Expression rightOperand = infixExpression.getRightOperand();
					if (PLUS.equals(infixExpression.getOperator()) && ((ASTNode.SIMPLE_NAME == leftOperand.getNodeType()
							&& ((SimpleName) leftOperand).getIdentifier().equals(operandName.getIdentifier())
							&& ASTNode.NUMBER_LITERAL == rightOperand.getNodeType()
							&& ONE.equals(((NumberLiteral) rightOperand).getToken()))
							|| ((ASTNode.SIMPLE_NAME == rightOperand.getNodeType()
									&& ((SimpleName) rightOperand).getIdentifier().equals(operandName.getIdentifier())
									&& ASTNode.NUMBER_LITERAL == leftOperand.getNodeType()
									&& ONE.equals(((NumberLiteral) leftOperand).getToken()))))) {

						isIncrement = true;
					}
				}

			} else if (ASTNode.PREFIX_EXPRESSION == expressionType) {
				// covers the case: ++operand;
				PrefixExpression postfixExpression = (PrefixExpression) expression;
				Expression operand = postfixExpression.getOperand();
				if (ASTNode.SIMPLE_NAME == operand.getNodeType()
						&& ((SimpleName) operand).getIdentifier().equals(operandName.getIdentifier())
						&& PLUS_PLUS.equals(postfixExpression.getOperator())) {
					isIncrement = true;
				}
			}
		}

		return isIncrement;
	}

	/**
	 * Checks whether the loop index is initialized to zero.
	 * 
	 * @return true if the iterating index is assigned to zero.
	 */
	private boolean isIndexInitToZero() {
		boolean initOutsideLoop = false;
		boolean initInLoop = false;

		ASTNode outIndexInit = null;
		ASTNode loopInitializer = null;
		if (indexInitializer.containsKey(OUTSIDE_LOOP_INDEX_DECLARATION)) {
			outIndexInit = indexInitializer.get(OUTSIDE_LOOP_INDEX_DECLARATION);
			initOutsideLoop = isInitializedToZero(outIndexInit);
		}

		if (indexInitializer.containsKey(LOOP_INITIALIZER)) {
			loopInitializer = indexInitializer.get(LOOP_INITIALIZER);
			initInLoop = isInitializedToZero(loopInitializer);
		}

		return initOutsideLoop || initInLoop;
	}

	private boolean isIndexIncremented() {
		return (indexUpdater.containsKey(LOOP_UPDATER) && !indexUpdater.containsKey(INTERNAL_INDEX_UPDATER))
				|| (!indexUpdater.containsKey(LOOP_UPDATER) && indexUpdater.containsKey(INTERNAL_INDEX_UPDATER));
	}

	/**
	 * Checks whether the given expression is an initialization to zero expression.
	 */
	private boolean isInitializedToZero(ASTNode initExpresion) {
		boolean assignedToZero = false;
		if (ASTNode.ASSIGNMENT == initExpresion.getNodeType()) {
			Assignment assignment = (Assignment) initExpresion;
			Expression rhs = assignment.getRightHandSide();
			if (ASTNode.NUMBER_LITERAL == rhs.getNodeType()) {
				assignedToZero = ((NumberLiteral) rhs).getToken().equals(ZERO);
			}
		} else if (ASTNode.VARIABLE_DECLARATION_EXPRESSION == initExpresion.getNodeType()) {
			VariableDeclarationExpression declExpresion = (VariableDeclarationExpression) initExpresion;
			List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(declExpresion.fragments(),
					VariableDeclarationFragment.class);
			if (fragments.size() == 1) {
				VariableDeclarationFragment fragment = fragments.get(0);
				Expression initializer = fragment.getInitializer();
				if (initializer != null && ASTNode.NUMBER_LITERAL == initializer.getNodeType()) {
					assignedToZero = ((NumberLiteral) initializer).getToken().equals(ZERO);
				}
			}
		} else if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == initExpresion.getNodeType()) {
			VariableDeclarationFragment declFragment = (VariableDeclarationFragment) initExpresion;
			Expression initializer = declFragment.getInitializer();
			if (initializer != null && ASTNode.NUMBER_LITERAL == initializer.getNodeType()) {
				assignedToZero = ((NumberLiteral) initializer).getToken().equals(ZERO);
			}
		}

		return assignedToZero;
	}

	public boolean checkTransformPrecondition() {
		return !hasEmptyStatement && !indexReferencedInsideLoop && !indexReferencedOutsideLoop && isIndexInitToZero()
				&& isIndexIncremented();
	}

	public VariableDeclarationFragment getOutsideIndexDeclaration() {
		return (VariableDeclarationFragment) indexInitializer.get(OUTSIDE_LOOP_INDEX_DECLARATION);
	}

	public ExpressionStatement getInternalIndexUpdater() {
		return (ExpressionStatement) indexUpdater.get(INTERNAL_INDEX_UPDATER);
	}
}

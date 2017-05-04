package at.splendit.simonykees.core.visitor.loop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PostfixExpression.Operator;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * For loops with an iterator can be replaced with a forEach loop since 1.7
 * 
 * @author Martin Huter, Ardit Ymeri
 * @since 0.9.2
 */
public class ForToForEachASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String ITERATOR_FULLY_QUALLIFIED_NAME = "java.util.Iterator"; //$NON-NLS-1$

	private Map<ForStatement, LoopOptimizationASTVisior> replaceInformationASTVisitorList;
	private Map<String, Integer> multipleIteratorUse;

	public ForToForEachASTVisitor() {
		super();
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

		} else {
			Expression loopExpression = node.getExpression();
			if (Expression.INFIX_EXPRESSION == loopExpression.getNodeType()) {
				InfixExpression infixExpression = (InfixExpression) loopExpression;
				Expression rhs = infixExpression.getRightOperand();
				if (Expression.METHOD_INVOCATION == rhs.getNodeType()) {
					MethodInvocation condition = (MethodInvocation) rhs;
					Expression conditionExpression = condition.getExpression();
					if (Expression.SIMPLE_NAME == conditionExpression.getNodeType()) {
						SimpleName iterableNode = (SimpleName) conditionExpression;

						if (ClassRelationUtil.isInheritingContentOfTypes(iterableNode.resolveTypeBinding(),
								Collections.singletonList(Iterable.class.getName()))) {

							if (StringUtils.equals("size", condition.getName().getIdentifier())
									&& condition.arguments().isEmpty()) {

								Expression lhs = infixExpression.getLeftOperand();
								if (Expression.SIMPLE_NAME == lhs.getNodeType()) {
									SimpleName index = (SimpleName) lhs;

									IteratingIndexVisitor indexVisitor = new IteratingIndexVisitor(index, node);
									Block outerBlock = ASTNodeUtil.getSpecificAncestor(node, Block.class);
									outerBlock.accept(indexVisitor);
									if (indexVisitor.checkTransformPrecondition()) {
										indexVisitor.getInternalIndexUpdater();
										indexVisitor.getOutsideIndexDeclaration();
										List<MethodInvocation> toBeReplaced = indexVisitor.getIteratorExpressions();
										List<ASTNode> toBeRemoved = indexVisitor.getNodesToBeRemoved();
										SimpleName newIteratorName = indexVisitor.getIteratorName();
										Type iteratorType = indexVisitor.getIteratorType();
										//TODO: otherwise generate a new name for the iterator 
										if(newIteratorName != null) {
																						
											toBeRemoved.forEach(redundantNode -> {
												if(redundantNode.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
													VariableDeclarationStatement declStatement = (VariableDeclarationStatement)redundantNode.getParent();
													if(declStatement.fragments().size() == 1) {
														astRewrite.remove(declStatement, null);
													} 
												}
												astRewrite.remove(redundantNode, null);
												
											});
											
											
											toBeReplaced.forEach(target -> astRewrite.replace(target, astRewrite.createCopyTarget(newIteratorName), null));
											
											SingleVariableDeclaration iteratorDecl = NodeBuilder.newSingleVariableDeclaration(node.getBody().getAST(),
													(SimpleName) astRewrite.createCopyTarget(newIteratorName), (Type)astRewrite.createCopyTarget(iteratorType));
													EnhancedForStatement newFor = NodeBuilder.newEnhancedForStatement(
													node.getBody().getAST(), 
													(Statement)astRewrite.createCopyTarget(node.getBody()), 
													(Expression)astRewrite.createCopyTarget(iterableNode) , 
													iteratorDecl);
													
													astRewrite.replace(node, newFor, null);		
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return true;
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

class IteratingIndexVisitor extends ASTVisitor {

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
	private Type newIteratorType;
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


	public IteratingIndexVisitor(SimpleName iteratingIndexName, ForStatement forStatement) {
		this.iteratingIndexName = iteratingIndexName;
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

	public Type getIteratorType() {
		return this.newIteratorType;
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
					if (ASTNode.EXPRESSION_STATEMENT == methodInvocation.getParent().getNodeType()) {
						this.hasEmptyStatement = true;
					} else if (GET.equals(methodInvocation.getName().getIdentifier())
							&& methodInvocation.arguments().size() == 1) {
						iteratingObjectInitializers.add(methodInvocation);
						if (newIteratorName == null
								&& VariableDeclarationFragment.INITIALIZER_PROPERTY == methodInvocation
										.getLocationInParent()) {
							VariableDeclarationFragment fragment = (VariableDeclarationFragment) methodInvocation
									.getParent();
							this.newIteratorName = fragment.getName();
							this.newIteratorType = ((VariableDeclarationStatement) fragment.getParent()).getType();
							this.nodesToBeRemoved.add(fragment);
						}
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

	private boolean isValidIncrementStatement(Statement statement, SimpleName operatorName) {
		boolean isIncrement = false;
		if (ASTNode.EXPRESSION_STATEMENT == statement.getNodeType()) {
			ExpressionStatement expressionStatement = (ExpressionStatement) statement;
			Expression expression = expressionStatement.getExpression();
			int expressionType = expression.getNodeType();
			if (ASTNode.POSTFIX_EXPRESSION == expressionType) {
				PostfixExpression postfixExpression = (PostfixExpression) expression;
				Expression operand = postfixExpression.getOperand();
				if (ASTNode.SIMPLE_NAME == operand.getNodeType()
						&& ((SimpleName) operand).getIdentifier().equals(operatorName.getIdentifier())
						&& PLUS_PLUS.equals(postfixExpression.getOperator())) {

					isIncrement = true;
				}
			} else if (ASTNode.ASSIGNMENT == expressionType) {
				Assignment assignmentExpression = (Assignment) expression;
				Expression lhs = assignmentExpression.getLeftHandSide();
				Expression rhs = assignmentExpression.getRightHandSide();

				if (ASTNode.SIMPLE_NAME == lhs.getNodeType()
						&& ((SimpleName) lhs).getIdentifier().equals(operatorName.getIdentifier())
						&& ASTNode.INFIX_EXPRESSION == rhs.getNodeType()) {

					InfixExpression infixExpression = (InfixExpression) rhs;
					Expression leftOperand = infixExpression.getLeftOperand();
					Expression rightOperand = infixExpression.getRightOperand();
					if (PLUS.equals(infixExpression.getOperator()) && ((ASTNode.SIMPLE_NAME == leftOperand.getNodeType()
							&& ((SimpleName) leftOperand).getIdentifier().equals(operatorName.getIdentifier())
							&& ASTNode.NUMBER_LITERAL == rightOperand.getNodeType()
							&& ONE.equals(((NumberLiteral) rightOperand).getToken()))
							|| ((ASTNode.SIMPLE_NAME == rightOperand.getNodeType()
									&& ((SimpleName) rightOperand).getIdentifier().equals(operatorName.getIdentifier())
									&& ASTNode.NUMBER_LITERAL == leftOperand.getNodeType()
									&& ONE.equals(((NumberLiteral) leftOperand).getToken())

							)))) {

						isIncrement = true;
					}
				}

			} else if (ASTNode.PREFIX_EXPRESSION == expressionType) {
				PrefixExpression postfixExpression = (PrefixExpression) expression;
				Expression operand = postfixExpression.getOperand();
				if (ASTNode.SIMPLE_NAME == operand.getNodeType()
						&& ((SimpleName) operand).getIdentifier().equals(operatorName.getIdentifier())
						&& PLUS_PLUS.equals(postfixExpression.getOperator())) {
					isIncrement = true;
				}
			}
		}

		return isIncrement;
	}

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

	private boolean isInitializedToZero(ASTNode loopInitializer) {
		boolean assignedToZero = false;
		if (ASTNode.ASSIGNMENT == loopInitializer.getNodeType()) {
			Assignment assignment = (Assignment) loopInitializer;
			Expression rhs = assignment.getRightHandSide();
			if (ASTNode.NUMBER_LITERAL == rhs.getNodeType()) {
				assignedToZero = ((NumberLiteral) rhs).getToken().equals(ZERO);
			}
		} else if (ASTNode.VARIABLE_DECLARATION_EXPRESSION == loopInitializer.getNodeType()) {
			VariableDeclarationExpression declExpresion = (VariableDeclarationExpression) loopInitializer;
			List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(declExpresion.fragments(),
					VariableDeclarationFragment.class);
			if (fragments.size() == 1) {
				VariableDeclarationFragment fragment = fragments.get(0);
				Expression initializer = fragment.getInitializer();
				if (initializer != null && ASTNode.NUMBER_LITERAL == initializer.getNodeType()) {
					assignedToZero = ((NumberLiteral) initializer).getToken().equals(ZERO);
				}
			}
		} else if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == loopInitializer.getNodeType()) {
			VariableDeclarationFragment declFragment = (VariableDeclarationFragment) loopInitializer;
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

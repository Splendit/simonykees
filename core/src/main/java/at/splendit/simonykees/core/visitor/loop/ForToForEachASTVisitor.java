package at.splendit.simonykees.core.visitor.loop;

import java.util.ArrayList;
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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.constants.ReservedNames;
import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractCompilationUnitASTVisitor;

/**
 * For loops with an iterator can be replaced with a forEach loop since 1.7
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class ForToForEachASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static final Integer ITERATOR_KEY = 1;
	private static final String ITERATOR_FULLY_QUALLIFIED_NAME = "java.util.Iterator"; //$NON-NLS-1$

	private static final Integer LIST_KEY = 2;
	private static final String LIST_FULLY_QUALLIFIED_NAME = "java.util.List"; //$NON-NLS-1$
	
	private Map<ForStatement, LoopOptimizationASTVisior> replaceInformationASTVisitorList;
	private Map<String, Integer> multipleIteratorUse;

	public ForToForEachASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(ITERATOR_KEY, generateFullyQuallifiedNameList(ITERATOR_FULLY_QUALLIFIED_NAME));
		this.fullyQuallifiedNameMap.put(LIST_KEY, generateFullyQuallifiedNameList(LIST_FULLY_QUALLIFIED_NAME));
		this.replaceInformationASTVisitorList = new HashMap<>();
		this.multipleIteratorUse = new HashMap<>();
	}

	public boolean visit(ForStatement node) {
		//Defined updaters are note allowed 
		if(!node.updaters().isEmpty()){
			return true;
		}
		SimpleName iteratorName = replaceAbleWhileCondition(node.getExpression());
		if (iteratorName != null) {
			if (ClassRelationUtil.isContentOfRegistertITypes(iteratorName.resolveTypeBinding(),
					iTypeMap.get(ITERATOR_KEY))) {
				Block parentNode = ASTNodeUtil.getSurroundingBlock(node);
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
		}
		return true;
	}
	
	@Override
	public void endVisit(ForStatement node) {
		// Do the replacement
		if (replaceInformationASTVisitorList.containsKey(node)) {
			LoopOptimizationASTVisior iteratorDefinitionAstVisior = replaceInformationASTVisitorList.remove(node);
			Type iteratorType = ASTNodeUtil.getSingleTypeParameterOfVariableDeclaration(iteratorDefinitionAstVisior.getIteratorDeclaration());

			// iterator has no type-parameter therefore a optimization is could
			// not be applied
			if (null == iteratorType) {
				return;
			}
			else {
				iteratorType = (Type) astRewrite.createMoveTarget(iteratorType);
			}
			// find LoopvariableName

			MethodInvocation nextCall = iteratorDefinitionAstVisior.getIteratorNextCall();
			SingleVariableDeclaration singleVariableDeclaration = null;
			if (nextCall.getParent() instanceof SingleVariableDeclaration) {
				singleVariableDeclaration = (SingleVariableDeclaration) astRewrite
						.createMoveTarget(nextCall.getParent());
				astRewrite.remove(nextCall.getParent(), null);
			} else if (nextCall.getParent() instanceof VariableDeclarationFragment
					&& nextCall.getParent().getParent() instanceof VariableDeclarationStatement) {
				VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) nextCall
						.getParent();
				VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) variableDeclarationFragment
						.getParent();
				if (1 == variableDeclarationStatement.fragments().size()) {
					singleVariableDeclaration = NodeBuilder.newSingleVariableDeclaration(node.getAST(),
							(SimpleName) astRewrite.createMoveTarget(variableDeclarationFragment.getName()),
							iteratorType);
					astRewrite.remove(variableDeclarationStatement, null);
				}
			}

			if (null == singleVariableDeclaration) {
				// Solution for Iteration over the same List without variables
				String iteratorName = iteratorDefinitionAstVisior.getListName().getFullyQualifiedName()
						+ ReservedNames.CLASS_ITERATOR;
				if(null == multipleIteratorUse.get(iteratorName)){
					multipleIteratorUse.put(iteratorName, 2);
				}
				else{
					Integer i = multipleIteratorUse.get(iteratorName);
					multipleIteratorUse.put(iteratorName, i + 1);
					iteratorName = iteratorName + i;
				}
				
				
				singleVariableDeclaration = NodeBuilder.newSingleVariableDeclaration(node.getAST(),
						NodeBuilder.newSimpleName(node.getAST(),iteratorName),
						iteratorType);
				//if the next call is used only as an ExpressionStatement just remove it.
				if(nextCall.getParent() instanceof ExpressionStatement){
					astRewrite.remove(nextCall.getParent(), null);
				}
				else{
					astRewrite.replace(nextCall,
							NodeBuilder.newSimpleName(node.getAST(),iteratorName),null);
				}
			}

			EnhancedForStatement newFor = NodeBuilder.newEnhancedForStatement(node.getAST(),
					(Statement) astRewrite.createMoveTarget(node.getBody()),
					(Expression) astRewrite.createMoveTarget(iteratorDefinitionAstVisior.getListName()),
					singleVariableDeclaration);
			astRewrite.replace(node, newFor, null);

			astRewrite.remove(iteratorDefinitionAstVisior.getIteratorDeclaration(), null);
			
			//clear the variableIterator if no other loop is present
			if(replaceInformationASTVisitorList.isEmpty()){
				multipleIteratorUse.clear();
			}
		}
	}
	
	private SimpleName replaceAbleWhileCondition(Expression node) {
		if (node instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) node;
			// check for hasNext operation on Iterator
			if (StringUtils.equals("hasNext", methodInvocation.getName().getFullyQualifiedName()) //$NON-NLS-1$
					&& methodInvocation.getExpression() instanceof SimpleName) {
				return (SimpleName) methodInvocation.getExpression();
			}
		}
		return null;
	}
	
	
	
	public boolean oldImplementationVisit(ForStatement node) {
		if (node.getExpression() instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) node.getExpression();

			// check for hasNext operation on Iterator
			if (StringUtils.equals("hasNext", methodInvocation.getName().getFullyQualifiedName()) //$NON-NLS-1$
					&& methodInvocation.getExpression() instanceof SimpleName) {
				SimpleName iteratorName = (SimpleName) methodInvocation.getExpression();

				if (iteratorName != null && !ClassRelationUtil
						.isContentOfRegistertITypes(iteratorName.resolveTypeBinding(), iTypeMap.get(ITERATOR_KEY))) {
					// Type is not an Iterator
					return false;
				}
				LoopOptimizationASTVisior iteratorDefinitionAstVisior = new LoopOptimizationASTVisior(iteratorName, null);
				if (1 == node.initializers().size()) {
					((ASTNode) node.initializers().get(0)).accept(iteratorDefinitionAstVisior);
				}

				FindNextVariableASTVisitor findNextVariableAstVisitor = new FindNextVariableASTVisitor(
						(SimpleName) iteratorName);
				findNextVariableAstVisitor.setAstRewrite(this.astRewrite);
				node.getBody().accept(findNextVariableAstVisitor);
				if (findNextVariableAstVisitor.isTransformable() && iteratorDefinitionAstVisior.getListName() != null) {

					SimpleName iterationVariable = findNextVariableAstVisitor.getVariableName();
					Type iterationType = findNextVariableAstVisitor.getIteratorVariableType();

					/*
					 * wenn der typ == null ist muss der typ wieder außerhab
					 * gesucht werden
					 */
					SingleVariableDeclaration iterationVariableDefinition = NodeBuilder.newSingleVariableDeclaration(
							node.getAST(), (SimpleName) astRewrite.createMoveTarget(iterationVariable),
							(Type) astRewrite.createMoveTarget(iterationType));

					Expression iterationList = (Expression) astRewrite
							.createMoveTarget(iteratorDefinitionAstVisior.getListName());

					EnhancedForStatement newFor = NodeBuilder.newEnhancedForStatement(node.getAST(),
							(Statement) astRewrite.createMoveTarget(node.getBody()), iterationList,
							iterationVariableDefinition);
					astRewrite.remove(findNextVariableAstVisitor.getRemoveWithTransformation(), null);
					astRewrite.replace(node, newFor, null);
				}
			}
		}

		/*
		 * Preconditions for second case node expression is an infixExpression
		 * of iterationVariable < listName.size(); updaters have only one entry;
		 * initializers have only one entry
		 */
		if (node.getExpression() instanceof InfixExpression && node.updaters().size() == 1
				&& node.initializers().size() == 1) {
			// needed components for refactoring
			SimpleName listName;
			Type listGenericType = null;
			SimpleName iterationVariable;

			/*
			 * first condition: node expression is an infixExpression of
			 * iterationVariable < listName.size()
			 */
			{
				InfixExpression infixExpression = (InfixExpression) node.getExpression();

				/*
				 * lesserSide < greaterSide; greaterSide > lesserSide
				 */
				Expression lesserSide = null;
				Expression greaterSide = null;
				if (infixExpression.getOperator().equals(InfixExpression.Operator.GREATER)) {
					greaterSide = infixExpression.getLeftOperand();
					lesserSide = infixExpression.getRightOperand();
				}
				if (infixExpression.getOperator().equals(InfixExpression.Operator.LESS)) {
					lesserSide = infixExpression.getLeftOperand();
					greaterSide = infixExpression.getRightOperand();
				}
				if (lesserSide != null && greaterSide != null && greaterSide instanceof MethodInvocation) {
					MethodInvocation methodInvocation = (MethodInvocation) greaterSide;
					if (StringUtils.equals("size", methodInvocation.getName().getFullyQualifiedName()) //$NON-NLS-1$
							&& methodInvocation.getExpression() instanceof SimpleName
							&& lesserSide instanceof SimpleName) {
						listName = (SimpleName) methodInvocation.getExpression();
						iterationVariable = (SimpleName) lesserSide;
						ITypeBinding listBinding = listName.resolveTypeBinding();

						if (!ClassRelationUtil.isInheritingContentOfRegistertITypes(listBinding,
								iTypeMap.get(LIST_KEY))) {
							// Iteration objects are no List
							return true;
						}

						ITypeBinding[] genericListTypes = listBinding.getTypeArguments();
						/**
						 * Transform ITypeBinding of generic Type from the list
						 * to its corresponding Type. If the Type is qualified,
						 * add the Type to the imports and use only the
						 * Class-Identifier of the Type as type-name
						 */
						if (genericListTypes != null && genericListTypes.length == 1) {
							listGenericType = NodeBuilder.typeFromBinding(node.getAST(), genericListTypes[0]);
							if (ASTNode.SIMPLE_TYPE == listGenericType.getNodeType()
									&& ASTNode.QUALIFIED_NAME == ((SimpleType) listGenericType).getName()
											.getNodeType()) {
								QualifiedName qualifiedName = (QualifiedName) ((SimpleType) listGenericType).getName();
								addImports.add(qualifiedName.getFullyQualifiedName());
								listGenericType = node.getAST()
										.newSimpleType(node.getAST().newName(qualifiedName.getName().getIdentifier()));

							} else {
								// TODO check all different possible Types
								return false;
							}
						}
					} else {
						return true;
					}
				} else {
					return true;
				}
			}

			/*
			 * second condition: updater is only an increment on variable
			 */
			{
				// Check if its only an In
				CheckIfOnlyIncrementASTVisitor checkIfOnlyIncrement = new CheckIfOnlyIncrementASTVisitor(
						iterationVariable);
				((ASTNode) node.updaters().get(0)).accept(checkIfOnlyIncrement);
				if (!checkIfOnlyIncrement.isEligible()) {
					return true;
				}
			}

			/*
			 * third condition: iteration variable is initialized to 0 and only
			 * used in while
			 */
			{
				CheckVariableInitializationASTVisitor checkVariableInitialization = new CheckVariableInitializationASTVisitor(
						iterationVariable);
				((ASTNode) node.initializers().get(0)).accept(checkVariableInitialization);
				if (checkVariableInitialization.isEligible()) {
					if (checkVariableInitialization.getVariableType() == null) {
						Block surroundingBlock = ASTNodeUtil.getSurroundingBlock(node);
						if (surroundingBlock != null) {
							VariableDefinitionASTVisitor variableDefinitionAstVisitor = new VariableDefinitionASTVisitor(
									iterationVariable, node);
							surroundingBlock.accept(variableDefinitionAstVisitor);
							if (!variableDefinitionAstVisitor.isUseableLoopVariable()) {
								return true;
							}
						}
					}
				}
			}
			/**
			 * forth condition: the list is only used with get in the loop body
			 */
			ListOnlyGetMethodInvocationASTVisitor listOnlyGetMethodInvocationASTVisitor = new ListOnlyGetMethodInvocationASTVisitor(
					iterationVariable, listName);
			node.getBody().accept(listOnlyGetMethodInvocationASTVisitor);
			if (!listOnlyGetMethodInvocationASTVisitor.isEligible()) {
				return false;
			}

			/*
			 * All Conditions met Do refactoring
			 */
			String listIteratorName = listName.getFullyQualifiedName() + "Iterator"; //$NON-NLS-1$
			SingleVariableDeclaration svd = NodeBuilder.newSingleVariableDeclaration(node.getAST(),
					NodeBuilder.newSimpleName(node.getAST(), listIteratorName), listGenericType);

			EnhancedForStatement efs = NodeBuilder.newEnhancedForStatement(node.getAST(),
					(Block) astRewrite.createMoveTarget(node.getBody()),
					(SimpleName) astRewrite.createMoveTarget(listName), svd);
			efs.toString();
			astRewrite.replace(node, efs, null);
			for (MethodInvocation methodIterator : listOnlyGetMethodInvocationASTVisitor.getMethodInvocationList()) {
				astRewrite.replace(methodIterator, NodeBuilder.newSimpleName(node.getAST(), listIteratorName), null);
			}
		}
		return true;
	}

	private class ListOnlyGetMethodInvocationASTVisitor extends ASTVisitor {

		private boolean eligible = true;
		private List<MethodInvocation> methodInvocationList = new ArrayList<>();
		private ASTMatcher astMatcher = new ASTMatcher();

		private SimpleName iterationVariable;
		private SimpleName listVariable;

		public ListOnlyGetMethodInvocationASTVisitor(SimpleName iterationVariable, SimpleName listVariable) {
			this.listVariable = listVariable;
			this.iterationVariable = iterationVariable;
		}

		/**
		 * Checks if only legal use on the collection is performed. Get is the
		 * only legal methodInvocation
		 */
		@Override
		public boolean visit(MethodInvocation node) {
			if (astMatcher.match(listVariable, node.getExpression())) {
				if ("get".equals(node.getName().getFullyQualifiedName())) { //$NON-NLS-1$
					if (node.arguments().size() == 1 && astMatcher.match(iterationVariable, node.arguments().get(0))) {
						eligible = eligible && addToMethodListClearIfNull(node);
					} else {
						eligible = eligible && addToMethodListClearIfNull(node);
						return false;
					}

				}
				// other function called on the list
				else {
					eligible = eligible && addToMethodListClearIfNull(node);
					return false;
				}
			}
			return true;
		}

		/**
		 * 
		 * @return
		 */
		private boolean addToMethodListClearIfNull(MethodInvocation methodInvocation) {
			if (methodInvocation == null) {
				methodInvocationList.clear();
				return false;
			} else {
				methodInvocationList.add(methodInvocation);
				return true;
			}
		}

		public boolean isEligible() {
			return eligible;
		}

		public List<MethodInvocation> getMethodInvocationList() {
			return methodInvocationList;
		}
	}

	private class CheckIfOnlyIncrementASTVisitor extends ASTVisitor {

		private boolean eligible = false;
		private SimpleName iterationVariable;
		private ASTMatcher astMatcher = new ASTMatcher();

		public CheckIfOnlyIncrementASTVisitor(SimpleName iterationVariable) {
			this.iterationVariable = iterationVariable;
		}

		@Override
		public boolean visit(PostfixExpression node) {
			if (astMatcher.match(iterationVariable, node.getOperand())
					&& PostfixExpression.Operator.INCREMENT.equals(node.getOperator())) {
				eligible = true;
			}
			return false;
		}

		@Override
		public boolean visit(PrefixExpression node) {
			if (astMatcher.match(iterationVariable, node.getOperand())
					&& PrefixExpression.Operator.INCREMENT.equals(node.getOperator())) {
				eligible = true;
			}
			return false;
		}

		@Override
		public boolean visit(Assignment node) {
			if (astMatcher.match(iterationVariable, node.getLeftHandSide())) {
				if (node.getRightHandSide() instanceof InfixExpression) {
					InfixExpression infixExpression = (InfixExpression) node.getRightHandSide();
					if ((infixExpression.extendedOperands() == null || infixExpression.extendedOperands().isEmpty())
							&& InfixExpression.Operator.PLUS.equals(infixExpression.getOperator())) {
						Expression leftOperand = infixExpression.getLeftOperand();
						Expression rightOperand = infixExpression.getRightOperand();
						if (rightOperand instanceof SimpleName && leftOperand instanceof NumberLiteral) {
							eligible = allowedIncrement((SimpleName) rightOperand, (NumberLiteral) leftOperand);
						}
						if (leftOperand instanceof SimpleName && rightOperand instanceof NumberLiteral) {
							eligible = allowedIncrement((SimpleName) leftOperand, (NumberLiteral) rightOperand);
						}
					}
				}
			}
			return false;
		}

		private boolean allowedIncrement(SimpleName identifier, NumberLiteral increment) {
			return astMatcher.match(iterationVariable, identifier) && "1".equals(increment.getToken()); //$NON-NLS-1$
		}

		public boolean isEligible() {
			return eligible;
		}
	}

	private class CheckVariableInitializationASTVisitor extends ASTVisitor {

		private SimpleName iterationVariable;
		private ASTMatcher astMatcher = new ASTMatcher();

		private boolean eligible = false;
		private Type variableType;

		public CheckVariableInitializationASTVisitor(SimpleName iterationVariable) {
			this.iterationVariable = iterationVariable;
		}

		@Override
		public void endVisit(VariableDeclarationExpression node) {
			if (eligible) {
				// useless? get of Collection must be an int/Integer!
				variableType = node.getType();
			}
		}

		@Override
		public boolean visit(Assignment node) {
			if (node.getRightHandSide() instanceof NumberLiteral) {
				eligible = testLiteralIsZeroAndVariableMatches(node.getLeftHandSide(),
						(NumberLiteral) node.getRightHandSide());
			}
			return false;
		}

		@Override
		public boolean visit(VariableDeclarationFragment node) {
			if (node.getInitializer() instanceof NumberLiteral) {
				eligible = testLiteralIsZeroAndVariableMatches(node.getName(), (NumberLiteral) node.getInitializer());
			}
			return false;
		}

		public boolean testLiteralIsZeroAndVariableMatches(Expression variable, NumberLiteral literal) {
			return "0".equals(literal.getToken()) && astMatcher.match(iterationVariable, variable); //$NON-NLS-1$
		}

		public boolean isEligible() {
			return eligible;
		}

		public Type getVariableType() {
			return variableType;
		}
	}
}

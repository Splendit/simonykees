package at.splendit.simonykees.core.visitor.loop;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractAddImportASTVisitor;
import at.splendit.simonykees.core.visitor.sub.VariableDeclarationsVisitor;

/**
 * For loops with an iterator can be replaced with a forEach loop since 1.7
 * 
 * @author Martin Huter, Ardit Ymeri
 * @since 0.9.2
 */
public class ForToForEachASTVisitor extends AbstractAddImportASTVisitor {

	private static final String ITERATOR_FULLY_QUALLIFIED_NAME = java.util.Iterator.class.getName();
	private static final String ITERABLE_FULLY_QUALIFIED_NAME = java.lang.Iterable.class.getName();
	private static final String SIZE = "size"; //$NON-NLS-1$
	private static final String LENGTH = "length"; //$NON-NLS-1$
	private static final String DEFAULT_ITERATOR_NAME = "iterator"; //$NON-NLS-1$
	private static final String KEY_SEPARATOR = "->"; //$NON-NLS-1$
	private static final String DOT = "."; //$NON-NLS-1$

	private Map<ForStatement, LoopOptimizationASTVisior> replaceInformationASTVisitorList;
	private Map<String, Integer> multipleIteratorUse;
	private CompilationUnit compilationUnit;
	private Map<String, String> tempIntroducedNames;
	private Set<String> newImports = new HashSet<>();

	public ForToForEachASTVisitor() {
		this.replaceInformationASTVisitorList = new HashMap<>();
		this.multipleIteratorUse = new HashMap<>();
		this.tempIntroducedNames = new HashMap<>();
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		return true;
	}

	@Override
	public void endVisit(CompilationUnit cu) {
		PackageDeclaration cuPackage = cu.getPackage();
		Name packageName = cuPackage.getName();
		String packageQualifiedName = packageName.getFullyQualifiedName();
		List<AbstractTypeDeclaration> cuDeclaredTypes = ASTNodeUtil.convertToTypedList(compilationUnit.types(),
				AbstractTypeDeclaration.class);

		List<String> toBeAdded = newImports.stream()
				.filter(newImport -> !isInSamePackage(newImport, packageQualifiedName, cuDeclaredTypes))
				.collect(Collectors.toList());
		super.addImports.addAll(toBeAdded);
		super.endVisit(cu);
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
					/*
					 * No surrounding parent block found should not happen,
					 * because the Iterator has to be defined in an parent
					 * block.
					 */
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
			Expression lhs = infixExpression.getLeftOperand();

			// if the expression operator is '<' and lhs is a simple name...
			if (InfixExpression.Operator.LESS.equals(infixExpression.getOperator())
					&& Expression.SIMPLE_NAME == lhs.getNodeType()) {
				SimpleName index = (SimpleName) lhs;

				if (ASTNode.METHOD_INVOCATION == rhs.getNodeType()) {
					// iterating over Lists
					MethodInvocation condition = (MethodInvocation) rhs;
					Expression conditionExpression = condition.getExpression();
					if (conditionExpression != null && Expression.SIMPLE_NAME == conditionExpression.getNodeType()) {
						SimpleName iterableNode = (SimpleName) conditionExpression;
						ITypeBinding iterableTypeBinding = iterableNode.resolveTypeBinding();

						/*
						 * ...and the right hand side of the infix expression is
						 * an invocation of List::size in the iterable object
						 */
						if (ClassRelationUtil.isInheritingContentOfTypes(iterableTypeBinding,
								Collections.singletonList(ITERABLE_FULLY_QUALIFIED_NAME))
								&& StringUtils.equals(SIZE, condition.getName().getIdentifier())
								&& condition.arguments().isEmpty()) {

							/*
							 * Initiate a visitor for investigating the
							 * replacement precondition and gathering the
							 * replacement information
							 */
							Block outerBlock = ASTNodeUtil.getSpecificAncestor(node, Block.class);
							ForLoopIteratingIndexASTVisitor indexVisitor = new ForLoopOverListsASTVisitor(index,
									iterableNode, node, outerBlock);
							outerBlock.accept(indexVisitor);

							if (indexVisitor.checkTransformPrecondition()) {
								Type iteratorType = findIteratorType(iterableTypeBinding);
								if (iteratorType != null) {
									replaceWithEnhancedFor(node, iterableNode, indexVisitor, iteratorType);
								}
							}
						}
					}

				} else if (ASTNode.QUALIFIED_NAME == rhs.getNodeType()) {
					// iterating over arrays
					QualifiedName condition = (QualifiedName) rhs;
					Name qualifier = condition.getQualifier();
					SimpleName name = condition.getName();

					if (LENGTH.equals(name.getIdentifier()) && qualifier.isSimpleName()) {
						SimpleName iterableNode = (SimpleName) qualifier;
						ITypeBinding iterableTypeBinding = qualifier.resolveTypeBinding();
						if (iterableTypeBinding != null && iterableTypeBinding.isArray()) {

							Block outerBlock = ASTNodeUtil.getSpecificAncestor(node, Block.class);
							ForLoopIteratingIndexASTVisitor indexVisitor = new ForLoopOverArraysASTVisitor(index,
									iterableNode, node, outerBlock);
							outerBlock.accept(indexVisitor);

							if (indexVisitor.checkTransformPrecondition()) {
								Type iteratorType = findIteratorType(iterableTypeBinding);
								if (iteratorType != null) {
									replaceWithEnhancedFor(node, iterableNode, indexVisitor, iteratorType);
								}
							}

						}
					}
				}
			}
		}
		return true;
	}

	private void replaceWithEnhancedFor(ForStatement node, SimpleName iterableNode,
			ForLoopIteratingIndexASTVisitor indexVisitor, Type iteratorType) {
		/*
		 * invocations of List::get to be replaced with the iterator object
		 */
		List<ASTNode> toBeReplaced = indexVisitor.getIteratingObjectInitializers();
		List<ASTNode> toBeRemoved = indexVisitor.getNodesToBeRemoved();
		SimpleName preferredIteratorName = indexVisitor.getIteratorName();
		Statement loopBody = node.getBody();
		// generate a safe iterator name
		Map<String, Boolean> nameMap = generateNewIteratorName(preferredIteratorName, loopBody);
		String newIteratorIdentifier = nameMap.keySet().iterator().next();
		storeTempName(node, newIteratorIdentifier);
		boolean eligiblePreferredName = nameMap.get(newIteratorIdentifier);
		if (eligiblePreferredName && indexVisitor.getPreferredNameFragment() != null) {
			toBeRemoved.add(indexVisitor.getPreferredNameFragment());
		}

		// remove the redundant nodes
		toBeRemoved.forEach(remove -> {
			if (remove.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
				VariableDeclarationStatement declStatement = (VariableDeclarationStatement) remove.getParent();
				if (declStatement.fragments().size() == 1) {
					astRewrite.remove(declStatement, null);
				}
			}
			astRewrite.remove(remove, null);
		});

		AST ast = astRewrite.getAST();

		/*
		 * replace the List::get invocations with the new iterator
		 */
		toBeReplaced.forEach(target -> astRewrite.replace(target, ast.newSimpleName(newIteratorIdentifier), null));

		// create a declaration of the new iterator
		SingleVariableDeclaration iteratorDecl = NodeBuilder.newSingleVariableDeclaration(loopBody.getAST(),
				ast.newSimpleName(newIteratorIdentifier), iteratorType);

		// create the new enhanced for loop
		EnhancedForStatement newFor = NodeBuilder.newEnhancedForStatement(loopBody.getAST(),
				(Statement) astRewrite.createCopyTarget(loopBody),
				(Expression) astRewrite.createCopyTarget(iterableNode), iteratorDecl);

		// replace the existing for loop with
		astRewrite.replace(node, newFor, null);
	}

	private void storeTempName(ForStatement node, String newIteratorIdentifier) {
		String key = generateTempIteratorKey(node);
		tempIntroducedNames.put(key, newIteratorIdentifier);

	}

	/**
	 * Finds the {@link Type} of the new iterator object from the type of the
	 * iterable object. If the type is a wild card then gets its upper bound.
	 * Furthermore, it collects the names of the new import statements that are
	 * necessary to be added after introducing the iterator object.
	 * 
	 * @param iterableNode
	 *            node expected to represent a parameterized type object
	 * @return type binding of the iterator
	 */
	private Type findIteratorType(ITypeBinding iterableTypeBinding) {
		Type iteratorType = null;
		ITypeBinding iteratorTypeBinding = null;
		if (iterableTypeBinding.isParameterizedType()) {

			ITypeBinding[] typeArguments = iterableTypeBinding.getTypeArguments();
			if (typeArguments.length == 1) {
				iteratorTypeBinding = typeArguments[0];
				if (iteratorTypeBinding != null && iteratorTypeBinding.getTypeBounds().length > 0) {
					iteratorTypeBinding = iteratorTypeBinding.getTypeBounds()[0];
				}
			}
		} else if (iterableTypeBinding.isArray()) {
			iteratorTypeBinding = iterableTypeBinding.getComponentType();
		}

		if (iteratorTypeBinding != null && !iteratorTypeBinding.getName().isEmpty()) {
			ASTRewrite astRewrite = getAstRewrite();
			ImportRewrite importRewrite = ImportRewrite.create(compilationUnit, true);
			iteratorType = importRewrite.addImport(iteratorTypeBinding, astRewrite.getAST());
			if (!iteratorTypeBinding.isMember()) {
				String[] addedImports = importRewrite.getAddedImports();
				for (String addedImport : addedImports) {
					if (!addedImport.startsWith(JAVA_LANG_PACKAGE)) {
						newImports.add(addedImport);
					}
				}
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
		if (preferedName == null || declaredNames.contains(preferedName.getIdentifier())
				|| tempIntroducedNames.containsValue(preferedName)) {
			allowedPreferedName = false;
			int counter = 0;
			String suffix = ""; //$NON-NLS-1$
			ASTNode scope = findScopeOfLoop(loopBody);
			VariableDeclarationsVisitor loopScopeVisitor = new VariableDeclarationsVisitor();
			scope.accept(loopScopeVisitor);
			List<SimpleName> scopeDeclaredNames = loopScopeVisitor.getVariableDeclarationNames();

			declaredNames = scopeDeclaredNames.stream().map(SimpleName::getIdentifier).collect(Collectors.toList());
			while (declaredNames.contains(DEFAULT_ITERATOR_NAME + suffix)
					|| tempIntroducedNames.containsValue(DEFAULT_ITERATOR_NAME + suffix)) {
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

	/**
	 * Finds the scope where the statement belongs to. A scope is either the
	 * body of:
	 * <ul>
	 * <li>a method</li>
	 * <li>an initializer</li>
	 * <li>a class/interface</li>
	 * <li>an enumeration</li>
	 * <li>an annotation declaration</li>
	 * </ul>
	 * 
	 * @param statement
	 *            a statement to look for the scope where it falls into.
	 * @return an {@link ASTNode} representing either of the above
	 */
	private ASTNode findScopeOfLoop(Statement statement) {
		ASTNode parent = statement.getParent();
		while (parent != null && parent.getNodeType() != ASTNode.METHOD_DECLARATION
				&& parent.getNodeType() != ASTNode.INITIALIZER && parent.getNodeType() != ASTNode.TYPE_DECLARATION
				&& parent.getNodeType() != ASTNode.ENUM_DECLARATION
				&& parent.getNodeType() != ASTNode.ANNOTATION_TYPE_DECLARATION) {

			parent = parent.getParent();
		}
		return parent;
	}

	/**
	 * Checks whether the new import points to a class in the same package or in
	 * the same file as the compilation unit.
	 * 
	 * @param newImport
	 *            qualified name of the new import
	 * @param cuPackageQualifiedName
	 *            qualified name of the compilation unit's package
	 * @param cuDeclaredTypes
	 *            types declared in the compilation unit.
	 * @return true if the new import points to a type in the same package as
	 *         the compilation unit or to a type declared inside the compilation
	 *         unit.
	 */
	private boolean isInSamePackage(String newImport, String cuPackageQualifiedName,
			List<AbstractTypeDeclaration> cuDeclaredTypes) {
		boolean isInSamePackage = false;

		if (newImport.startsWith(cuPackageQualifiedName)) {
			int dotLastIndex = newImport.lastIndexOf(DOT);
			String suffix = newImport.substring(dotLastIndex);
			List<String> suffixComponents = Arrays.asList(suffix.split(DOT));
			if (suffixComponents.size() > 1) {
				isInSamePackage = cuDeclaredTypes.stream().map(type -> type.getName().getIdentifier())
						.filter(name -> name.equals(suffixComponents.get(0))).findAny().isPresent();
			} else {
				isInSamePackage = true;
			}
		}

		return isInSamePackage;
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

		this.tempIntroducedNames.remove(generateTempIteratorKey(node));
	}

	private String generateTempIteratorKey(ForStatement node) {
		return node.getStartPosition() + KEY_SEPARATOR + node.getLength();
	}
}
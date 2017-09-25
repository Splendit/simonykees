package eu.jsparrow.core.visitor.loop;

import java.util.ArrayList;
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
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

import eu.jsparrow.core.builder.NodeBuilder;
import eu.jsparrow.core.util.ASTNodeUtil;
import eu.jsparrow.core.util.ClassRelationUtil;
import eu.jsparrow.core.visitor.AbstractAddImportASTVisitor;
import eu.jsparrow.core.visitor.sub.VariableDeclarationsVisitor;

/**
 * A superclass of the visitors converting a loop ({@link ForStatement} or
 * {@link WhileStatement}) to a {@link EnhancedForStatement}.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 * @param <T>
 *            type of the target loop statement, expected to be either a
 *            ({@link ForStatement} or a {@link WhileStatement}).
 */
public abstract class LoopToForEachASTVisitor<T extends Statement> extends AbstractAddImportASTVisitor {

	protected static final String ITERATOR_FULLY_QUALLIFIED_NAME = java.util.Iterator.class.getName();
	protected static final String ITERABLE_FULLY_QUALIFIED_NAME = java.lang.Iterable.class.getName();
	protected static final String SIZE = "size"; //$NON-NLS-1$
	protected static final String LENGTH = "length"; //$NON-NLS-1$
	protected static final String DEFAULT_ITERATOR_NAME = "iterator"; //$NON-NLS-1$
	protected static final String KEY_SEPARATOR = "->"; //$NON-NLS-1$

	private CompilationUnit compilationUnit;
	private Map<String, String> tempIntroducedNames;
	private Set<String> newImports = new HashSet<>();
	private Map<String, List<ITypeBinding>> innerTypesMap = new HashMap<>();
	private List<ITypeBinding> topLevelTypes = new ArrayList<>();

	protected LoopToForEachASTVisitor() {
		this.tempIntroducedNames = new HashMap<>();
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		DeclaredTypesASTVisitor declaredTypesVisitor = new DeclaredTypesASTVisitor();
		compilationUnit.accept(declaredTypesVisitor);
		innerTypesMap = declaredTypesVisitor.getDeclaredTypes();
		topLevelTypes = declaredTypesVisitor.getTopLevelTypes();
		return true;
	}

	@Override
	public void endVisit(CompilationUnit cu) {
		super.addImports.addAll(filterNewImportsByExcludingCurrentPackage(this.compilationUnit, newImports));
		super.endVisit(cu);
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
	protected Type findIteratorType(Statement loop, ITypeBinding iterableTypeBinding) {
		Type iteratorType = null;
		ITypeBinding iteratorTypeBinding = null;
		if (iterableTypeBinding.isParameterizedType()) {

			ITypeBinding[] typeArguments = iterableTypeBinding.getTypeArguments();
			if (typeArguments.length == 1) {
				iteratorTypeBinding = typeArguments[0];
				if (iteratorTypeBinding == null) {
					return null;
				}

				if (!iteratorTypeBinding.isTypeVariable() && iteratorTypeBinding.getTypeBounds().length > 0) {
					iteratorTypeBinding = iteratorTypeBinding.getTypeBounds()[0];
				}
			}
		} else if (iterableTypeBinding.isArray()) {
			iteratorTypeBinding = iterableTypeBinding.getComponentType();
		}

		if (iteratorTypeBinding == null || StringUtils.isEmpty(iteratorTypeBinding.getName())) {
			return null;
		}

		ASTRewrite astRewrite = getAstRewrite();
		ImportRewrite importRewrite = ImportRewrite.create(compilationUnit, true);
		String[] addedImports;

		if (iteratorTypeBinding.isMember() && !enclosedInSameType(loop, iteratorTypeBinding)) {
			/*
			 * the type of the iterator is an inner type which is not 
			 * declared in the same class enclosing the loop node.
			 */
			ITypeBinding outerType = iteratorTypeBinding.getDeclaringClass();
			importRewrite.addImport(outerType, astRewrite.getAST());
			addedImports = importRewrite.getAddedImports();
			String fullyQualifiedName = iteratorTypeBinding.getErasure().getQualifiedName();
			int outerTypeStartingIndex = fullyQualifiedName.lastIndexOf(outerType.getErasure().getName());
			Name qualifiedName = astRewrite.getAST().newName(StringUtils.substring(fullyQualifiedName, outerTypeStartingIndex));
			iteratorType = convertToQualifiedName(importRewrite.addImport(iteratorTypeBinding, astRewrite.getAST()),
					qualifiedName);
		} else {
			/*
			 * ImportRewrite::addImport is a work around for creating a Type
			 * from an ITypeBinding
			 */
			iteratorType = importRewrite.addImport(iteratorTypeBinding, astRewrite.getAST());
			addedImports = importRewrite.getAddedImports();
			
			if (qualifiedNameNeeded(loop, iteratorTypeBinding)) {
				iteratorType = convertToQualifiedName(iteratorType, iteratorTypeBinding.getErasure());
			}
		}

		Arrays.stream(addedImports).filter(addedImport -> !StringUtils.startsWith(addedImport, JAVA_LANG_PACKAGE))
				.forEach(newImports::add);

		return iteratorType;
	}

	/**
	 * Checks whether the loop statement and the declaration of the given type
	 * are enclosed in the same class.
	 * 
	 * @param loop
	 *            a node expected to represent a loop statement.
	 * @param iteratorTypeBinding
	 *            a type binding expected to represent the type of the elements
	 *            where the loop iterates through.
	 * @return {@code true} if the loop and the type declaration are wrapped by
	 *         the same class or {@code false} otherwise.
	 */
	private boolean enclosedInSameType(Statement loop, ITypeBinding iteratorTypeBinding) {
		AbstractTypeDeclaration enclosingType = ASTNodeUtil.getSpecificAncestor(loop, AbstractTypeDeclaration.class);
		if (enclosingType != null && iteratorTypeBinding != null) {
			ITypeBinding enclosingTypeBinding = enclosingType.resolveBinding();
			if (enclosingTypeBinding != null
					&& (ClassRelationUtil.compareITypeBinding(enclosingTypeBinding.getErasure(), iteratorTypeBinding.getErasure())
							|| ClassRelationUtil.compareITypeBinding(enclosingTypeBinding.getErasure(),
									iteratorTypeBinding.getDeclaringClass().getErasure()))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Converts the {@link SimpleType}s, the {@link ArrayType}s and the
	 * {@link ParameterizedType}s to types with qualified name.
	 * 
	 * @param type
	 *            original type to be converted.
	 * @param typeBinding
	 *            a type binding to get the qualified name from.
	 * 
	 * @return the given type binding having a qualified name property.
	 */
	private Type convertToQualifiedName(Type type, ITypeBinding typeBinding) {
		AST ast = type.getAST();
		Name qualifiedName = ast.newName(typeBinding.getQualifiedName());
		return convertToQualifiedName(type, qualifiedName);
	}
	
	/**
	 * Sets the given name as the type property of the given {@link Type} node.
	 * Considers {@link SimpleType}s, {@link ArrayType}s and
	 * {@link ParameterizedType}s.
	 * 
	 * @param type
	 *            the type to be modified
	 * @param qualifiedName
	 *            new name of the type.
	 * 
	 * @return the type node having the new name property or the unmodified type
	 *         node if it doesn't fall in any of the aforementioned types.
	 */
	private Type convertToQualifiedName(Type type, Name qualifiedName) {
		AST ast = type.getAST();
		if (type.isArrayType()) {
			ArrayType arrayType = (ArrayType) type;
			SimpleType simpleType = ast.newSimpleType(qualifiedName);
			arrayType.setStructuralProperty(ArrayType.ELEMENT_TYPE_PROPERTY, simpleType);
			return arrayType;
		} else if (type.isSimpleType()) {
			SimpleType simpleType = (SimpleType) type;
			simpleType.setName(qualifiedName);
			return simpleType;
		} else if (type.isParameterizedType()) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			SimpleType simpleType = ast.newSimpleType(qualifiedName);
			parameterizedType.setStructuralProperty(ParameterizedType.TYPE_PROPERTY, simpleType);
			return parameterizedType;
		}

		return type;
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
	protected Map<String, Boolean> generateNewIteratorName(SimpleName preferedName, Statement loopBody,
			Name iterableName) {
		VariableDeclarationsVisitor loopBodyDeclarationsVisitor = new VariableDeclarationsVisitor();
		loopBody.accept(loopBodyDeclarationsVisitor);
		List<SimpleName> loobBodyDeclarations = loopBodyDeclarationsVisitor.getVariableDeclarationNames();
		List<String> declaredNames = loobBodyDeclarations.stream().filter(name -> name != preferedName)
				.map(SimpleName::getIdentifier).collect(Collectors.toList());

		String newName;
		Boolean allowedPreferedName;
		if (preferedName == null || declaredNames.contains(preferedName.getIdentifier())
				|| tempIntroducedNames.containsValue(preferedName.getIdentifier())) {
			allowedPreferedName = false;
			int counter = 0;
			String suffix = ""; //$NON-NLS-1$
			ASTNode scope = ASTNodeUtil.findScope(loopBody);
			VariableDeclarationsVisitor loopScopeVisitor = new VariableDeclarationsVisitor();
			scope.accept(loopScopeVisitor);
			List<SimpleName> scopeDeclaredNames = loopScopeVisitor.getVariableDeclarationNames();
			String defaultIteratorName = createDefaultIteratorName(iterableName);
			declaredNames = scopeDeclaredNames.stream().map(SimpleName::getIdentifier).collect(Collectors.toList());
			while (declaredNames.contains(defaultIteratorName + suffix)
					|| tempIntroducedNames.containsValue(defaultIteratorName + suffix)) {
				counter++;
				suffix = Integer.toString(counter);
			}
			newName = defaultIteratorName + suffix;
		} else {
			allowedPreferedName = true;
			newName = preferedName.getIdentifier();
		}

		Map<String, Boolean> nameMap = new HashMap<>();
		nameMap.put(newName, allowedPreferedName);

		return nameMap;
	}

	/**
	 * Constructs the default name of the iterator object. If the iterable name
	 * ends with an {@code s}, the constructed name is created by removing the
	 * ending {@code s}. Otherwise, either the prefix {@code a} or {@code an} is
	 * added to the iterable name depending on whether it starts with a vowel or
	 * not.
	 * 
	 * @param iterableName
	 *            the name of the iterable object
	 * 
	 * @return the new name for the iterating object.
	 */
	private String createDefaultIteratorName(Name iterableName) {
		SimpleName simpleName;
		if (iterableName.isQualifiedName()) {
			simpleName = ((QualifiedName) iterableName).getName();
		} else {
			simpleName = (SimpleName) iterableName;
		}

		String identifier = simpleName.getIdentifier();
		if (identifier.length() > 1 && StringUtils.endsWith(identifier, "s")) { //$NON-NLS-1$
			return StringUtils.substring(identifier, 0, identifier.length() - 1);
		} else {
			return addSingularPrefix(identifier);
		}
	}

	/**
	 * Adds the prefix {@code a} or {@code an} depending on whether the
	 * identifier starts with a vowel or not.
	 * 
	 * @param identifier
	 *            a string representing a variable name.
	 * 
	 * @return {@code a}/{@code an} + {@code identifier} converted to camel
	 *         cased.
	 */
	private String addSingularPrefix(String identifier) {

		String firstLetter = StringUtils.substring(identifier, 0, 1);
		String remaining = StringUtils.substring(identifier, 1);
		String prefix;
		if (isVowel(identifier.charAt(0))) {
			prefix = "an"; //$NON-NLS-1$
		} else {
			prefix = "a"; //$NON-NLS-1$
		}
		return prefix + StringUtils.upperCase(firstLetter) + remaining;
	}

	/**
	 * Checks if a character is a vowel.
	 * 
	 * @param c
	 *            character to be checked
	 * @return {@code true} if the character is a vowel or {@code false}
	 *         otherwise.
	 */
	private boolean isVowel(char c) {
		return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' || c == 'y';
	}

	protected void storeTempName(Statement node, String newIteratorIdentifier) {
		String key = generateTempIteratorKey(node);
		tempIntroducedNames.put(key, newIteratorIdentifier);

	}

	/**
	 * Performs the replacements of a loop (either {@link ForStatement} or
	 * {@link WhileStatement}) with an {@link EnhancedForStatement}. Removes the
	 * redundant nodes.
	 * 
	 * @param loop
	 *            a node representing the loop to be replaced
	 * @param loopBody
	 *            body of the loop to be replaced
	 * @param iterableNode
	 *            the node representing the object that the loops iterates
	 *            through
	 * @param indexVisitor
	 *            a node representing the iterating index
	 * @param iteratorType
	 *            the type binding of the elements of the iterable object.
	 */
	protected void replaceWithEnhancedFor(Statement loop, Statement loopBody, SimpleName iterableNode,
			LoopIteratingIndexASTVisitor indexVisitor, Type iteratorType) {
		/*
		 * invocations of List::get to be replaced with the iterator object
		 */
		List<ASTNode> toBeReplaced = indexVisitor.getIteratingObjectInitializers();
		List<ASTNode> toBeRemoved = indexVisitor.getNodesToBeRemoved();
		SimpleName preferredIteratorName = indexVisitor.getIteratorName();

		// generate a safe iterator name
		Map<String, Boolean> nameMap = generateNewIteratorName(preferredIteratorName, loopBody, iterableNode);
		String newIteratorIdentifier = nameMap.keySet().iterator().next();
		storeTempName(loop, newIteratorIdentifier);
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
		astRewrite.replace(loop, newFor, null);
	}

	/**
	 * Checks whether a qualified name is needed for the declaration of a
	 * variable of the given type in the given statement (in this case, is
	 * expected to be a loop). If the loop's variable type is an inner type
	 * declared in another compilation unit, then a qualified name is 
	 * needed for the declaration of the loop variable.  
	 * 
	 * @param loopStatement
	 *            a node representing a loop statement.
	 * @param iteratorType
	 *            the type binding of the iterating object.
	 * @return {@code true} if qualified name is needed for the iterator
	 *         declaration or {@code false} otherwise.
	 */
	private boolean qualifiedNameNeeded(Statement loopStatement, ITypeBinding iteratorType) {
		// the type wrapping the loop statement.
		AbstractTypeDeclaration currentClass = ASTNodeUtil.getSpecificAncestor(loopStatement,
				AbstractTypeDeclaration.class);

		/*
		 * used for collecting the types that are visible in the given
		 * loopStatement
		 */
		List<ITypeBinding> types = new ArrayList<>();
		types.addAll(this.innerTypesMap.get(currentClass.resolveBinding().getQualifiedName()));
		ASTNode parent = currentClass.getParent();

		if (parent == this.compilationUnit) {
			// the wrapping type is a top level type.
			types.addAll(topLevelTypes);
		} else if (parent != null) {
			// the wrapping type is an inner type
			ITypeBinding parentBinding = ((AbstractTypeDeclaration) parent).resolveBinding();
			if (parentBinding != null) {
				types.addAll(this.innerTypesMap.get(parentBinding.getQualifiedName()));
			}
		}
		
		ITypeBinding iteratorErasure = iteratorType.getErasure();

		return
		// iterator type is not an inner type
		types.stream().map(ITypeBinding::getErasure).map(ITypeBinding::getQualifiedName)
				.noneMatch(qualifiedName -> qualifiedName.equals(iteratorErasure.getQualifiedName())) &&
		// iterator type clashes with an inner type
				types.stream().map(ITypeBinding::getName).anyMatch(name -> name.equals(iteratorErasure.getName()));

	}

	private String generateTempIteratorKey(Statement node) {
		return node.getStartPosition() + KEY_SEPARATOR + node.getLength();
	}

	protected void clearTempItroducedNames(Statement node) {
		this.tempIntroducedNames.remove(generateTempIteratorKey(node));

	}

	/**
	 * Analyzes a loop over arrays and replaces it with an
	 * {@link EnhancedForStatement} if possible. Supports while loops and for
	 * loop using an iterating index.
	 * 
	 * @param loop
	 *            a node representing the whole loop.
	 * @param body
	 *            a node representing the body of the loop
	 * @param condition
	 *            a qualified name accessing the length property of an array
	 * @param index
	 *            a simple name representing the iterating index
	 * @param factory
	 *            a pointer to the corresponding helper visitor constructor
	 */
	protected void analyzeLoopOverArray(T loop, Statement body, QualifiedName condition, SimpleName index,
			IteratingIndexVisitorFactory<T> factory) {

		Name qualifier = condition.getQualifier();
		SimpleName name = condition.getName();

		if (LENGTH.equals(name.getIdentifier()) && qualifier.isSimpleName()) {
			SimpleName iterableNode = (SimpleName) qualifier;
			ITypeBinding iterableTypeBinding = qualifier.resolveTypeBinding();
			if (iterableTypeBinding != null && iterableTypeBinding.isArray()) {

				Block outerBlock = ASTNodeUtil.getSpecificAncestor(loop, Block.class);
				LoopIteratingIndexASTVisitor indexVisitor = createIteratingIndexVisitor(index, iterableNode, loop,
						outerBlock, factory);
				outerBlock.accept(indexVisitor);

				if (indexVisitor.checkTransformPrecondition()) {
					Type iteratorType = findIteratorType(loop, iterableTypeBinding);
					if (iteratorType != null) {
						replaceWithEnhancedFor(loop, body, iterableNode, indexVisitor, iteratorType);
					}
				}

			}
		}
	}

	/**
	 * Analyzes a loop over a {@link List} and replaces it with an
	 * {@link EnhancedForStatement} if possible. Supports while loops and for
	 * loop using an iterating index.
	 * 
	 * @param loop
	 *            a node representing the whole loop.
	 * @param body
	 *            a node representing the body of the loop
	 * @param condition
	 *            the condition expression of the loop
	 * @param index
	 *            a simple name representing the iterating index
	 * @param factory
	 *            a pointer to the corresponding helper visitor constructor
	 */
	protected void analyzeLoopOverList(T loop, Statement body, MethodInvocation condition, SimpleName index,
			IteratingIndexVisitorFactory<T> factory) {

		Expression conditionExpression = condition.getExpression();
		if (conditionExpression != null && Expression.SIMPLE_NAME == conditionExpression.getNodeType()) {
			SimpleName iterableNode = (SimpleName) conditionExpression;
			ITypeBinding iterableTypeBinding = iterableNode.resolveTypeBinding();

			/*
			 * ...and the right hand side of the infix expression is an
			 * invocation of List::size in the iterable object
			 */
			if (ClassRelationUtil.isInheritingContentOfTypes(iterableTypeBinding,
					Collections.singletonList(ITERABLE_FULLY_QUALIFIED_NAME))
					&& StringUtils.equals(SIZE, condition.getName().getIdentifier())
					&& condition.arguments().isEmpty()) {

				/*
				 * Initiate a visitor for investigating the replacement
				 * precondition and gathering the replacement information
				 */
				Block outerBlock = ASTNodeUtil.getSpecificAncestor(loop, Block.class);
				LoopIteratingIndexASTVisitor indexVisitor = createIteratingIndexVisitor(index, iterableNode, loop,
						outerBlock, factory);
				outerBlock.accept(indexVisitor);

				if (indexVisitor.checkTransformPrecondition()) {
					Type iteratorType = findIteratorType(loop, iterableTypeBinding);
					if (iteratorType != null) {
						replaceWithEnhancedFor(loop, body, iterableNode, indexVisitor, iteratorType);
					}
				}
			}
		}
	}

	/**
	 * Makes use of {@link IteratingIndexVisitorFactory} to construct an
	 * instance of {@link LoopIteratingIndexASTVisitor}.
	 * 
	 * @param index
	 *            a simple name representing the iterating index of the loop
	 * @param iterable
	 *            a simple name representing the object that the loop iterates
	 *            through
	 * @param node
	 *            a node representing the whole loop
	 * @param outerBlock
	 *            the outer block of the loop
	 * @param factory
	 *            a pointer to the constructor of a
	 *            {@link LoopIteratingIndexASTVisitor}
	 * 
	 * @return an instance of {@link LoopIteratingIndexASTVisitor}
	 */
	private LoopIteratingIndexASTVisitor createIteratingIndexVisitor(SimpleName index, SimpleName iterable, T node,
			Block outerBlock, IteratingIndexVisitorFactory<T> factory) {
		return factory.create(index, iterable, node, outerBlock);
	}

	/**
	 * Checks if the given node is the body property of a while loop or for
	 * loop.
	 * 
	 * @param node
	 *            node to be checked.
	 * @return {@code true} if the node represents the body of an outer loop or
	 *         {@code false} otherwise.
	 */
	protected boolean isSingleStatementBodyOfOuterLoop(T node) {
		StructuralPropertyDescriptor locationProperty = node.getLocationInParent();

		return ForStatement.BODY_PROPERTY == locationProperty || WhileStatement.BODY_PROPERTY == locationProperty;
	}
}

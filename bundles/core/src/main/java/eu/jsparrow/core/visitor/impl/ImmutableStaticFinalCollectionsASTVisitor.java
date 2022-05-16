package eu.jsparrow.core.visitor.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.markers.common.ImmutableStaticFinalCollectionsEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * The {@link VariableDeclarationFragment}s of the {@link FieldDeclaration} will
 * be visited and checked for their type and modifiers. If a static final
 * {@link Collection} is found, which does not use one of the
 * Collections.unmodifiable...() methods but a normal
 * {@link ClassInstanceCreation} will be altered correspondingly. The
 * {@link ClassInstanceCreation} and the initialisation won't be altered, they
 * will just be passed as an argument to the Collections.unmodifiable...()
 * method.
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class ImmutableStaticFinalCollectionsASTVisitor extends AbstractAddImportASTVisitor
		implements ImmutableStaticFinalCollectionsEvent {

	/*** METHOD NAMES ***/

	private static final String UNMODIFIABLE_COLLECTION = "unmodifiableCollection"; //$NON-NLS-1$
	private static final String UNMODIFIABLE_LIST = "unmodifiableList"; //$NON-NLS-1$
	private static final String UNMODIFIABLE_MAP = "unmodifiableMap"; //$NON-NLS-1$
	private static final String UNMODIFIABLE_NAVIGABLE_MAP = "unmodifiableNavigableMap"; //$NON-NLS-1$
	private static final String UNMODIFIABLE_NAVIGABLE_SET = "unmodifiableNavigableSet"; //$NON-NLS-1$
	private static final String UNMODIFIABLE_SET = "unmodifiableSet"; //$NON-NLS-1$
	private static final String UNMODIFIABLE_SORTED_MAP = "unmodifiableSortedMap"; //$NON-NLS-1$
	private static final String UNMODIFIABLE_SORTED_SET = "unmodifiableSortedSet"; //$NON-NLS-1$

	/*** TYPE NAMES ***/

	private static final String JAVA_UTIL_COLLECTION = java.util.Collection.class.getName();
	private static final String JAVA_UTIL_LIST = java.util.List.class.getName();
	private static final String JAVA_UTIL_MAP = java.util.Map.class.getName();
	private static final String JAVA_UTIL_NAVIGABLE_MAP = java.util.NavigableMap.class.getName();
	private static final String JAVA_UTIL_NAVIGABLE_SET = java.util.NavigableSet.class.getName();
	private static final String JAVA_UTIL_SET = java.util.Set.class.getName();
	private static final String JAVA_UTIL_SORTED_MAP = java.util.SortedMap.class.getName();
	private static final String JAVA_UTIL_SORTED_SET = java.util.SortedSet.class.getName();

	private static final String JAVA_UTIL_COLLECTIONS = java.util.Collections.class.getName();

	/*** TYPE LISTS ***/

	private static final List<String> COLLECTION_TYPE_LIST = Collections.singletonList(JAVA_UTIL_COLLECTION);
	private static final List<String> LIST_TYPE_LIST = Collections.singletonList(JAVA_UTIL_LIST);
	private static final List<String> MAP_TYPE_LIST = Collections.singletonList(JAVA_UTIL_MAP);
	private static final List<String> NAVIGABLE_MAP_TYPE_LIST = Collections.singletonList(JAVA_UTIL_NAVIGABLE_MAP);
	private static final List<String> NAVIGABLE_SET_TYPE_LIST = Collections.singletonList(JAVA_UTIL_NAVIGABLE_SET);
	private static final List<String> SET_TYPE_LIST = Collections.singletonList(JAVA_UTIL_SET);
	private static final List<String> SORTED_MAP_TYPE_LIST = Collections.singletonList(JAVA_UTIL_SORTED_MAP);
	private static final List<String> SORTED_SET_TYPE_LIST = Collections.singletonList(JAVA_UTIL_SORTED_SET);

	/*** HELPER FIELDS ***/

	private Set<String> excludedNames = new HashSet<>();
	private Map<String, String> methodNames = new HashMap<>();
	private Map<String, Expression> initializersToReplace = new HashMap<>();
	private String javaVersion;
	// allowed method names
	@SuppressWarnings("nls")
	private List<String> collectionNonModifyingMethods = Arrays.asList(
			// Collection

			"contains", "containsAll", "equals", "hashCode", "isEmpty", "iterator", "parallelStream", "size",
			"spliteraotr", "stream", "toArray",

			// List

			"get", "indexOf", "isEmpty", "lastIndexOf", "listIterator", "subList",

			// Map

			"containsKey", "containsValue", "entrySet", "forEach", "getOrDefault", "keySet", "values",

			// NavigableMap

			"ceilingEntry", "ceilingKey", "descendingKeySet", "descendingMap", "firstEntry", "floorEntry", "floorKey",
			"headMap", "higherEntry", "higherKey", "lastEntry", "lowerEntry", "lowerKey", "navigableKeySet", "subMap",
			"tailMap",

			// NavigableSet

			"ceiling", "descendingIterator", "descendingSet", "floor", "headSet", "higher", "lower", "subSet",
			"tailSet",

			// SortedMap

			"comparator", "firstKey", "lastKey",

			// SortedSet

			"first", "last");

	public ImmutableStaticFinalCollectionsASTVisitor(String javaVersion) {
		this.javaVersion = javaVersion;
	}

	/*** VISITORS ***/

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (continueVisiting) {
			verifyImport(compilationUnit, JAVA_UTIL_COLLECTIONS);
		}
		return continueVisiting;
	}

	@Override
	public boolean visit(VariableDeclarationFragment fragmentNode) {

		if (fragmentNode.getLocationInParent() != FieldDeclaration.FRAGMENTS_PROPERTY) {
			excludeInitializer(fragmentNode.getInitializer());
			return false;
		}
		FieldDeclaration parent = (FieldDeclaration) fragmentNode.getParent();
		ITypeBinding parentTypeBinding = parent.getType()
			.resolveBinding();

		Expression initializer = fragmentNode.getInitializer();
		if (parentTypeBinding != null && ASTNodeUtil.hasModifier(parent.modifiers(), Modifier::isStatic)
				&& ASTNodeUtil.hasModifier(parent.modifiers(), Modifier::isFinal)
				&& ASTNodeUtil.hasModifier(parent.modifiers(), Modifier::isPrivate)
				&& verifyInitializerPrecondition(initializer)) {

			ITypeBinding initializerTypeBinding = initializer.resolveTypeBinding();
			List<String> parentTypeList = Collections.singletonList(parentTypeBinding.getErasure()
				.getQualifiedName());

			if (ClassRelationUtil.isContentOfTypes(initializerTypeBinding, parentTypeList)
					|| ClassRelationUtil.isInheritingContentOfTypes(initializerTypeBinding, parentTypeList)) {
				String methodNameString = getSuitableMethodNameForType(parentTypeBinding);

				if (methodNameString != null) {
					String fieldName = fragmentNode.getName()
						.getIdentifier();
					initializersToReplace.put(fieldName, initializer);
					methodNames.put(fieldName, methodNameString);
					addMarkerEvent(fragmentNode);
					return false;
				}
			}
		}
		excludeInitializer(initializer);
		return false;
	}

	private void excludeInitializer(Expression initializer) {
		if (initializer != null && initializer.getNodeType() == ASTNode.SIMPLE_NAME) {
			String initializerName = ((SimpleName) initializer).getIdentifier();
			excludedNames.add(initializerName);
		}
	}

	private boolean verifyInitializerPrecondition(Expression initializer) {
		if (initializer == null) {
			return false;
		}

		if (ASTNode.CLASS_INSTANCE_CREATION != initializer.getNodeType()) {
			return false;
		}

		if (JavaCore.compareJavaVersions(javaVersion, JavaCore.VERSION_1_8) >= 0) {
			return true;
		}

		return !ASTNodeUtil.containsDiamondOperator((ClassInstanceCreation) initializer);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocationNode) {
		Expression expression = methodInvocationNode.getExpression();
		if (expression != null && ASTNode.SIMPLE_NAME == expression.getNodeType()) {
			ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
			if (ClassRelationUtil.isContentOfTypes(expressionTypeBinding, COLLECTION_TYPE_LIST)
					|| ClassRelationUtil.isInheritingContentOfTypes(expressionTypeBinding, COLLECTION_TYPE_LIST)
					|| ClassRelationUtil.isContentOfTypes(expressionTypeBinding, MAP_TYPE_LIST)
					|| ClassRelationUtil.isInheritingContentOfTypes(expressionTypeBinding, MAP_TYPE_LIST)) {

				String expressionName = ((SimpleName) expression).getIdentifier();
				String methodName = methodInvocationNode.getName()
					.getIdentifier();

				if (!collectionNonModifyingMethods.contains(methodName)) {
					excludedNames.add(expressionName);
				}
			} else if (methodInvocationNode.arguments() != null && !methodInvocationNode.arguments()
				.isEmpty()) {
				List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocationNode.arguments(),
						Expression.class);
				arguments.forEach(argument -> {
					ITypeBinding argumentTypeBinding = argument.resolveTypeBinding();
					if ((ClassRelationUtil.isContentOfTypes(argumentTypeBinding, COLLECTION_TYPE_LIST)
							|| ClassRelationUtil.isInheritingContentOfTypes(argumentTypeBinding, COLLECTION_TYPE_LIST)
							|| ClassRelationUtil.isContentOfTypes(argumentTypeBinding, MAP_TYPE_LIST)
							|| ClassRelationUtil.isInheritingContentOfTypes(argumentTypeBinding, MAP_TYPE_LIST))
							&& ASTNode.SIMPLE_NAME == argument.getNodeType()) {
						excludedNames.add(((SimpleName) argument).getIdentifier());
					}
				});
			}
		}

		return true;
	}

	@Override
	public boolean visit(Initializer initializerNode) {
		if (ASTNodeUtil.hasModifier(initializerNode.modifiers(), Modifier::isStatic)) {
			Block block = initializerNode.getBody();
			if (block != null && block.statements() != null && !block.statements()
				.isEmpty()) {
				List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
				excludedNames.addAll(statements.stream()
					.filter(ExpressionStatement.class::isInstance)
					.map(ExpressionStatement.class::cast)
					.map(ExpressionStatement::getExpression)
					.filter(MethodInvocation.class::isInstance)
					.map(MethodInvocation.class::cast)
					.map(ASTNodeUtil::getLeftMostExpressionOfMethodInvocation)
					.filter(SimpleName.class::isInstance)
					.map(SimpleName.class::cast)
					.map(SimpleName::getIdentifier)
					.collect(Collectors.toSet()));
			}
		}

		return true;
	}

	@Override
	public boolean visit(ReturnStatement returnStatement) {
		Expression expression = returnStatement.getExpression();
		if (expression != null) {
			excludeFields(expression);
		}
		return true;
	}

	@Override
	public boolean visit(Assignment assignment) {
		Expression rightHandSide = assignment.getRightHandSide();
		excludeFields(rightHandSide);
		return true;
	}

	private void excludeFields(Expression expression) {
		if (expression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return;
		}

		SimpleName simpleName = (SimpleName) expression;
		IBinding typeBinding = simpleName.resolveBinding();
		if (IBinding.VARIABLE != typeBinding.getKind()) {
			return;
		}
		IVariableBinding variableBinding = (IVariableBinding) typeBinding;
		if (variableBinding.isField()) {
			this.excludedNames.add(variableBinding.getName());
		}
	}

	@Override
	public void endVisit(CompilationUnit compilationUnitNode) {
		methodNames.keySet()
			.stream()
			.filter(key -> initializersToReplace.keySet()
				.contains(key) && !excludedNames.contains(key))
			.forEach(key -> {
				MethodInvocation newMI = createNewMethodInvocation(initializersToReplace.get(key),
						methodNames.get(key));
				astRewrite.replace(initializersToReplace.get(key), newMI, null);
				onRewrite();
			});

		super.endVisit(compilationUnitNode);
	}

	/**
	 * creates the new {@link MethodInvocation} with the given name and the
	 * given initializer as an argument
	 * 
	 * @param initializer
	 * @param methodNameString
	 * @return new {@link MethodInvocation}
	 */
	private MethodInvocation createNewMethodInvocation(Expression initializer, String methodNameString) {
		Name collectionsClassName = addImport(JAVA_UTIL_COLLECTIONS, initializer);
		SimpleName methodName = astRewrite.getAST()
			.newSimpleName(methodNameString);

		MethodInvocation newMI = astRewrite.getAST()
			.newMethodInvocation();
		newMI.setExpression(collectionsClassName);
		newMI.setName(methodName);

		ClassInstanceCreation cicCopy = (ClassInstanceCreation) astRewrite.createCopyTarget(initializer);

		ListRewrite newMIArgs = astRewrite.getListRewrite(newMI, MethodInvocation.ARGUMENTS_PROPERTY);
		newMIArgs.insertFirst(cicCopy, null);

		return newMI;
	}

	/**
	 * checks the type of the {@link VariableDeclarationFragment} and selects
	 * the suitable method name for the unmodifiable {@link Collection} or
	 * {@link Map}
	 * 
	 * @param typeBinding
	 *            of the {@link VariableDeclarationFragment}
	 * @return the suitable method name for the given type or null, if there
	 *         isn't one
	 */
	private String getSuitableMethodNameForType(ITypeBinding typeBinding) {
		String methodName = null;

		if (ClassRelationUtil.isContentOfTypes(typeBinding, NAVIGABLE_SET_TYPE_LIST)) {
			methodName = UNMODIFIABLE_NAVIGABLE_SET;
		} else if (ClassRelationUtil.isContentOfTypes(typeBinding, SORTED_SET_TYPE_LIST)) {
			methodName = UNMODIFIABLE_SORTED_SET;
		} else if (ClassRelationUtil.isContentOfTypes(typeBinding, SET_TYPE_LIST)) {
			methodName = UNMODIFIABLE_SET;

		} else if (ClassRelationUtil.isContentOfTypes(typeBinding, NAVIGABLE_MAP_TYPE_LIST)) {
			methodName = UNMODIFIABLE_NAVIGABLE_MAP;
		} else if (ClassRelationUtil.isContentOfTypes(typeBinding, SORTED_MAP_TYPE_LIST)) {
			methodName = UNMODIFIABLE_SORTED_MAP;
		} else if (ClassRelationUtil.isContentOfTypes(typeBinding, MAP_TYPE_LIST)) {
			methodName = UNMODIFIABLE_MAP;

		} else if (ClassRelationUtil.isContentOfTypes(typeBinding, LIST_TYPE_LIST)) {
			methodName = UNMODIFIABLE_LIST;

		} else if (ClassRelationUtil.isContentOfTypes(typeBinding, COLLECTION_TYPE_LIST)) {
			methodName = UNMODIFIABLE_COLLECTION;
		}

		return methodName;
	}

	/*** PRIVATE HELPER METHODS ***/
}

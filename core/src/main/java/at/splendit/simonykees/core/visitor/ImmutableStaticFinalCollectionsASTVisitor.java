package at.splendit.simonykees.core.visitor;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;

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
public class ImmutableStaticFinalCollectionsASTVisitor extends AbstractAddImportASTVisitor {

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

	private static final String JAVA_UTIL_COLLECTIONS_SIMPLENAME = java.util.Collections.class.getSimpleName();
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

	@Override
	public boolean visit(VariableDeclarationFragment fragmentNode) {

		if (fragmentNode.getParent() != null && ASTNode.FIELD_DECLARATION == fragmentNode.getParent().getNodeType()) {
			FieldDeclaration parent = (FieldDeclaration) fragmentNode.getParent();
			ITypeBinding parentTypeBinding = parent.getType().resolveBinding();
			if (parentTypeBinding != null) {

				if (ASTNodeUtil.hasModifier(parent.modifiers(), Modifier::isStatic)
						&& ASTNodeUtil.hasModifier(parent.modifiers(), Modifier::isFinal)) {

					Expression initializer = fragmentNode.getInitializer();
					if (initializer != null && ASTNode.CLASS_INSTANCE_CREATION == initializer.getNodeType()) {

						ITypeBinding initializerTypeBinding = initializer.resolveTypeBinding();
						List<String> parentTypeList = Collections
								.singletonList(parentTypeBinding.getErasure().getQualifiedName());

						if (ClassRelationUtil.isContentOfTypes(initializerTypeBinding, parentTypeList)
								|| ClassRelationUtil.isInheritingContentOfTypes(initializerTypeBinding,
										parentTypeList)) {
							String methodNameString = getSuitableMethodNameForType(parentTypeBinding);

							if (methodNameString != null) {
								this.addImports.add(JAVA_UTIL_COLLECTIONS);

								SimpleName collectionsClassName = astRewrite.getAST()
										.newSimpleName(JAVA_UTIL_COLLECTIONS_SIMPLENAME);
								SimpleName methodName = astRewrite.getAST().newSimpleName(methodNameString);

								MethodInvocation newMI = astRewrite.getAST().newMethodInvocation();
								newMI.setExpression(collectionsClassName);
								newMI.setName(methodName);

								ClassInstanceCreation cicCopy = (ClassInstanceCreation) astRewrite
										.createCopyTarget(initializer);

								ListRewrite newMIArgs = astRewrite.getListRewrite(newMI,
										MethodInvocation.ARGUMENTS_PROPERTY);
								newMIArgs.insertFirst(cicCopy, null);

								astRewrite.replace(initializer, newMI, null);
							}
						}
					}
				}
			}

		}

		return false;
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
}

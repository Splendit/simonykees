package eu.jsparrow.rules.common.util;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for computing information related with the ancestors of a
 * type. Makes use of {@link ITypeBinding} for finding finding super types and
 * their properties. Furthermore, finds the corresponding boxed types for each
 * java primitive type.
 * 
 * @author Martin Huter, Ardit Ymeri
 * @since 0.9.2
 */
public class ClassRelationUtil {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String INT = "int"; //$NON-NLS-1$
	private static final String DOUBLE = "double"; //$NON-NLS-1$
	private static final String FLOAT = "float"; //$NON-NLS-1$
	private static final String LONG = "long"; //$NON-NLS-1$
	private static final String SHORT = "short"; //$NON-NLS-1$
	private static final String BOOLEAN = "boolean"; //$NON-NLS-1$
	private static final String BYTE = "byte"; //$NON-NLS-1$
	private static final String CHAR = "char"; //$NON-NLS-1$

	private ClassRelationUtil() {
	}

	/**
	 * Checks whether the given type binding is a subtype of any of the types
	 * having the qualified name in the given list.
	 * 
	 * @param iTypeBinding
	 *            Is an {@link ITypeBinding} that is compared to the list of
	 *            injected java-classes if it is related to it by polymorphism
	 * @param fullyQuallifiedTargetnames
	 *            list of qualified names of the super types to look for.
	 * @return if the {@link ITypeBinding} is part of the registered types the
	 *         return value is true
	 */
	public static boolean isInheritingContentOfTypes(ITypeBinding iTypeBinding,
			List<String> fullyQuallifiedTargetnames) {

		if (iTypeBinding == null) {
			return false;
		}

		return findAncestors(iTypeBinding).stream()
			.map(ITypeBinding::getErasure)
			.map(ITypeBinding::getQualifiedName)
			.anyMatch(fullyQuallifiedTargetnames::contains);

	}

	/**
	 * Checks whether the erasure of the given type binding coincides with any
	 * of the given registered type.
	 * 
	 * @param iTypeBinding
	 *            type binding to be checked
	 * @param registeredIType
	 *            list of qualified names of target types
	 * 
	 * @return if the erasure of the type binding equals the registeredType
	 */
	public static boolean isContentOfType(ITypeBinding iTypeBinding, String registeredIType) {

		if (iTypeBinding == null) {
			return false;
		}

		return registeredIType.equals(iTypeBinding.getErasure()
			.getQualifiedName());
	}

	/**
	 * Checks whether the erasure of the given type binding coincides with any
	 * of the given list of registered types.
	 * 
	 * @param iTypeBinding
	 *            type binding to be checked
	 * @param registeredITypes
	 *            list of qualified names of target types
	 * 
	 * @return if the erasure the type binding occurs in the list of types
	 */
	public static boolean isContentOfTypes(ITypeBinding iTypeBinding, List<String> registeredITypes) {

		if (iTypeBinding == null) {
			return false;
		}

		return registeredITypes.contains(iTypeBinding.getErasure()
			.getQualifiedName());
	}

	/**
	 * Compares the given lists by getting the qualified name of corresponding
	 * elements on same positions.
	 * 
	 * @param firstTypeBindings
	 *            the first {@link ITypeBinding} array to be compared
	 * @param secondTypeBindings
	 *            the second {@link ITypeBinding} array to be compared
	 * @return if both lists have the same size and all corresponding elements
	 *         have the same qualified name.
	 */
	public static boolean compareITypeBinding(ITypeBinding[] firstTypeBindings, ITypeBinding[] secondTypeBindings) {
		if (!compareSizes(firstTypeBindings, secondTypeBindings)) {
			return false;
		}

		for (int i = 0; i < firstTypeBindings.length; i++) {
			if (!compareITypeBinding(firstTypeBindings[i], secondTypeBindings[i])) {
				return false;
			}
		}

		return true;
	}

	private static boolean compareSizes(ITypeBinding[] firstTypeBindings, ITypeBinding[] secondTypeBindings) {
		if (firstTypeBindings == null || secondTypeBindings == null) {
			return false;
		}

		int lhsSize = firstTypeBindings.length;
		int rhsSize = secondTypeBindings.length;

		return lhsSize == rhsSize;
	}

	public static boolean compareBoxedITypeBinding(ITypeBinding[] firstTypeBindings,
			ITypeBinding[] secondTypeBindings) {
		if (!compareSizes(firstTypeBindings, secondTypeBindings)) {
			return false;
		}

		for (int i = 0; i < firstTypeBindings.length; i++) {
			ITypeBinding firstType = firstTypeBindings[i];
			ITypeBinding secondType = secondTypeBindings[i];
			if (firstType.isPrimitive() || secondType.isPrimitive()) {
				String firstTypeName = findBoxedTypeOfPrimitive(firstType);
				String secondTypeName = findBoxedTypeOfPrimitive(secondType);
				if (!firstTypeName.equals(secondTypeName)) {
					return false;
				}
			} else if (!compareITypeBinding(firstType, secondType)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @see ClassRelationUtil#compareITypeBinding(ITypeBinding[],
	 *      ITypeBinding[])
	 * 
	 * @param firstTypeBinding
	 *            the first {@link ITypeBinding} to be compared
	 * @param secondTypeBinging
	 *            the second {@link ITypeBinding} to be compared
	 * @return whether or not the {@link ITypeBinding}s have the same qualified
	 *         name
	 */
	public static boolean compareITypeBinding(ITypeBinding firstTypeBinding, ITypeBinding secondTypeBinging) {
		if (null == firstTypeBinding || null == secondTypeBinging) {
			return false;
		}

		String lhsTypeName = firstTypeBinding.getQualifiedName();
		String rhsTypeName = secondTypeBinging.getQualifiedName();
		return lhsTypeName.equals(rhsTypeName);
	}

	/**
	 * Finds the list of type bindings of the supper classes and interfaces
	 * inherited by the given type binding.
	 * 
	 * @param typeBinding
	 *            the resolved type binding of the class to find the ancestors
	 *            for.
	 * @return the list of type bindings of all ancestors
	 */
	public static List<ITypeBinding> findAncestors(ITypeBinding typeBinding) {
		List<ITypeBinding> ancesotrs = new ArrayList<>();

		if (typeBinding != null) {
			// get the type binding of super class
			ITypeBinding parentClass = typeBinding.getSuperclass();
			if (parentClass != null) {
				ancesotrs.add(parentClass);
				ancesotrs.addAll(findAncestors(parentClass));
			}

			// get type bindings of the implemented interfaces
			for (ITypeBinding iTypeBinding : typeBinding.getInterfaces()) {
				if (iTypeBinding != null) {
					ancesotrs.add(iTypeBinding);
					ancesotrs.addAll(findAncestors(iTypeBinding));
				}
			}
		}

		return ancesotrs;
	}

	/**
	 * Finds the names of the non-private fields that are inherited from the
	 * given type binding.
	 * 
	 * @param typeBinding
	 *            a type binding
	 * @return list of inherited field names
	 */
	public static List<String> findInheretedFields(ITypeBinding typeBinding) {
		List<ITypeBinding> ancestors = findAncestors(typeBinding);

		return ancestors.stream()
			.flatMap(ancestor -> Arrays.asList(ancestor.getDeclaredFields())
				.stream()
				.filter(field -> !Modifier.isPrivate(field.getModifiers())))
			.map(IVariableBinding::getName)
			.collect(Collectors.toList());
	}

	public static List<IMethodBinding> findInheretedMethods(ITypeBinding typeBinding) {
		List<ITypeBinding> ancestors = findAncestors(typeBinding);

		return ancestors.stream()
			.flatMap(ancestor -> Arrays.asList(ancestor.getDeclaredMethods())
				.stream()
				.filter(method -> !Modifier.isPrivate(method.getModifiers())))
			.collect(Collectors.toList());
	}

	/**
	 * Returns the name of the corresponding boxed type for the given primitive
	 * type.
	 * 
	 * @param primitiveType
	 *            the binding of a primitive type the name of a primitive
	 * 
	 * @return
	 *         <ul>
	 *         <li>the name of the corresponding boxed type if the given type is
	 *         primitive</li>
	 *         <li>the type name it is not a primitive</li>
	 *         <li>or an empty string if the given type is {@code null}</li>
	 *         </ul>
	 */
	public static String findBoxedTypeOfPrimitive(ITypeBinding primitiveType) {

		if (primitiveType == null) {
			return ""; //$NON-NLS-1$
		}

		String primitiveTypeName = primitiveType.getName();
		if (!primitiveType.isPrimitive()) {
			return primitiveTypeName;
		}

		String expressionName;
		switch (primitiveTypeName) {
		case INT:
			expressionName = Integer.class.getSimpleName();
			break;
		case DOUBLE:
			expressionName = Double.class.getSimpleName();
			break;
		case FLOAT:
			expressionName = Float.class.getSimpleName();
			break;
		case LONG:
			expressionName = Long.class.getSimpleName();
			break;
		case SHORT:
			expressionName = Short.class.getSimpleName();
			break;
		case BOOLEAN:
			expressionName = Boolean.class.getSimpleName();
			break;
		case BYTE:
			expressionName = Byte.class.getSimpleName();
			break;
		case CHAR:
			expressionName = Character.class.getSimpleName();
			break;
		default:
			expressionName = primitiveTypeName;
			break;
		}

		return expressionName;
	}

	/**
	 * Checks if a method is overloaded on the i-th parameter (expected to be a
	 * parameterized type). The search is based on type of the expression of the
	 * given method invocation, or in the type where the method is declared if
	 * the expression of the method invocation is {@code null}.
	 * 
	 * @param methodInvocation
	 *            a node representing the occurrence of the method invocation
	 * @param methodBinding
	 *            a method to be checked for overloading
	 * @param i
	 *            the position of the parameterized type parameter
	 * @return {@code true} if the method is overloaded based only on the i-th
	 *         parameter and {@code false} otherwise.
	 */
	public static boolean isOverloadedWithParameterizedTypes(MethodInvocation methodInvocation,
			IMethodBinding methodBinding, int i) {
		Expression expression = methodInvocation.getExpression();
		ITypeBinding expressionBinding;
		if (expression != null && (expressionBinding = expression.resolveTypeBinding()) != null) {
			return isOverloadedWithParameterizedTypes(expressionBinding, methodBinding, i);
		} else {
			ITypeBinding declaringClass = methodBinding.getDeclaringClass();
			return isOverloadedWithParameterizedTypes(declaringClass, methodBinding, i);
		}
	}

	/**
	 * Checks if a method is overloaded on the i-th parameter (expected to be a
	 * parameterized type). The search is based on the methods declared on the
	 * given {@link ITypeBinding} and its parents.
	 * 
	 * @param declaringClass
	 *            a type to get the overloading candidates from.
	 * @param methodBinding
	 *            a method to be checked for overloading
	 * @param i
	 *            the position of the parameterized type parameter
	 * @return {@code true} if the method is overloaded based only on the i-th
	 *         parameter and {@code false} otherwise.
	 */
	public static boolean isOverloadedWithParameterizedTypes(ITypeBinding declaringClass, IMethodBinding methodBinding,
			int i) {

		List<IMethodBinding> methods = new ArrayList<>();
		methods.addAll(Arrays.asList(declaringClass.getDeclaredMethods()));
		List<ITypeBinding> ancestors = findAncestors(declaringClass);
		methods.addAll(ancestors.stream()
			.flatMap(ancestor -> Arrays.stream(ancestor.getDeclaredMethods())
				.filter(method -> !Modifier.isPrivate(method.getModifiers())))
			.collect(Collectors.toList()));
		return methods.stream()
			.filter(method -> method.getName()
				.equals(methodBinding.getName()) && (method.getParameterTypes().length > i)
					&& method.getParameterTypes()[i].isParameterizedType()
					&& matchingNonParameterizedTypes(methodBinding.getParameterTypes(), method.getParameterTypes()))
			.count() > 1;
	}

	/**
	 * Checks if the given arrays of types are matching with each-other. The
	 * parameterized types are excluded.
	 * 
	 * @param parameterTypes
	 *            array of types
	 * @param parameterTypes2
	 *            array of types.
	 * 
	 * @return {@code true} if the length of the arrays are equal and all
	 *         corresponding types except the parameterized ones are matching
	 *         and {@code false} otherwise.
	 */
	private static boolean matchingNonParameterizedTypes(ITypeBinding[] parameterTypes,
			ITypeBinding[] parameterTypes2) {
		if (parameterTypes.length != parameterTypes2.length) {
			return false;
		}
		boolean allButParamMatching = true;
		for (int j = 0; j < parameterTypes.length; j++) {
			ITypeBinding type1 = parameterTypes[j];
			ITypeBinding type2 = parameterTypes2[j];
			if (!type1.isParameterizedType() && !compareITypeBinding(type1, type2)) {
				allButParamMatching = false;
				break;
			}
		}
		return allButParamMatching;
	}

	/**
	 * Finds the {@link IMethodBinding}s of all methods overloading the given
	 * one, i.e. having the same method name, having different parameters and
	 * declared on the same or on a parent type.
	 * 
	 * @param methodInvocation
	 *            the method to find the overloads for.
	 * @return {@link List} of overloaded methods, or an empty list if the type
	 *         bindings cannot be resolved.
	 */
	public static List<IMethodBinding> findOverloadedMethods(MethodInvocation methodInvocation) {

		Expression expression = methodInvocation.getExpression();
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return Collections.emptyList();
		}
		ITypeBinding type;
		if (expression != null) {
			type = expression.resolveTypeBinding();
		} else {
			type = methodBinding.getDeclaringClass();
		}

		if (type == null) {
			return Collections.emptyList();
		}

		return findOverloadedMethods(methodBinding, type);
	}

	/**
	 * Finds the {@link IMethodBinding}s of all methods overloading the given
	 * one, i.e. having the same method name, having different parameters and
	 * declared on the same or on a parent type.
	 * 
	 * @param methodBinding
	 *            the {@link IMethodBinding} of the method to find the overloads
	 *            for.
	 * @param type
	 *            the type where the search for overloaded methods will start
	 *            from.
	 * @return {@link List} of overloaded methods, or an empty list if the type
	 *         bindings cannot be resolved.
	 */
	public static List<IMethodBinding> findOverloadedMethods(IMethodBinding methodBinding, ITypeBinding type) {
		List<IMethodBinding> methods = new ArrayList<>();
		methods.addAll(Arrays.asList(type.getDeclaredMethods()));
		methods.addAll(findInheretedMethods(type));
		String methodIdentifier = methodBinding.getName();

		return methods.stream()
			.filter(method -> methodIdentifier.equals(method.getName()))
			// exclude overridden methods
			.filter(method -> !methodBinding.overrides(method))
			// exclude the binding of the method itself
			.filter(method -> method.getMethodDeclaration() != null && !method.getMethodDeclaration()
				.isEqualTo(methodBinding.getMethodDeclaration()))
			.collect(Collectors.toList());
	}

	/**
	 * Verifies if the given {@link IMethodBinding}s have the same signature
	 * except for the parameter on the given position.
	 * 
	 * @param methodBinding
	 *            the type binding of the original method
	 * @param overloadedMethod
	 *            the type binding of a method overloading the original method
	 * @param paramterIndex
	 *            the position of the parameter expected to have a different
	 *            type on each of the given methods.
	 * @return {@code true} the condition is satisfied and {@code false}
	 *         otherwise.
	 */
	public static boolean isOverloadedOnParameter(IMethodBinding methodBinding, IMethodBinding overloadedMethod,
			int paramterIndex) {
		ITypeBinding[] methodParameterTypes = methodBinding.getParameterTypes();
		ITypeBinding[] overloadedMethodParameterTypes = overloadedMethod.getParameterTypes();

		if (methodParameterTypes.length != overloadedMethodParameterTypes.length) {
			return false;
		}

		for (int i = 0; i < methodParameterTypes.length; i++) {
			ITypeBinding methodParamter = methodParameterTypes[i];
			ITypeBinding overloadedParameter = overloadedMethodParameterTypes[i];

			if (i != paramterIndex
					&& !isContentOfType(methodParamter.getErasure(), overloadedParameter.getQualifiedName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the modifiers of the given {@link IMethodBinding} allow for
	 * accessing it in the arguments section of the given method invocation.
	 * 
	 * @param method
	 *            the type binding of the method to be checked
	 * @param wrapperMethod
	 *            normal method invocation
	 * @return {@code true} if the method can be invoked in the arguments of the
	 *         given wrapper method, or {@code false} otherwise.
	 */
	public static boolean isVisibleIn(IMethodBinding method, MethodInvocation wrapperMethod) {
		if (Modifier.isPublic(method.getModifiers())) {
			return true;
		}

		ITypeBinding methodDeclaringClass = method.getDeclaringClass();
		ITypeBinding erasure = methodDeclaringClass.getErasure();
		String qualifiedName = erasure == null ? methodDeclaringClass.getQualifiedName() : erasure.getQualifiedName();
		AbstractTypeDeclaration type = ASTNodeUtil.getSpecificAncestor(wrapperMethod, AbstractTypeDeclaration.class);

		ITypeBinding typeBinding = type.resolveBinding();
		if (typeBinding == null) {
			return false;
		}

		if (Modifier.isPrivate(method.getModifiers())) {
			return ClassRelationUtil.isContentOfType(typeBinding, qualifiedName);
		}

		if (!Modifier.isProtected(method.getModifiers())) {
			return belongToSamePackage(methodDeclaringClass, typeBinding);
		}

		if (!Modifier.isProtected(method.getModifiers())) {
			return belongToSamePackage(methodDeclaringClass, typeBinding) || ClassRelationUtil
				.isInheritingContentOfTypes(typeBinding, Collections.singletonList(qualifiedName));
		}

		return true;
	}

	/**
	 * Checks if the given type bindings belong to the same package.
	 * 
	 * @param originTypeBinding
	 *            origin type
	 * @param typeBinding
	 *            target type
	 * @return {@code true} if both types belong to the same package or
	 *         {@code false} otherwise.
	 */
	public static boolean belongToSamePackage(ITypeBinding originTypeBinding, ITypeBinding typeBinding) {
		IPackageBinding typePackage = typeBinding.getPackage();
		IPackageBinding originPackage = originTypeBinding.getPackage();

		if (typePackage == originPackage) {
			// either both are null, or typePackage and originPackage are
			// references to the same object
			return true;
		}
		return typePackage != null && originPackage != null && typePackage.getName()
			.equals(originPackage.getName());
	}

	/**
	 * Returns the first upper type bound of this type variable, wildcard,
	 * capture, or intersectionType.
	 * 
	 * @param typeBinding
	 *            the type to be checked
	 * @return the first upper type bound or the unchanged type if no upper
	 *         bound is found or if the given type does not represent any of the
	 *         aforementioned types.
	 */
	public static ITypeBinding findFirstTypeBound(ITypeBinding typeBinding) {
		if (typeBinding.isTypeVariable() || typeBinding.isCapture() || typeBinding.isWildcardType()
				|| typeBinding.isIntersectionType()) {
			ITypeBinding[] typeBounds = typeBinding.getTypeBounds();
			if (typeBounds.length > 0) {
				return typeBounds[0];
			}
		}
		return typeBinding;
	}

	/**
	 * Checks if the given {@link ITypeBinding} represents the boxing of a
	 * primitive type.
	 * 
	 * @param typeBinding
	 *            the {@link ITypeBinding} to be checked.
	 * @return {@code true} if the binding represents a boxing or {@code false}
	 *         otherwise.
	 */
	public static boolean isBoxedType(ITypeBinding typeBinding) {
		return isContentOfTypes(typeBinding,
				Arrays.asList(java.lang.Integer.class.getName(), java.lang.Double.class.getName(),
						java.lang.Float.class.getName(), java.lang.Long.class.getName(),
						java.lang.Short.class.getName(), java.lang.Boolean.class.getName(),
						java.lang.Byte.class.getName(), java.lang.Character.class.getName()));
	}

	/**
	 * Checks if the given {@link ImportDeclaration} is
	 * {@link ImportDeclaration#isOnDemand} which implicitly imports the given
	 * type.
	 * 
	 * @param importDeclaration
	 *            import declaration to be checked.
	 * @param javaFileName
	 *            expected is a file name which is combined from the simple name
	 *            of the type to be checked and a ".java" - suffix.
	 * @return {@code true} if a type with the given name exists in the package
	 *         imported with the on-demand {@link ImportDeclaration} or
	 *         {@code false} otherwise.
	 */
	public static boolean importsTypeOnDemand(ImportDeclaration importDeclaration, String expectedTypeName) {
		if (!importDeclaration.isOnDemand()) {
			return false;
		}

		IBinding iBinding = importDeclaration.resolveBinding();
		if (iBinding.getKind() != IBinding.PACKAGE) {
			return false;
		}

		IPackageBinding iPackageBinding = (IPackageBinding) iBinding;
		IJavaElement packageJavaElement = iPackageBinding.getJavaElement();
		if (packageJavaElement.getElementType() != IJavaElement.PACKAGE_FRAGMENT) {
			return false;
		}

		IPackageFragment iPackageFragment = (IPackageFragment) packageJavaElement;
		try {
			IJavaElement[] children = iPackageFragment.getChildren();
			for (IJavaElement child : children) {
				String elementName = child.getElementName();
				String typeName = elementName.replaceAll("\\.(class|java)$", ""); //$NON-NLS-1$//$NON-NLS-2$
				if (expectedTypeName.equals(typeName)) {
					return true;
				}
			}

		} catch (JavaModelException e) {
			logger.debug(e.getMessage(), e);
		}

		return false;
	}

	/**
	 * Checks if the given {@link ImportDeclaration} is
	 * {@link ImportDeclaration#isOnDemand} and implicitly imports the given
	 * inner type.
	 * 
	 * @param importDeclaration
	 *            import declaration to be checked.
	 * @param simpleTypeName
	 *            expected is a simple type name.
	 * @return {@code true} if a type with the given name exists in the type
	 *         imported with the on-demand {@link ImportDeclaration} or
	 *         {@code false} otherwise.
	 */
	public static boolean importsInnerTypeOnDemand(ImportDeclaration importDeclaration, String simpleTypeName) {
		if (!importDeclaration.isOnDemand()) {
			return false;
		}

		IBinding iBinding = importDeclaration.resolveBinding();
		if (iBinding.getKind() != IBinding.TYPE) {
			return false;
		}

		ITypeBinding iTypeBinding = (ITypeBinding) iBinding;
		return Arrays.stream(iTypeBinding.getDeclaredTypes())
			.map(ITypeBinding::getName)
			.anyMatch(simpleTypeName::equals);
	}

	/**
	 * 
	 * Checks if the given {@link ImportDeclaration} is a
	 * Static-Import-on-Demand which implicitly imports the given static method.
	 * 
	 * @param importDeclaration
	 *            import declaration to be checked.
	 * @param methodName
	 *            name of the static method
	 * @return {@code true} if a static method with the given name exists in the
	 *         type imported with the on-demand {@link ImportDeclaration} or
	 *         {@code false} otherwise.
	 */
	public static boolean importsStaticMethodOnDemand(ImportDeclaration importDeclaration, String methodName) {
		if (!importDeclaration.isOnDemand()) {
			return false;
		}

		if (!importDeclaration.isStatic()) {
			return false;
		}

		IBinding iBinding = importDeclaration.resolveBinding();
		if (iBinding.getKind() != IBinding.TYPE) {
			return false;
		}

		ITypeBinding typeBinding = (ITypeBinding) iBinding;

		IMethodBinding[] declaredMethods = typeBinding.getDeclaredMethods();
		return Arrays.stream(declaredMethods)
			.filter(m -> Modifier.isStatic(m.getModifiers()))
			.map(IMethodBinding::getName)
			.anyMatch(methodName::equals);
	}

	/**
	 * 
	 * @param methodInvocation
	 *            method to be checked.
	 * @return if the class where the given method invocation is declared
	 *         belongs to {@code java.util} package.
	 */
	public static boolean isJavaUtilMethod(MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		IPackageBinding declaringClassPackage = declaringClass.getPackage();
		if (declaringClassPackage == null) {
			return false;
		}
		String packageName = declaringClassPackage.getName();
		return packageName.startsWith("java.util"); //$NON-NLS-1$
	}

	/**
	 * Checks if the given expression represents a new instance creation of the
	 * given qualified type name.
	 * 
	 * 
	 * @param expression
	 *            expression to be checked
	 * @param fullyQualifiedTypeName
	 *            expected fully qualified type name.
	 * @return if the condition is met and the {@link ClassInstanceCreation} has
	 *         no {@link AnonymousClassDeclaration}.
	 */
	public static boolean isNewInstanceCreationOf(Expression expression, String fullyQualifiedTypeName) {
		if (expression == null || expression.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return false;
		}
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		if (classInstanceCreation.getAnonymousClassDeclaration() != null) {
			return false;
		}
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		return isContentOfType(typeBinding, fullyQualifiedTypeName);
	}

}

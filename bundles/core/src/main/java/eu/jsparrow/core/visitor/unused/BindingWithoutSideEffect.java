package eu.jsparrow.core.visitor.unused;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

@SuppressWarnings("nls")
public class BindingWithoutSideEffect {

	private static final String EQUALS = "equals";
	private static final String HASH_CODE = "hashCode";
	private static final String GET_CLASS = "getClass";
	private static final String JAVA_LANG = "java.lang";
	private static final String JAVA_TIME = "java.time";
	private static final String JAVA_UTIL = "java.util";
	private static final String JAVA_UTIL_DOT = "java.util.";
	
	private static final List<String> JAVA_UTIL_COLLECTION = Collections
		.singletonList(java.util.Collection.class.getName());
	private static final List<String> JAVA_UTIL_MAP = Collections
		.singletonList(java.util.Map.class.getName());

	private static final List<String> IMMUTABLE_CLASSES;
	private static final List<String> TYPES_SUPPORTING_ALL_STATIC_METHODS;
	private static final List<String> OTHER_TYPES_SUPPORTING_CONSTRUCTORS;

	private static final List<String> SUPPORTED_COLLECTION_METHODS = Collections.unmodifiableList(Arrays.asList(
			"contains", "containsAll", "get", "indexOf", "isEmpty", "lastIndexOf", "size",
			"toArray", "toString",
			"element", "getFirst", "getLast"
	// , "stream" ??
	// , "spliterator" ??

	));
	private static final List<String> SUPPORTED_MAP_METHODS = Collections.unmodifiableList(Arrays.asList(
			"containsKey", "containsValue", "get", "getOrDefault", "isEmpty", "size"));

	private static final List<String> SUPPORTED_INSTANCE_METHODE_PREFIXES = Collections.unmodifiableList(Arrays.asList(
			"can", "contains", "get", "has", "is"));

	static {
		IMMUTABLE_CLASSES = Stream.of(
				String.class,
				Boolean.class,
				Character.class,
				Byte.class,
				Short.class,
				Integer.class,
				Long.class,
				Float.class,
				Double.class,
				java.util.Optional.class,
				java.util.OptionalInt.class,
				java.util.OptionalLong.class,
				java.util.OptionalDouble.class,
				java.math.BigInteger.class,
				java.math.BigDecimal.class)
			.map(Class::getName)
			.collect(Collectors.toList());

		TYPES_SUPPORTING_ALL_STATIC_METHODS = Stream.of(
				java.util.Objects.class,
				java.util.Arrays.class,
				java.util.Collections.class,
				java.util.List.class,
				java.util.Set.class,
				java.util.EnumSet.class,
				java.util.Map.class,
				java.util.EnumSet.class,
				java.util.Comparator.class,
				java.lang.Math.class,
				java.lang.StrictMath.class,
				java.nio.file.Path.class,
				java.nio.file.Paths.class)
			.map(Class::getName)
			.collect(Collectors.toList());

		OTHER_TYPES_SUPPORTING_CONSTRUCTORS = Stream.of(
				java.util.GregorianCalendar.class,
				java.util.Date.class,
				java.util.Random.class,
				java.lang.Object.class,
				java.lang.StringBuilder.class,
				java.lang.StringBuffer.class)
			.map(Class::getName)
			.collect(Collectors.toList());
	}

	private BindingWithoutSideEffect() {
		/*
		 * Private default constructor hiding implicit public one.
		 */
	}

	private static boolean isSupportedCollection(ITypeBinding typeBinding) {
		if (ClassRelationUtil.isContentOfTypes(typeBinding, JAVA_UTIL_COLLECTION)) {
			return true;
		}
		if (ClassRelationUtil.isInheritingContentOfTypes(typeBinding, JAVA_UTIL_COLLECTION)) {
			IPackageBinding packageBinding = typeBinding.getPackage();
			return packageBinding.getName()
				.equals(JAVA_UTIL)
					|| packageBinding.getName()
						.startsWith(JAVA_UTIL_DOT);
		}
		return false;
	}

	private static boolean isSupportedMap(ITypeBinding typeBinding) {
		if (ClassRelationUtil.isContentOfTypes(typeBinding, JAVA_UTIL_MAP)) {
			return true;
		}
		if (ClassRelationUtil.isInheritingContentOfTypes(typeBinding, JAVA_UTIL_MAP)) {
			IPackageBinding packageBinding = typeBinding.getPackage();
			return packageBinding.getName()
				.equals(JAVA_UTIL)
					|| packageBinding.getName()
						.startsWith(JAVA_UTIL_DOT);

		}
		return false;
	}

	static boolean isSupportedConstructorType(ITypeBinding typeBinding) {
		return ClassRelationUtil.isContentOfTypes(typeBinding, IMMUTABLE_CLASSES) ||
				isSupportedCollection(typeBinding) ||
				isSupportedMap(typeBinding) ||
				ClassRelationUtil.isContentOfTypes(typeBinding, OTHER_TYPES_SUPPORTING_CONSTRUCTORS);

	}

	static boolean isSupportedMethod(IMethodBinding methodBinding) {
		if (isSupportedObjectMethod(methodBinding)) {
			return true;
		}
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (ClassRelationUtil.isContentOfTypes(declaringClass, IMMUTABLE_CLASSES)) {
			return true;
		}
		int modifiers = methodBinding.getModifiers();
		if (Modifier.isStatic(modifiers)) {
			return ClassRelationUtil.isContentOfTypes(declaringClass, TYPES_SUPPORTING_ALL_STATIC_METHODS);
		}

		String methodName = methodBinding.getName();
		if (isSupportedCollection(declaringClass) && SUPPORTED_COLLECTION_METHODS.contains(methodName) ||
				isSupportedMap(declaringClass) && SUPPORTED_MAP_METHODS.contains(methodName)) {
			return true;
		}
		String packageName = declaringClass.getPackage()
			.getName();

		return (packageName.equals(JAVA_LANG) ||
				packageName.equals(JAVA_UTIL) ||
				packageName.equals(JAVA_TIME)) &&
				SUPPORTED_INSTANCE_METHODE_PREFIXES.stream()
					.anyMatch(methodName::startsWith);

	}

	private static boolean isSupportedObjectMethod(IMethodBinding methodBinding) {
		String methodName = methodBinding.getName();
		ITypeBinding[] parameterTypes = methodBinding.getMethodDeclaration()
			.getParameterTypes();
		if (methodName.equals(EQUALS) && parameterTypes.length == 1) {
			ITypeBinding parameterType = parameterTypes[0];
			return ClassRelationUtil.isContentOfType(parameterType, java.lang.Object.class.getName());

		}
		return parameterTypes.length == 0 && (methodName.equals(GET_CLASS) || methodName.equals(HASH_CODE));
	}
}

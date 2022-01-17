package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Defines groups of types which are supported for being used as argument for
 * the overloaded static {@code assertThat} methods defined in one of the
 * classes defined in {@code org.assertj.core.api}. Each group of type is
 * represented by a {@link Predicate} on a {@link ITypeBinding}.
 * <ul>
 * <li>{@code Assertions}</li>
 * <li>{@code AssertionsForClassTypes}</li>
 * <li>{@code AssertionsForInterfaceTypes}</li>
 * </ul>
 * 
 * @since 4.7.0
 * 
 */
public class SupportedAssertJAssertThatArgumentTypes {
	private static final String JAVA_UTIL = "java.util"; //$NON-NLS-1$

	public static final Predicate<ITypeBinding> IS_SUPPORTED_ARRAY_TYPE = getSupportedArrayTypePredicate();
	public static final Predicate<ITypeBinding> IS_STRING = getTypeBindingPredicate(String.class);
	public static final Predicate<ITypeBinding> IS_SUPPORTED_ITERABLE = SupportedAssertJAssertThatArgumentTypes::isSupportedIterableType;
	public static final Predicate<ITypeBinding> IS_SUPPORTED_MAP_TYPE = SupportedAssertJAssertThatArgumentTypes::isSupportedMapType;
	public static final Predicate<ITypeBinding> IS_SUPPORTED_ITERATOR_TYPE = SupportedAssertJAssertThatArgumentTypes::isSupportedIteratorTypeForAssertion;

	public static final Predicate<ITypeBinding> IS_STREAM = getTypeBindingPredicate(
			java.util.stream.Stream.class,
			java.util.stream.IntStream.class,
			java.util.stream.LongStream.class,
			java.util.stream.DoubleStream.class);
	public static final Predicate<ITypeBinding> IS_PREDICATE = getTypeBindingPredicate(
			java.util.function.DoublePredicate.class,
			java.util.function.IntPredicate.class,
			java.util.function.LongPredicate.class,
			java.util.function.Predicate.class);
	public static final Predicate<ITypeBinding> IS_OPTIONAL = getTypeBindingPredicate(
			java.util.Optional.class,
			java.util.OptionalDouble.class,
			java.util.OptionalInt.class,
			java.util.OptionalLong.class);
	public static final Predicate<ITypeBinding> IS_FILE = getTypeBindingPredicate(java.io.File.class);
	public static final Predicate<ITypeBinding> IS_PATH = getTypeBindingPredicate(java.nio.file.Path.class);
	public static final Predicate<ITypeBinding> IS_SUPPORTED_TEMPORAL_TYPE = getTypeBindingPredicate(
			java.util.Date.class,
			java.time.Instant.class,
			java.time.LocalDate.class,
			java.time.LocalDateTime.class,
			java.time.LocalTime.class,
			java.time.OffsetDateTime.class,
			java.time.OffsetTime.class,
			java.time.ZonedDateTime.class);
	public static final Predicate<ITypeBinding> IS_OTHER_SUPPORTED_TYPE = getTypeBindingPredicate(
			java.lang.Object.class,
			java.lang.Boolean.class,
			java.lang.Character.class,
			java.lang.Byte.class,
			java.lang.Short.class,
			java.lang.Integer.class,
			java.lang.Long.class,
			java.lang.Float.class,
			java.lang.Double.class,
			//
			java.lang.StringBuffer.class,
			java.lang.StringBuilder.class,
			java.lang.CharSequence.class,
			//
			java.lang.Class.class,
			java.lang.Exception.class,
			java.lang.Throwable.class,
			//
			java.io.InputStream.class,
			//
			java.math.BigInteger.class,
			java.math.BigDecimal.class,
			//
			java.time.Period.class);

	static final List<Predicate<ITypeBinding>> ALL_SUPPORTED_REFERENZ_TYPE_PREDICATES = Collections
		.unmodifiableList(Arrays.asList(
				IS_SUPPORTED_ARRAY_TYPE,
				IS_STRING,
				IS_SUPPORTED_ITERABLE,
				IS_SUPPORTED_MAP_TYPE,
				IS_STREAM,
				IS_PREDICATE,
				IS_OPTIONAL,
				IS_FILE,
				IS_PATH,
				IS_SUPPORTED_TEMPORAL_TYPE, // getTypeBindingPredicate(
				IS_OTHER_SUPPORTED_TYPE));

	static boolean isSupportedAssertThatArgumentType(ITypeBinding typeBinding) {
		return typeBinding.isPrimitive() || ALL_SUPPORTED_REFERENZ_TYPE_PREDICATES.stream()
			.anyMatch(predicate -> predicate.test(typeBinding));
	}

	static boolean isSupportedIterableType(ITypeBinding typeBinding) {
		if (ClassRelationUtil.isContentOfType(typeBinding, java.lang.Iterable.class.getName())) {
			return true;
		}
		IPackageBinding packageBinding = typeBinding.getPackage();
		if (packageBinding == null) {
			return false;
		}
		String packageName = packageBinding
			.getName();
		return packageName.equals(JAVA_UTIL)
				&& ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
						Arrays.asList(java.lang.Iterable.class.getName()));
	}

	static boolean isSupportedMapType(ITypeBinding typeBinding) {
		if (ClassRelationUtil.isContentOfType(typeBinding, java.util.Map.class.getName())) {
			return true;
		}
		IPackageBinding packageBinding = typeBinding.getPackage();
		if (packageBinding == null) {
			return false;
		}
		String packageName = packageBinding
			.getName();
		return packageName.equals(JAVA_UTIL)
				&& ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
						Arrays.asList(java.util.Map.class.getName()));
	}

	static boolean isSupportedIteratorTypeForAssertion(ITypeBinding typeBinding) {

		if (ClassRelationUtil.isContentOfType(typeBinding, java.util.Iterator.class.getName())) {
			return true;
		}
		IPackageBinding packageBinding = typeBinding.getPackage();
		if (packageBinding == null) {
			return false;
		}
		String packageName = packageBinding
			.getName();
		return packageName.equals(JAVA_UTIL)
				&& ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
						Arrays.asList(java.util.Iterator.class.getName()));

	}

	public static Predicate<ITypeBinding> getSupportedArrayTypePredicate() {
		List<String> supportedArrayComponentTypes = Stream.of(
				byte.class,
				char.class,
				short.class,
				int.class,
				long.class,
				float.class,
				double.class,
				Short.class,
				Integer.class,
				Long.class,
				Byte.class,
				Character.class,
				Float.class,
				Double.class,
				Object.class)
			.map(Class::getName)
			.collect(Collectors.toList());

		return typeBinding -> typeBinding.getDimensions() == 1 &&
				ClassRelationUtil.isContentOfTypes(typeBinding.getComponentType(), supportedArrayComponentTypes)
				||
				typeBinding.getDimensions() == 2 &&
						ClassRelationUtil.isContentOfTypes(typeBinding.getComponentType()
							.getComponentType(), supportedArrayComponentTypes);
	}

	static Predicate<ITypeBinding> getTypeBindingPredicate(Class<?>... classes) {
		if (classes.length == 1) {
			return typeBinding -> ClassRelationUtil.isContentOfType(typeBinding, classes[0].getName());
		}
		List<String> classNamesList = Stream.of(classes)
			.map(Class::getName)
			.collect(Collectors.toList());
		return typeBinding -> ClassRelationUtil.isContentOfTypes(typeBinding, classNamesList);
	}

	private SupportedAssertJAssertThatArgumentTypes() {
		// private default constructor hiding implicit public one
	}
}

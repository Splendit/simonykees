package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * 
 * // see https://assertj.github.io/doc/#assertj-core-supported-types
 *
 */
public class SupportedTypesForAssertions {

	private static final String JAVA_UTIL = "java.util"; //$NON-NLS-1$

	private static final List<String> SUPPORTED_TYPES_FOR_ASSERTIONS = Collections.unmodifiableList(Stream.of(
			java.lang.Object.class,
			// wrapper types for primitives
			java.lang.Boolean.class,
			java.lang.Character.class,
			java.lang.Byte.class,
			java.lang.Short.class,
			java.lang.Integer.class,
			java.lang.Long.class,
			java.lang.Float.class,
			java.lang.Double.class,
			//
			java.lang.String.class,
			java.lang.CharSequence.class,
			java.lang.Iterable.class,
			//
			java.lang.Class.class,
			java.lang.Exception.class,
			java.lang.Throwable.class,
			//
			java.io.File.class,
			java.io.InputStream.class,
			java.nio.file.Path.class,
			//
			java.util.Date.class,
			java.util.Optional.class,
			java.util.OptionalDouble.class,
			java.util.OptionalInt.class,
			java.util.OptionalLong.class,
			java.util.Map.class,
			java.util.Iterator.class,
			//
			java.util.stream.Stream.class,
			java.util.stream.IntStream.class,
			java.util.stream.LongStream.class)
		.map(Class::getName)
		.collect(Collectors.toList()));

	public static boolean isSupportedTypeForAsseertion(ITypeBinding typeBinding) {

		if (ClassRelationUtil.isContentOfTypes(typeBinding, SUPPORTED_TYPES_FOR_ASSERTIONS)) {
			return true;
		}

		String packageName = typeBinding.getPackage()
			.getName();

		return packageName.equals(JAVA_UTIL)
				&& ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
						Arrays.asList(java.lang.Iterable.class.getName(), java.util.Iterator.class.getName(),
								java.util.Map.class.getName()));

	}

	private SupportedTypesForAssertions() {
		// hiding implicit default constructor
	}

}

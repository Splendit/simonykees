package eu.jsparrow.license.api.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * We rely on {@link ReflectionToStringBuilder} to generate some of our log
 * statements. This class allows us to leverage the power of the base class,
 * while allowing us to shorten some of the data using the {@link Shorten}
 * annotation.
 * 
 */
public class AnnotationToStringBuilder extends ReflectionToStringBuilder {

	public AnnotationToStringBuilder(Object object) {
		super(object);
	}

	public AnnotationToStringBuilder(Object object, ToStringStyle style) {
		super(object, style);
	}

	@Override
	protected Object getValue(Field field) throws IllegalAccessException {
		if (!field.getType()
			.isAssignableFrom(String.class)) {
			return super.getValue(field);
		}

		List<Annotation> annotations = Arrays.asList(field.getAnnotations());
		boolean hasShortenAnnotation = annotations.stream()
			.anyMatch(x -> x instanceof Shorten);
		if (!hasShortenAnnotation) {
			return super.getValue(field);
		}
		String value = (String) super.getValue(field);
		return StringUtils.abbreviate(value, 6);
	}
}

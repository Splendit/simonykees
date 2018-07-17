package eu.jsparrow.license.api.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used together with the {@link AnnotationToStringBuilder}
 * in order to shorten String fields of a class before serializing that class
 * into a String.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Shorten {

}

package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Expression;

/**
 * Wrapper class for the case that {@code assumeNotNull(Object...)} is called
 * with a single argument which may be interpreted as an object or as an array
 * of objects.
 */
public class AssumeNotNullWithSingleVararg {
	private Expression expressionResolvedAsObject;
	private ArrayCreation arrayCreation;
	private AssumptionThatEveryItemNotNull assumeNotNullWithNullableArray;

	AssumeNotNullWithSingleVararg(Expression expressionResolvedAsObject) {
		this.expressionResolvedAsObject = expressionResolvedAsObject;
	}

	AssumeNotNullWithSingleVararg(ArrayCreation arrayCreation) {
		this.arrayCreation = arrayCreation;
	}

	AssumeNotNullWithSingleVararg(AssumptionThatEveryItemNotNull assumeNotNullOnNullableArray) {
		this.assumeNotNullWithNullableArray = assumeNotNullOnNullableArray;
	}

	Optional<Expression> getExpressionResolvedAsObject() {
		return Optional.ofNullable(expressionResolvedAsObject);
	}

	Optional<ArrayCreation> getArrayCreation() {
		return Optional.ofNullable(arrayCreation);
	}

	Optional<AssumptionThatEveryItemNotNull> getAssumeNotNullWithNullableArray() {
		return Optional.ofNullable(assumeNotNullWithNullableArray);
	}
}

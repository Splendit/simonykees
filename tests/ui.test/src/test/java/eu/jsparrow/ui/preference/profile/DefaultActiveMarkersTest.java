package eu.jsparrow.ui.preference.profile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultActiveMarkersTest {

	private DefaultActiveMarkers defaultActiveMarkers;

	@BeforeEach
	void setUp() {
		defaultActiveMarkers = new DefaultActiveMarkers();
	}

	@Test
	void testDefaultMarkersSize() {
		List<String> actual = defaultActiveMarkers.getActiveMarkers();
		assertThat(actual, hasSize(27));
	}
	@Test
	void testDefaultActiveMarkers_shouldReturnAllMarkers() {
		List<String> actual = defaultActiveMarkers.getActiveMarkers();
		assertAll(
				() -> actual.contains("AvoidConcatenationInLoggingStatementsResolver"),
				() -> actual.contains("CollectionRemoveAllResolver"),
				() -> actual.contains("DiamondOperatorResolver"),
				() -> actual.contains("EnumsWithoutEqualsResolver"),
				() -> actual.contains("FunctionalInterfaceResolver"),
				() -> actual.contains("IndexOfToContainsResolver"),
				() -> actual.contains("InefficientConstructorResolver"),
				() -> actual.contains("InsertBreakStatementInLoopsResolver"),
				() -> actual.contains("LambdaToMethodReferenceResolver"),
				() -> actual.contains("MapGetOrDefaultResolver"),
				() -> actual.contains("PrimitiveBoxedForStringResolver"),
				() -> actual.contains("PutIfAbsentResolver"),
				() -> actual.contains("RemoveNewStringConstructorResolver"),
				() -> actual.contains("RemoveNullCheckBeforeInstanceofResolver"),
				() -> actual.contains("RemoveRedundantTypeCastResolver"),
				() -> actual.contains("RemoveUnusedParameterResolver"),
				() -> actual.contains("StringLiteralEqualityCheckResolver"),
				() -> actual.contains("UseCollectionsSingletonListResolver"),
				() -> actual.contains("UseComparatorMethodsResolver"),
				() -> actual.contains("UseIsEmptyOnCollectionsResolver"));

	}

}

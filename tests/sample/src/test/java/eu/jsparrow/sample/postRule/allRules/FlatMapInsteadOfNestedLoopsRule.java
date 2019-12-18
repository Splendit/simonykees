package eu.jsparrow.sample.postRule.allRules;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
@SuppressWarnings("nls")
public class FlatMapInsteadOfNestedLoopsRule {

	private static final Logger logger = LoggerFactory.getLogger(FlatMapInsteadOfNestedLoopsRule.class);

	public void test() {
		final List<List<List<String>>> matrix2 = Collections
			.singletonList(Collections.singletonList(Arrays.asList("asdf", "jkl")));
		// I don't want to break anything
		// I don't want to break anything
		// inner comment one
		/* lambda leading comment */
		/* lambda-body leading comment */
		// inner comment two
		// inner comment three
		/* inner comment four */
		/* lambda-body trailing comment */
		// trailing comment two
		// trailing comment one
		// outer comment
		matrix2.stream()
			.filter(row -> !row.isEmpty()) // I don't want to break anything
			.flatMap(List::stream)
			.filter(col -> !col.isEmpty())
			.flatMap(List::stream)
			.filter(element -> !StringUtils.isEmpty(element))
			.map(element -> StringUtils.substring(element, 0, 1))
			.forEach(logger::info);

		final List<List<List<List<String>>>> matrix3 = Collections
			.singletonList(Collections.singletonList(Collections.singletonList(Arrays.asList("asdf", "jkl"))));
		matrix3.stream()
			.filter(row -> !row.isEmpty())
			.flatMap(List::stream)
			.filter(col -> !col.isEmpty())
			.flatMap(List::stream)
			.filter(cell -> !cell.isEmpty())
			.flatMap(List::stream)
			.filter(element -> !StringUtils.isEmpty(element))
			.map(element -> StringUtils.substring(element, 0, 1))
			.forEach(logger::info);

		final List<List<String>> matrix = Collections.singletonList(Arrays.asList("asdf", "jkl"));
		matrix.stream()
			.filter(row -> !row.isEmpty())
			.forEach(row -> {
				logger.info(String.valueOf(row));
				row.stream()
					.filter(element -> !StringUtils.isEmpty(element))
					.map(element -> StringUtils.substring(element, 0, 1))
					.forEach(logger::info);
			});

		/* comment inside filter */
		/* some comment inside map */
		// some comment here
		matrix.stream()
			.filter(row -> !row.isEmpty())
			.flatMap(List::stream)
			.filter(element -> !StringUtils.isEmpty(element))
			.map(element -> StringUtils.substring(element, 0, 1))
			.forEach(element -> {
				logger.info(element);
				logger.info(element);
			});

		matrix.stream()
			.flatMap(List::stream)
			.filter(element -> !StringUtils.isEmpty(element))
			.map(element -> StringUtils.substring(element, 0, 1))
			.forEach(logger::info);

		matrix.stream()
			.filter(row -> !row.isEmpty())
			.flatMap(List::stream)
			.forEach(logger::info);

		matrix.stream()
			.flatMap(List::stream)
			.forEach(logger::info);

		matrix.stream()
			.flatMap(List::stream)
			.forEach(logger::info);

		matrix.stream()
			.flatMap(List::stream)
			.forEach(logger::info);

		matrix.stream()
			.flatMap(List::stream)
			.forEach(logger::info);

		matrix.stream()
			.flatMap(List::stream)
			.forEach(logger::info);

		matrix.stream()
			.forEach(row -> matrix.get(row.size())
				.stream()
				.forEach(logger::info));

		matrix.stream()
			.forEach(row -> row.stream()
				.filter(element -> !StringUtils.isEmpty(element))
				.forEach(element -> logger.info(row + element)));

		class TestObject {
			List<String> testList = Arrays.asList("asdf", "jkl");

			public List<String> getTestList() {
				return testList;
			}
		}

		final List<TestObject> matrix4 = Arrays.asList(new TestObject(), new TestObject());
		matrix4.forEach(t -> t.getTestList()
			.forEach(logger::info));
	}

	public void testAvoidingOuterMostLoop() {
		final List<List<List<String>>> matrix2 = Collections
			.singletonList(Collections.singletonList(Arrays.asList("asdf", "jkl")));
		matrix2.stream()
			.filter(row -> !row.isEmpty())
			.forEach(row -> {
				/*
				 * Some statement just to avoid transformation
				 */
				if (matrix2.size() == 2) {
					return;
				}
				row.stream()
					.filter(col -> !col.isEmpty())
					.flatMap(List::stream)
					.filter(element -> !StringUtils.isEmpty(element))
					.map(element -> StringUtils.substring(element, 0, 1))
					.forEach(logger::info);
			});
	}

	public void testAvoidInnerMostLoop() {
		final List<List<List<String>>> matrix2 = Collections
			.singletonList(Collections.singletonList(Arrays.asList("asdf", "jkl")));
		matrix2.stream()
			.filter(first -> !first.isEmpty())
			.flatMap(List::stream)
			.filter(second -> !second.isEmpty())
			.forEach(second -> {
				if (matrix2.size() == 2) {
					return;
				}
				second.stream()
					.filter(third -> !StringUtils.isEmpty(third))
					.map(third -> StringUtils.substring(third, 0, 1))
					.forEach(logger::info);
			});
	}

	public void testQuartedNestedStreams() {
		final List<List<List<List<String>>>> matrix3 = Collections
			.singletonList(Collections.singletonList(Collections.singletonList(Arrays.asList("asdf", "jkl"))));
		/*
		 * Some statement just to avoid transformation
		 */
		matrix3.stream()
			.flatMap(List::stream)
			.forEach(second -> {
				final int size = matrix3.size();
				second.forEach(third -> third.forEach(logger::info));
			});
	}
}

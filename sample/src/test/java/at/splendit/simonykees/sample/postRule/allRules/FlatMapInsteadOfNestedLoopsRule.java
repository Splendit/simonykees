package at.splendit.simonykees.sample.postRule.allRules;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.0.4
 */
@SuppressWarnings("nls")
public class FlatMapInsteadOfNestedLoopsRule {

	private static final Logger logger = LoggerFactory.getLogger(FlatMapInsteadOfNestedLoopsRule.class);

	public void test() {
		List<List<List<String>>> matrix2 = Arrays.asList(Arrays.asList(Arrays.asList("asdf", "jkl")));
		matrix2.stream().filter(row -> !row.isEmpty()).flatMap(List::stream).filter(col -> !col.isEmpty())
				.flatMap(List::stream).filter(element -> !StringUtils.isEmpty(element))
				.map(element -> StringUtils.substring(element, 0, 1)).forEach(logger::info);

		List<List<List<List<String>>>> matrix3 = Arrays
				.asList(Arrays.asList(Arrays.asList(Arrays.asList("asdf", "jkl"))));
		matrix3.stream().filter(row -> !row.isEmpty()).flatMap(List::stream).filter(col -> !col.isEmpty())
				.flatMap(List::stream).filter(cell -> !cell.isEmpty()).flatMap(List::stream)
				.filter(element -> !StringUtils.isEmpty(element)).map(element -> StringUtils.substring(element, 0, 1))
				.forEach(logger::info);

		List<List<String>> matrix = Arrays.asList(Arrays.asList("asdf", "jkl"));
		matrix.stream().filter((row) -> !row.isEmpty()).forEach((row) -> {
			System.out.print(row);
			row.stream().filter((element) -> !StringUtils.isEmpty(element))
					.map((element) -> StringUtils.substring(element, 0, 1)).forEach(logger::info);
		});

		matrix.stream().filter(row -> !row.isEmpty()).flatMap(List::stream)
				.filter(element -> !StringUtils.isEmpty(element)).map(element -> StringUtils.substring(element, 0, 1))
				.forEach(logger::info);

		matrix.stream().flatMap(List::stream).filter(element -> !StringUtils.isEmpty(element))
				.map(element -> StringUtils.substring(element, 0, 1)).forEach(logger::info);

		matrix.stream().filter(row -> !row.isEmpty()).flatMap(List::stream).forEach(logger::info);

		matrix.stream().flatMap(List::stream).forEach(logger::info);

		matrix.stream().flatMap(List::stream).forEach(logger::info);

		matrix.stream().flatMap(List::stream).forEach(logger::info);

		matrix.stream().flatMap(List::stream).forEach(logger::info);

		matrix.stream().flatMap(List::stream).forEach(logger::info);
	}
}

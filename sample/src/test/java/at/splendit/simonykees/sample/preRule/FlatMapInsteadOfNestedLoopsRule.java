package at.splendit.simonykees.sample.preRule;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.0.4
 */
public class FlatMapInsteadOfNestedLoopsRule {

	public void test() {
		List<List<String>> matrix = Arrays.asList(Arrays.asList("asdf", "jkl"));
		for (List<String> row : matrix) {
			if (!row.isEmpty()) {
				System.out.print(row);
				for (String element : row) {
					if (!element.isEmpty()) {
						String prefix = element.substring(0, 1);
						System.out.print(prefix);
					}
				}
			}
		}

		matrix.stream().filter(row -> !row.isEmpty()).forEach(row -> {
			row.stream().filter(element -> !element.isEmpty()).map(element -> element.substring(0, 1))
					.forEach(element -> {
						System.out.print(element);
					});
		});

		matrix.forEach(row -> {
			row.forEach(element -> {
				System.out.print(element);
			});
		});

		for (List<String> row : matrix) {
			for (String element : row) {
				System.out.print(element);
			}
		}
	}

}

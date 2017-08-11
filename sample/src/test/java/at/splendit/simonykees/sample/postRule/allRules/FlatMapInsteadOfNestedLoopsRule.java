package at.splendit.simonykees.sample.postRule.allRules;

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
		matrix.stream().filter(row -> !row.isEmpty()).flatMap(row -> row.stream()).filter(element -> !element.isEmpty())
				.map(element -> element.substring(0, 1)).forEach(prefix -> {
					System.out.print(prefix);
				});

		matrix.stream().forEach(row -> {
			row.stream().forEach(element -> {
				System.out.println(element);
			});
		});
	}

}

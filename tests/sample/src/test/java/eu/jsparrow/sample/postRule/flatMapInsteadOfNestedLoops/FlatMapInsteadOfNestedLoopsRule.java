package eu.jsparrow.sample.postRule.flatMapInsteadOfNestedLoops;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
@SuppressWarnings("nls")
public class FlatMapInsteadOfNestedLoopsRule {
	
	public void test() {
		List<List<List<String>>> matrix2 = Arrays.asList(Arrays.asList(Arrays.asList("asdf", "jkl")));
		// inner comment one
		// trailing comment one 
		// I don't want to break anything
		// inner comment two
		// trailing comment two
		/* lambda-body leading comment */
		/* lambda leading comment */
		// inner comment three
		/* inner comment four */
		/* lambda-body trailing comment */
		// outer comment
		matrix2.stream().filter(row -> !row.isEmpty()) // I don't want to break anything
.flatMap(row -> row.stream())
			.filter(col -> !col.isEmpty())
			.flatMap(col -> col.stream())
			.filter(element -> !element.isEmpty())
			.map(element -> element.substring(0, 1))
			.forEach(element -> {
				System.out.print(element);
			});

		List<List<List<List<String>>>> matrix3 = Arrays.asList(Arrays.asList(Arrays.asList(Arrays.asList("asdf", "jkl"))));
		matrix3.stream().filter(row -> !row.isEmpty()).flatMap(row -> row.stream())
			.filter(col -> !col.isEmpty())
			.flatMap(col -> col.stream())
			.filter(cell -> !cell.isEmpty())
			.flatMap(cell -> cell.stream())
			.filter(element -> !element.isEmpty())
			.map(element -> element.substring(0, 1))
			.forEach(element -> {
				System.out.print(element);
			});

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

		// some comment here
		matrix.stream().filter(row -> !row.isEmpty()).flatMap(row -> row.stream())
			.filter(element -> /* comment inside filter */ !element.isEmpty())
			.map(element -> /* some comment inside map */ element.substring(0, 1))
			.forEach(element -> {
				System.out.print(element);
				System.out.print(element);
			});

		matrix.stream()
			.flatMap(row -> row.stream())
			.filter(element -> !element.isEmpty())
			.map(element -> element.substring(0, 1))
			.forEach(element -> {
				System.out.print(element);
			});

		matrix.stream().filter(row -> !row.isEmpty()).flatMap(row -> row.stream())
			.forEach(element -> {
				System.out.print(element);
			});

		for (List<String> row : matrix) {
			for (String element : row) {
				System.out.print(element);
			}
		}

		matrix.stream()
			.flatMap(row -> row.stream())
			.forEach(element -> {
				System.out.print(element);
			});

		matrix.stream().flatMap(row -> row.stream())
			.forEach(element -> {
				System.out.print(element);
			});

		matrix.stream().flatMap(row -> row.stream())
			.forEach(element -> {
				System.out.print(element);
			});

		matrix.stream()
			.flatMap(row -> row.stream())
			.forEach(element -> {
				System.out.print(element);
			});

		matrix.stream().forEach(row -> {
			matrix.get(row.size()).stream().forEach(element -> {
				System.out.println(element);
			});
		});

		matrix.stream().forEach(row -> {
			row.stream().filter(element -> !element.isEmpty()).forEach(element -> {
				System.out.println(row + element);
			});
		});

		class TestObject {
			List<String> testList = Arrays.asList("asdf", "jkl");

			public List<String> getTestList() {
				return testList;
			}
		}

		List<TestObject> matrix4 = Arrays.asList(new TestObject(), new TestObject());
		matrix4.forEach(t -> {
			t.getTestList().forEach(element -> {
				System.out.println(element);
			});
		});
	}
	
	public void testAvoidingOuterMostLoop() {
		List<List<List<String>>> matrix2 = Arrays.asList(Arrays.asList(Arrays.asList("asdf", "jkl")));
		matrix2.stream().filter(row -> !row.isEmpty()).forEach(row -> {
			/*
			 * Some statement just to avoid transformation
			 */
			if(matrix2.size() == 2) {
				return;
			}
			row.stream().filter(col -> !col.isEmpty()).flatMap(col -> col.stream())
				.filter(element -> !element.isEmpty())
				.map(element -> element.substring(0, 1))
				.forEach(element -> {
					System.out.print(element);
				});
		});
	}

	public void testAvoidInnerMostLoop() {
		List<List<List<String>>> matrix2 = Arrays.asList(Arrays.asList(Arrays.asList("asdf", "jkl")));
		matrix2.stream().filter(first -> !first.isEmpty()).flatMap((first) -> first.stream()).filter(second -> !second.isEmpty()).forEach(second -> {
			if (matrix2.size() == 2) {
				return;
			}
			second.stream().filter(third -> !third.isEmpty()).map(third -> third.substring(0, 1)).forEach(third -> {
				System.out.print(third);
			});
		});
	}
	
	public void testQuartedNestedStreams() {
		List<List<List<List<String>>>> matrix3 = Arrays.asList(Arrays.asList(Arrays.asList(Arrays.asList("asdf", "jkl"))));
		/*
		 * Some statement just to avoid transformation
		 */
		matrix3.stream().flatMap(first -> first.stream())
			.forEach(second -> {
				int size = matrix3.size();
				second.forEach(third -> {
					third.forEach(fourth -> {
						System.out.print(fourth);
					});
				});
			});
	}
}

package at.splendit.simonykees.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.List;

public class EnhancedForLoopToStreamSumRule {

	public int forToStreamSum(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum = 0;
		for (int n : numbers) {
			sum += n;
		}
		return sum;
	}

	public int forToStreamSumPlusOperation(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum = 0;
		for (int n : numbers) {
			sum += n;
		}
		return sum;
	}

	public int multipleDeclarationFragments(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum = 0;
		int sum2 = 0;
		for (int n : numbers) {
			sum += n;
		}
		return sum + sum2;
	}

	public int multipleDeclarationStatements(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum = 0;
		int sum2 = 0;
		double sum3 = 1;
		for (int n : numbers) {
			sum += n;
		}
		return sum + sum2;
	}

	public int wrongInit(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum = 1;
		for (int n : numbers) {
			sum += n;
		}
		return sum;
	}

	public int modifiedSumVariable(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum = 0;
		sum = 1;
		for (int n : numbers) {
			sum += n;
		}
		return sum;
	}

	public int multipleStatementsInLoopBody(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum = 0;
		int sum2 = 0;
		for (int n : numbers) {
			sum += n;
			sum2 += sum;
		}
		return sum + sum2;
	}

	public int forToStreamSumSingleBodyStatement(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum = 0;
		for (int n : numbers) {
			sum += n;
		}
		return sum;
	}

	public double sumListOfDoubles(String input) {
		List<Double> numbers = generateDoubleList(input);
		double sum = 0;
		for (double n : numbers) {
			sum += n;
		}
		return sum;
	}

	public long sumListOfLongs(String input) {
		List<Long> numbers = generateLongList(input);
		long sum = 0;
		for (double n : numbers) {
			sum += n;
		}
		return sum;
	}

	private List<Long> generateLongList(String input) {
		List<Long> intList = new ArrayList<>();
		for (Byte aByte : input.getBytes()) {
			intList.add(aByte.longValue());
		}
		return intList;
	}

	private List<Double> generateDoubleList(String input) {
		List<Double> intList = new ArrayList<>();
		for (Byte aByte : input.getBytes()) {
			intList.add(aByte.doubleValue());
		}
		return intList;
	}

	private List<Integer> generateIntList(String input) {
		List<Integer> intList = new ArrayList<>();
		for (Byte aByte : input.getBytes()) {
			intList.add(aByte.intValue());
		}

		return intList;
	}
}

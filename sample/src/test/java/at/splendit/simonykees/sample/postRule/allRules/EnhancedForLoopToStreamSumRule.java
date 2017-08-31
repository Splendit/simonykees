package at.splendit.simonykees.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class EnhancedForLoopToStreamSumRule {

	public int forToStreamSum(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum = numbers.stream().mapToInt(Integer::intValue).sum();
		return sum;
	}

	public int forToStreamSumPlusOperation(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum = numbers.stream().mapToInt(Integer::intValue).sum();
		return sum;
	}

	public int multipleDeclarationFragments(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum2 = 0;
		int sum = numbers.stream().mapToInt(Integer::intValue).sum();
		return sum + sum2;
	}

	public int multipleDeclarationStatements(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum2 = 0;
		double sum3 = 1;
		int sum = numbers.stream().mapToInt(Integer::intValue).sum();
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
		int sum = numbers.stream().mapToInt(Integer::intValue).sum();
		return sum;
	}

	/*
	 * Boxed sum variable
	 */

	public double boxedIntegerSum(String input) {
		List<Integer> numbers = generateIntList(input);
		Integer sum = numbers.stream().mapToInt(Integer::intValue).sum();
		return sum;
	}

	public long boxedLongSum(String input) {
		List<Long> numbers = generateLongList(input);
		Long sum = numbers.stream().mapToLong(Long::longValue).sum();
		return sum;
	}

	public double boxedDoubleSum(String input) {
		List<Double> numbers = generateDoubleList(input);
		Double sum = numbers.stream().mapToDouble(Double::doubleValue).sum();
		return sum;
	}

	public double zeroDotZeroLiteral(String input) {
		List<Double> numbers = generateDoubleList(input);
		Double sum = numbers.stream().mapToDouble(Double::doubleValue).sum();
		return sum;
	}

	public double zeroDotZeroZeroLiteral(String input) {
		List<Double> numbers = generateDoubleList(input);
		Double sum = numbers.stream().mapToDouble(Double::doubleValue).sum();
		return sum;
	}

	/*
	 * various types of collections
	 */

	public double sumListOfDoubles(String input) {
		List<Double> numbers = generateDoubleList(input);
		double sum = numbers.stream().mapToDouble(Double::doubleValue).sum();
		return sum;
	}

	public long sumListOfLongs(String input) {
		List<Long> numbers = generateLongList(input);
		long sum = numbers.stream().mapToLong(Long::longValue).sum();
		return sum;
	}

	public int sumListOfShorts(String input) {
		List<Short> numbers = new ArrayList<>();
		int sum = numbers.stream().mapToInt(Short::intValue).sum();
		return sum;
	}

	public double sumListOfFloats(String input) {
		List<Float> numbers = new ArrayList<>();
		double sum = numbers.stream().mapToDouble(Float::doubleValue).sum();
		return sum;
	}

	public double sumListOfBytes(String input) {
		List<Byte> numbers = new ArrayList<>();
		double sum = numbers.stream().mapToDouble(Byte::doubleValue).sum();
		return sum;
	}

	/**
	 * unsupported sum type
	 */
	public short unSupportedType(String input) {
		List<Short> numbers = new ArrayList<>();
		short sum = 0;
		for (short s : numbers) {
			sum += s;
		}
		return sum;
	}

	/*
	 * Summing values of different types. The sum variable could have different
	 * type from the elements of the collection. i.e. adding apples to pears...
	 */

	public int incompatibleTypes(String input) {
		List<Double> numbers = generateDoubleList(input);
		int sum = numbers.stream().mapToInt(Double::intValue).sum();
		return sum;
	}

	public int incompatibleTypes2(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum = numbers.stream().mapToInt(Integer::intValue).sum();
		return sum;
	}

	public double incompatibleTypes3(String input) {
		List<Integer> numbers = generateIntList(input);
		double sum = numbers.stream().mapToDouble(Integer::doubleValue).sum();
		return sum;
	}

	public double incompatibleTypes4(String input) {
		List<Long> numbers = generateLongList(input);
		double sum = numbers.stream().mapToDouble(Long::doubleValue).sum();
		return sum;
	}

	public long incompatibleTypes5(String input) {
		List<Double> numbers = generateDoubleList(input);
		long sum = numbers.stream().mapToLong(Double::longValue).sum();
		return sum;
	}

	public long incompatibleTypes6(String input) {
		List<Integer> numbers = generateIntList(input);
		long sum = numbers.stream().mapToLong(Integer::longValue).sum();
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

	/*
	 * Multiple operands
	 */

	public int multipleOperands(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum = 0;
		for (int n : numbers) {
			sum = sum + n + 2;
		}
		return sum;
	}

	public int multipleOperandsAssignmentExp(String input) {
		List<Integer> numbers = generateIntList(input);
		int sum = 0;
		for (int n : numbers) {
			sum += n + 2;
		}
		return sum;
	}
}

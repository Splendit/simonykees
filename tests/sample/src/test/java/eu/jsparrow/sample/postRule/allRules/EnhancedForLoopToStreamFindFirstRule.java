package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
@SuppressWarnings({ "nls" })
public class EnhancedForLoopToStreamFindFirstRule {

	private static final Logger logger = LoggerFactory.getLogger(EnhancedForLoopToStreamFindFirstRule.class);
	private String globalKey = "";

	public String convertToFindFirstBreak(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> values = generateList(input);
		// comment before loop
		// comment after loop head
		// comment inside 1
		// comment inside 2
		// comment after
		String key = values // comment after loop head
			.stream()
			.filter(value -> value.length() > 4)
			.findFirst()
			.orElse("");
		sb.append(key);

		return sb.toString();
	}

	public String focingTailingMap(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> values = generateList(input);
		String key = values.stream()
			.filter(value -> value.length() > 4)
			.findFirst()
			.map(value -> value + " sth to force a tailing map")
			.orElse("");
		sb.append(key);

		return sb.toString();
	}

	public String methodInvocationAsInitializer(String input) {
		final List<String> values = generateList(input);
		String key = values.stream()
			.filter(value -> value.length() > 4)
			.findFirst()
			.orElse(values.get(0));

		return key;
	}

	public String irrelevantAssignment(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> values = generateList(input);
		String key = values.stream()
			.filter(value -> value.length() > 4)
			.findFirst()
			.map(value -> "sth irrelevant")
			.orElse("");
		sb.append(key);

		return sb.toString();
	}

	public String multipleDeclarationFragment(String input) {
		final StringBuilder sb = new StringBuilder();
		final String anotherKey = input;
		final List<String> values = generateList(anotherKey);
		String key = values.stream()
			.filter(value -> value.length() > 4)
			.findFirst()
			.orElse("");
		sb.append(key + anotherKey);

		return sb.toString();
	}

	public String referencedVariable(String input) {
		final StringBuilder sb = new StringBuilder();
		String key = "";
		final List<String> values = generateList(input);
		key = "key";
		for (String value : values) {
			if (value.length() > 4) {
				key = value;
				break;
			}
		}
		sb.append(key);

		return sb.toString();
	}

	public String multipleIfThenBodyStatements(String input) {
		final StringBuilder sb = new StringBuilder();
		String key = "";
		final List<String> values = generateList(input);
		for (String value : values) {
			if (value.length() > 4) {
				sb.append(value.length());
				key = value;
				break;
			}
		}
		sb.append(key);

		return sb.toString();
	}

	public String multipleLoopBodyStatements(String input) {
		final StringBuilder sb = new StringBuilder();
		String key = "";
		final List<String> values = generateList(input);
		for (String value : values) {
			if (value.length() > 4) {
				key = value;
				break;
			}
			sb.append(value.length());
		}
		sb.append(key);

		return sb.toString();
	}

	public String elseClause(String input) {
		final StringBuilder sb = new StringBuilder();
		String key = "";
		final List<String> values = generateList(input);
		for (String value : values) {
			if (value.length() > 4) {
				key = value;
				break;
			} else {
				sb.append(value.length());
			}
		}
		sb.append(key);

		return sb.toString();
	}

	public String missingDeclarationFragment(String input) {
		final StringBuilder sb = new StringBuilder();

		final List<String> values = generateList(input);
		for (String value : values) {
			if (value.length() > 4) {
				globalKey = value;
				break;
			}
		}
		sb.append(globalKey);

		return sb.toString();
	}

	public String missingBreakStatement(String input) {
		String localKey = "localKey";
		final List<String> values = generateList(input);
		for (String value : values) {
			if (value.length() > 4) {
				localKey = value;
			}
		}

		return localKey;
	}

	public String missingIfStatement(String input) {
		String localKey = "localKey";
		final List<String> values = generateList(input);
		for (String value : values) {
			localKey = value;
			break;
		}

		return localKey;
	}

	public String nonEffectivelyFinalCondition(String input) {
		final List<String> values = generateList(input);
		String localKey = "localKey";
		String key = "key";
		key = "";
		for (String value : values) {
			if (value.equals(key)) {
				localKey = value;
				break;
			}
		}

		return localKey;
	}

	public String referencingFinalVariable(String input) {
		final List<String> values = generateList(input);
		final String key = "key";
		String localKey = values.stream()
			.filter(value -> value.equals(key))
			.findFirst()
			.orElse("localKey");

		return localKey;
	}

	public String throwException(String input) throws Exception {
		final List<String> values = generateList(input);
		String localKey = "localKey";
		final String key = "key";
		for (String value : values) {
			if (compareAndThrowException(value, key)) {
				localKey = value;
				break;
			}
		}

		return localKey;
	}

	public String assigningNullValue(String input) {
		final List<String> values = generateList(input);
		String localKey = "localKey";
		final String key = "key";
		for (String value : values) {
			if (value.equals(key)) {
				localKey = null;
				break;
			}
		}

		return localKey;
	}

	public double implicitBreakCasting00(String input) {
		final List<Integer> values = new ArrayList<>();
		int defaultIndex = values.stream()
			.filter(value -> value > 4)
			.findFirst()
			.orElse(-1);

		return defaultIndex;
	}

	public double implicitBreakCasting01(String input) {
		final List<String> values = generateList(input);
		int defaultIndex = values.stream()
			.filter(value -> value.length() > 4)
			.findFirst()
			.map(String::length)
			.orElse(-1);

		return defaultIndex;
	}

	public double implicitBreakCasting20(String input) {
		final List<Integer> values = new ArrayList<>();
		double defaultIndex = values.stream()
			.filter(value -> value > 4)
			.findFirst()
			.map(Double::valueOf)
			.orElse(-1.0);

		return defaultIndex;
	}

	public double implicitBreakCasting21(String input) {
		final List<Integer> values = new ArrayList<>();
		double defaultIndex = values.stream()
			.filter(value -> value > 4)
			.findFirst()
			.map(value -> Double.valueOf(value + 1))
			.orElse(-1.0);

		return defaultIndex;
	}

	public double implicitBreakCasting30(String input) {
		final double defaultIndex = -1.0;
		final List<Double> values = new ArrayList<>();
		// comment before loop
		// comment inside
		return values.stream()
			.filter(value -> value > 4)
			.findFirst()
			.orElse(defaultIndex);
	}

	public double implicitBreakCasting31(String input) {
		final List<Double> values = new ArrayList<>();
		double defaultIndex = values.stream()
			.filter(value -> value > 4)
			.findFirst()
			.map(value -> value * 3.1)
			.orElse(Double.valueOf(-1));
		return defaultIndex;
	}

	/*
	 * ************* Loops with return statement ***************
	 */

	public String convertToFindFirstReturn(String input) {
		final List<String> values = generateList(input);
		logger.info("I dont care what happens next!");
		return values.stream()
			.filter(value -> value.length() > 4)
			.findFirst()
			.orElse("");
	}

	public String missingBrackets(String input) {
		final List<String> values = generateList(input);
		return values.stream()
			.filter(value -> value.length() > 4)
			.findFirst()
			.orElse("");
	}

	public String missingBrackets2(String input) {
		final List<String> values = generateList(input);
		return values.stream()
			.filter(value -> value.length() > 4)
			.findFirst()
			.orElse("");
	}

	public String forcingTailingMap(String input) {
		final List<String> values = generateList(input);
		return values.stream()
			.filter(value -> value.length() > 4)
			.findFirst()
			.map(value -> value + "sth to force a tailing map")
			.orElse("nothing long was found");
	}

	public String returningIrrelevantValue(String input) {
		final List<String> values = generateList(input);
		return values.stream()
			.filter(value -> value.length() > 4)
			.findFirst()
			.map(value -> "nothingToDo with 'value'")
			.orElse("");
	}

	public String missingReturnValue(String input) {
		final List<String> values = generateList(input);
		values.stream()
			.filter(value -> value.length() > 4)
			.forEach(value -> {
				// missing body
			});

		return "";
	}

	public String throwException2(String input) throws Exception {
		final List<String> values = generateList(input);
		final String localKey = "localKey";
		final String key = "key";
		for (String value : values) {
			if (compareAndThrowException(value, key)) {
				return value;
			}
		}

		return localKey;
	}

	public String methodInvocationAsReturnExpression(String input) {
		final List<String> values = generateList(input);
		return values.stream()
			.filter(value -> value.length() > 4)
			.findFirst()
			.orElse(values.get(0));
	}

	public String returningNullValue(String input) {
		final List<String> values = generateList(input);
		for (String value : values) {
			if (value.length() > 4) {
				return null;
			}
		}

		return values.get(0);
	}

	public double implicitReturnCasting00(String input) {
		final int defaultIndex = -1;
		final List<Integer> values = new ArrayList<>();
		return values.stream()
			.filter(value -> value > 4)
			.findFirst()
			.orElse(defaultIndex);
	}

	public double implicitReturnCasting01(String input) {
		final int defaultIndex = -1;
		final List<String> values = generateList(input);
		return values.stream()
			.filter(value -> value.length() > 4)
			.findFirst()
			.map(String::length)
			.orElse(defaultIndex);
	}

	public double implicitReturnCasting10(String input) {
		final int defaultIndex = -1;
		final List<Double> values = new ArrayList<>();
		return values.stream()
			.filter(value -> value > 4)
			.findFirst()
			.orElse(Double.valueOf(defaultIndex));
	}

	public double implicitReturnCasting11(String input) {
		final int defaultIndex = -1;
		final List<Double> values = new ArrayList<>();
		return values.stream()
			.filter(value -> value > 4)
			.findFirst()
			.map(value -> value * 2)
			.orElse(Double.valueOf(defaultIndex));
	}

	public double implicitReturnCasting20(String input) {
		final double defaultIndex = -1;
		final List<Integer> values = new ArrayList<>();
		return values.stream()
			.filter(value -> value > 4)
			.findFirst()
			.map(Double::valueOf)
			.orElse(defaultIndex);
	}

	public double implicitReturnCasting21(String input) {
		final double defaultIndex = -1;
		final List<Integer> values = new ArrayList<>();
		return values.stream()
			.filter(value -> value > 4)
			.findFirst()
			.map(value -> Double.valueOf(value + 1))
			.orElse(defaultIndex);
	}

	public double implicitReturnCasting30(String input) {
		final double defaultIndex = -1;
		final List<Double> values = new ArrayList<>();
		return values.stream()
			.filter(value -> value > 4)
			.findFirst()
			.orElse(defaultIndex);
	}

	public double implicitReturnCasting31(String input) {
		final double defaultIndex = -1;
		final List<Double> values = new ArrayList<>();
		return values.stream()
			.filter(value -> value > 4)
			.findFirst()
			.map(value -> value * 2)
			.orElse(defaultIndex);
	}

	public ReturnTypeSample returnSubtypeInstead(String input) {
		/*
		 * SIM-798
		 */
		final List<String> values = generateList(input);
		return values.stream()
			.filter(value -> value.equals(input))
			.findFirst()
			.map(ReturnTypeSampleChild::new)
			.orElse(null);
	}

	public ReturnTypeSample returnSubtypeOrSibling_shouldNotTransform(String input) {
		final List<String> values = generateList(input);
		for (String value : values) {
			if (value.equals(input)) {
				return new ReturnTypeSampleChild(value);
			}
		}
		return new ReturnTypeSampleChildSibling("");
	}

	public Object mapToParameterizedType_shouldNotTransform(String input) {
		final List<String> values = generateList(input);
		for (String value : values) {
			if (value.equals(input)) {
				return createParaeterizedInstance(value);
			}
		}
		return new Object();
	}

	private ParameterizedSampleType<?> createParaeterizedInstance(String value) {
		value.chars();
		return new ParameterizedSampleType<ReturnTypeSampleChild>();
	}

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(","));
	}

	private boolean compareAndThrowException(String value, String key) throws Exception {
		if (value == null || key == null) {
			throw new Exception();
		}
		return value.equals(key);
	}

	class ReturnTypeSample {
		ReturnTypeSample(String value) {

		}
	}

	class ParameterizedSampleType<T extends ReturnTypeSample> {

		public ReturnTypeSample streamOfTypeVariable_shouldNotAddCasting() {
			/*
			 * SIM-1336
			 */
			final ReturnTypeSample typeSample = new ReturnTypeSample("");
			return getAllTypeSamples().stream()
				.filter(value -> value.equals(typeSample))
				.findFirst()
				.orElse(null);
		}

		private List<T> getAllTypeSamples() {
			return new ArrayList<>();
		}
	}

	class ReturnTypeSampleChild extends ReturnTypeSample {

		ReturnTypeSampleChild(String value) {
			super(value);
		}

	}

	class ReturnTypeSampleChildSibling extends ReturnTypeSample {

		ReturnTypeSampleChildSibling(String value) {
			super(value);
		}

	}
}

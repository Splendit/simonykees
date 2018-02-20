package eu.jsparrow.sample.postRule.allRules;

@SuppressWarnings("nls")
public class TestInefficientConstructorPrimitiveRule {

	Byte b1 = Byte.valueOf((byte) 1);
	Byte b3 = Byte.valueOf("1");

	public Integer primIntegerToIntegerTest() {
		return Integer.valueOf(1);
	}

	public Integer stringToIntegerTest() {
		/* leading comment */
		/* internal comment */
		/* trailing comment */
		return Integer.valueOf(/* argument */ "1");
	}

	public Double primDoubleToDoubleTest() {
		/* leading comment */
		/* internal comment */
		/* trailing comment */
		return Double.valueOf(/* one . o */ 1.0);
	}

	public Double stringToDoubleTest() {
		return Double.valueOf(/* string one dot zero */ "1.0");
	}

	public Float primFloatToFloatTest() {
		return Float.valueOf(/* float one dot zero */1.0f);
	}

	public Float primDoubleToFloatTest() {
		return new Float(/* float one dot zero */ 1.0);
	}

	public Float stringToFloatTest() {
		return Float.valueOf("1.0f");
	}

	public Long primLongToLongTest() {
		return Long.valueOf(/* One Long */1L);
	}

	public Long stringToLongTest() {
		return Long.valueOf("1L");
	}

	public Short primShortToShortTest() {
		return Short.valueOf((short) 1);
	}

	public Short stringToShortTest() {
		return Short.valueOf("1");
	}

	public Character characterToCharacterTest() {
		return Character.valueOf('c');
	}

	public Byte primByteToByteTest() {
		return Byte.valueOf((byte) 1);
	}

	public Byte stringToByteTest() {
		return Byte.valueOf("1");
	}

	public Number doubleToNumberTest() {
		return new TestNumberConstructor(2d).getValue();
	}

	private static class TestNumberConstructor {

		private Number value;

		protected TestNumberConstructor(double value) {
			this(Double.valueOf(value));
		}

		protected TestNumberConstructor(Number value) {
			this.value = value;
		}

		protected Number getValue() {
			return this.value;
		}

	}

}

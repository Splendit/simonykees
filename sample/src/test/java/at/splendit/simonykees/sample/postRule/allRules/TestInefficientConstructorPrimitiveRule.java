package at.splendit.simonykees.sample.postRule.allRules;

@SuppressWarnings("nls")
public class TestInefficientConstructorPrimitiveRule {

	public Integer primIntegerToIntegerTest() {
		return 1;
	}

	public Integer stringToIntegerTest() {
		return Integer.valueOf("1");
	}

	public Double primDoubleToDoubleTest() {
		return 1.0;
	}

	public Double stringToDoubleTest() {
		return Double.valueOf("1.0");
	}

	public Float primFloatToFloatTest() {
		return 1.0f;
	}

	public Float primDoubleToFloatTest() {
		return new Float(1.0);
	}

	public Float stringToFloatTest() {
		return Float.valueOf("1.0f");
	}

	public Long primLongToLongTest() {
		return 1L;
	}

	public Long stringToLongTest() {
		return Long.valueOf("1L");
	}

	public Short primShortToShortTest() {
		return (short) 1;
	}

	public Short stringToShortTest() {
		return Short.valueOf("1");
	}

	public Character characterToCharacterTest() {
		return new Character('c');
	}

	public Byte primByteToByteTest() {
		return (byte) 1;
	}

	public Byte stringToByteTest() {
		return Byte.valueOf("1");
	}

	Byte b1 = (byte) 1;
	Byte b3 = Byte.valueOf("1");

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

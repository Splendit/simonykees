package eu.jsparrow.sample.preRule;

@SuppressWarnings("nls")
public class TestInefficientConstructorPrimitiveRule {

	public Integer primIntegerToIntegerTest() {
		return new Integer(1);
	}

	public Integer stringToIntegerTest() {
		return /* leading comment */ new /* internal comment */ Integer( /* argument */ "1") /* trailing comment */;
	}

	public Double primDoubleToDoubleTest() {
		return  /* leading comment */ new /* internal comment */ Double( /* one . o */ 1.0) /* trailing comment */;
	}

	public Double stringToDoubleTest() {
		return new Double(/* string one dot zero */ "1.0");
	}

	public Float primFloatToFloatTest() {
		return new Float(/* float one dot zero */1.0f);
	}

	public Float primDoubleToFloatTest() {
		return new Float(/* float one dot zero */ 1.0);
	}

	public Float stringToFloatTest() {
		return new Float("1.0f");
	}

	public Long primLongToLongTest() {
		return new Long(/* One Long */1L);
	}

	public Long stringToLongTest() {
		return new Long("1L");
	}

	public Short primShortToShortTest() {
		return new Short((short) 1);
	}

	public Short stringToShortTest() {
		return new Short("1");
	}

	public Character characterToCharacterTest() {
		return new Character('c');
	}

	public Byte primByteToByteTest() {
		return new Byte((byte) 1);
	}

	public Byte stringToByteTest() {
		return new Byte("1");
	}

	Byte b1 = new Byte((byte) 1);
	Byte b3 = new Byte("1");

	public Number doubleToNumberTest() {
		return new TestNumberConstructor(2d).getValue();
	}

	private static class TestNumberConstructor {

		private Number value;

		protected TestNumberConstructor(double value) {
			this(new Double(value));
		}

		protected TestNumberConstructor(Number value) {
			this.value = value;
		}

		protected Number getValue() {
			return this.value;
		}

	}

}

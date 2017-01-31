package at.splendit.simonykees.sample.preRule;

public class TestInefficientConstructorCornerCase {

	public void doubleInNumber(double d) {
		inNumber(d);
	}

	private Number inNumber(Number n) {
		return n;
	}

	public void test() {
		new Foo(1.0);
		new Foo(new Double(1.0));
	}

	private class Foo {
		public Foo(double value) {
			this(new Double(value));
		}

		public Foo(Double value) {

		}
		
		public Foo(Number value) {

		}
	}
}

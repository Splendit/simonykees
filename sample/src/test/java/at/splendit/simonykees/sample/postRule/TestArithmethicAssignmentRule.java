package at.splendit.simonykees.sample.postRule;

public class TestArithmethicAssignmentRule {

	private int a = 3;
	private int c = 3;
	private int b = 3;
	private int q = 3;

	public void doSomething(int i) {
		i += 1;
		i = 7 + 4 - 3;
		i += 3;
		i -= 3;
		i += 4 - 3;
		i += 4 + 3;
		i += 3 + 4;
		i += 3 - 4;
		i += a - 4;
		i = a + b;
		i += a;
		i *= a;
		b *= 4;
		q *= (3 + 4);
		q = a * b / c * q * 3 - i;
		a /= 4;
	}
}

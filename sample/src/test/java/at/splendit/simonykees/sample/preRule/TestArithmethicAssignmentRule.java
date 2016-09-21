package at.splendit.simonykees.sample.preRule;

public class TestArithmethicAssignmentRule {

	private int a = 3;
	private int c = 3;
	private int b = 3;
	private int q = 3;

	public void doSomething(int i) {
		i = i + 1;
		i = 7 + 4 - 3;
		i = i + 3;
		i = i - 3;
		i = i + 4 - 3;
		i = i + 4 + 3;
		i = 3 + i + 4;
		i = 3 + i - 4;
		i = a + i - 4;
		i = a + b;
		i = a + i;
		i = i * a;
		b = 4 * b;
		q = (3 + 4) * q;
		q = a * b / c * q * 3 - i;
		a = a / 4;
	}
}

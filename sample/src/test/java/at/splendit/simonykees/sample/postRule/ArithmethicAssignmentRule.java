package at.splendit.simonykees.sample.postRule;

public class ArithmethicAssignmentRule {

	private int a = 3;
	private int c = 3;
	private int b = 3;
	private int q = 3;

	public String a(String i) {
		i = i + "aaa"; //$NON-NLS-1$
		return i;
	}

	public int a(int i) {
		i += 1;
		return i;
	}

	public int b(int i) {
		i = 7 + 4 - 3;
		return i;
	}

	public int c(int i) {
		i += 3;
		return i;
	}

	public int d(int i) {
		i -= 3;
		return i;
	}

	public int e(int i) {
		i += 4 - 3;
		return i;
	}

	public int f(int i) {
		i += 4 + 3;
		return i;
	}

	public int g(int i) {
		i += 3 + 4;
		return i;
	}

	public int h(int i) {
		i += 3 - 4;
		return i;
	}

	public int i(int i) {
		i += a - 4;
		return i;
	}

	public int j(int i) {
		i = a + b;
		return i;
	}

	public int k(int i) {
		i += a;
		return i;
	}

	public int l(int i) {
		i *= a;
		return i;
	}

	public int m(int i) {
		i *= 4;
		return i;
	}

	public int n(int i) {
		i *= (3 + 4);
		return i;
	}

	public int o(int i) {
		i = a * b / c * q * 3 - i;
		return i;
	}

	public int p(int i) {
		i /= 4;
		return i;
	}

	public int q(int i) {
		i += 1 + 2 + 3 + 4;
		return i;
	}

	public int r(int i) {
		i += 1 + 2 + 4 + i;
		return i;
	}

	public int s(int i) {
		i = 1 * 2 + 4 * i;
		return i;
	}

	public int t(int i) {
		i += i * 4;
		return i;
	}

	public int u(int i) {
		i += 1 - i + i + 4;
		return i;
	}

	public int v(int i) {
		i *= (2 + 4 + i);
		return i;
	}

}

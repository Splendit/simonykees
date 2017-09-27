package eu.jsparrow.sample.postRule.primitiveObjectUseEquals;

@SuppressWarnings({ "nls", "unused" })
public class TestPrimitiveObjectUseEqualsRule {

	public void testIntegerShouldRefactor() {
		Integer a = new Integer(1);
		Integer b = new Integer(2);

		if (a.equals(b)) {
		}

		if (!a.equals(b)) {
		}

		if (!(a.equals(b))) {
		}

		if (!(!a.equals(b))) {
		}

		if (new Integer(1).equals(new Integer(2))) {
		}

		if (!new Integer(1).equals(new Integer(2))) {
		}
	}

	public void testStringShouldRefactor() {
		String a = new String("a");
		String b = new String("b");

		if (a.equals(b)) {
		}

		if (!a.equals(b)) {
		}

		if (!(a.equals(b))) {
		}

		if (!(!a.equals(b))) {
		}

		if ("a".equals("b")) {
		}

		if (!"a".equals("b")) {
		}

		if (new String("a").equals(new String("b"))) {
		}

		if (!new String("a").equals(new String("b"))) {
		}
	}

	public void testOtherPrimitivesShouldRefactor() {
		if (new Byte("1").equals(new Byte("2"))) {
		}

		if (new Character('a').equals(new Character('b'))) {
		}

		if (new Short("1").equals(new Short("2"))) {
		}

		if (new Long(1).equals(new Long(2))) {
		}

		if (new Float(1).equals(new Float(2))) {
		}

		if (new Double(1).equals(new Double(2))) {
		}

		if (new Boolean(true).equals(new Boolean(false))) {
		}
	}

	public void testActualPrimitivesShouldNotRefactor() {
		int a = 1;
		int b = 2;
		if (a == b) {
		}

		if (new Integer(1) == 2) {
		}

		if (1 == new Integer(2)) {
		}

		if (1f == 2f) {
		}

		if (1l == 2l) {
		}

		if (1d == 2d) {
		}

		if (true == false) {
		}
	}

	public void testOtherInfixShouldNotRefactor() {
		Integer a = new Integer(1);
		Integer b = new Integer(2);

		if (a >= b) {
		}

		if (a <= b) {
		}

		if (a < b) {
		}

		if (a > b) {
		}
	}

	public void testCastedExpressionShouldNotRefactor() {
		int c = 0;
		Integer d = new Integer(1);
		if (((Integer) c).equals(d)) {
		}
		if (c == (Integer) d) {
		}
	}
}
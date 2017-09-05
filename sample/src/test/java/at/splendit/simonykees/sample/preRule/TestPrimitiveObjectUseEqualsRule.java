package at.splendit.simonykees.sample.preRule;

public class TestPrimitiveObjectUseEqualsRule {

	public void testIntegerShouldRefactor() {
		Integer a = new Integer(1);
		Integer b = new Integer(2);

		if (a == b) {
		}

		if (a != b) {
		}

		if (!(a == b)) {
		}

		if (!(a != b)) {
		}

		if (new Integer(1) == new Integer(2)) {
		}

		if (new Integer(1) != new Integer(2)) {
		}
	}

	public void testStringShouldRefactor() {
		String a = new String("a");
		String b = new String("b");

		if (a == b) {
		}

		if (a != b) {
		}

		if (!(a == b)) {
		}

		if (!(a != b)) {
		}

		if ("a" == "b") {
		}

		if ("a" != "b") {
		}

		if (new String("a") == new String("b")) {
		}

		if (new String("a") != new String("b")) {
		}
	}

	public void testOtherPrimitivesShouldRefactor() {
		if (new Byte("1") == new Byte("2")) {
		}

		if (new Character('a') == new Character('b')) {
		}

		if (new Short("1") == new Short("2")) {
		}

		if (new Long(1) == new Long(2)) {
		}

		if (new Float(1) == new Float(2)) {
		}

		if (new Double(1) == new Double(2)) {
		}

		if (new Boolean(true) == new Boolean(false)) {
		}
	}

	public void testActualPrimitivesShouldNotRefactor() {
		if (1 == 2) {
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
}
package eu.jsparrow.sample.postRule.allRules;

public class TestInlineLocalVariablesRule {

	void exampleWithThrow_shouldTransform() {
		final String msg = "Runtime Exception!";
		/* 1 */
		// 2
		/* 3 */
		// 4
		// 5
		// 6
		// 12
		/* 14 */
		throw // 13
		new /* 7 */ RuntimeException /* 8 */ (/* 9 */ msg // 10
		)/* 11 */; // 15
	}

	int exampleWithReturn_shouldTransform(int a, int b, int c, int d) {
		/* 1 */
		/* 2 */
		/*
		 * 17
		 */
		/* 19 */
		/* 20 */
		/* 18 */
		return /* 3 */ (/* 4 */ a /* 5 */ + /* 6 */ b/* 7 */) // 8
				// 9
				/* 10 */ * /* 11 */ (// 12
				c /* 13 */ - /* 14 */ d/* 15 */)/*
												 * 16
												 */ ; /* 21 */
	}

	int lineCommentAfterInitializer_shouldNotTransform() {
		final int x = 1 // comment
		;
		return x;
	}
}

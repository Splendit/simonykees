package eu.jsparrow.sample.preRule;

public class TestInlineLocalVariablesRule {

	void exampleWithThrow_shouldTransform() {
		String msg = "Runtime Exception!";
		/* 1 */
		// 2
		RuntimeException /* 3 */ // 4
		x // 5
				= // 6
				new /* 7 */ RuntimeException /* 8 */ (/* 9 */ msg // 10
				)/* 11 */ ; // 12
		throw // 13
		x /* 14 */; // 15
	}

	int exampleWithReturn_shouldTransform(int a, int b, int c, int d) {
		/* 1 */
		int /* 2 */ x = /* 3 */ (/* 4 */ a /* 5 */ + /* 6 */ b/* 7 */) // 8
				// 9
				/* 10 */ * /* 11 */ (// 12
				c /* 13 */ - /* 14 */ d/* 15 */)/*
												 * 16
												 */
		;/*
			 * 17
			 */
		/* 18 */
		return /* 19 */ x /* 20 */ ; /* 21 */
	}

	int lineCommentAfterInitializer_shouldNotTransform() {
		int x = 1 // comment
		;
		return x;
	}
}

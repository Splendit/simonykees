package eu.jsparrow.sample.postRule.allRules;

import java.util.Arrays;
import java.util.List;

public class TestInlineLocalVariablesRule {
	List<String> result;

	void exampleWithAssignment() {
		/* 1 */
		// 2
		final List<String> /* 3 */
		// 4
		x /* 5 */
				=
				// 6
				/* 7 */ Arrays/* 8 */ ./* 9 */ asList(/* 10 */ "item-1"/* 11 */ ,
						// 12
						/* 13 */ "item-2"/* 14 */)/* 15 */;/* 16 */
		// 17
		result /* 18 */ = /* 19 */ x /* 20 */; // 21
	}

	int exampleWithReturn(int a, int b, int c, int d) {
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
				/* 10 */ * /* 11 */ (/* 12 */ c
						/* 13 */ - /* 14 */ d/* 15 */)/*
														 * 16
														 */ ; /* 21 */
	}

	int lineCommentAfterInitializer_shouldNotTransform() {
		final int x = 1 // comment
		;
		return x;
	}

}

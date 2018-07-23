package eu.jsparrow.sample.postRule.allRules;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class OptionalIfPresentRule {

	private static final Logger logger = LoggerFactory.getLogger(OptionalIfPresentRule.class);
	private final String value2 = "";

	{
		Optional<String> input = Optional.empty();
		input.ifPresent(logger::info);
	}

	public void default_comment(Optional<String> input) {
		// comment after value initialization
		input.ifPresent(logger::info);
	}

	public void default_comment1(Optional<String> input) {
		// comment after isPresent
		input.ifPresent(logger::info);
	}

	public void default_comment2(Optional<String> input) {
		// comment above isPresent
		input.ifPresent(logger::info);
	}

	public void default_comment3(Optional<String> input) {
		// comment unconnected

		input.ifPresent(logger::info);
	}

	public void default_comment4(Optional<String> input) {
		// comment under isPresent
		input.ifPresent(logger::info);
	}

	public void default_comment5(Optional<String> input) {
		// comment under value initialization
		input.ifPresent(logger::info);
	}

	public void default_comment6(Optional<String> input) {
		// comment at the end of isPresent block
		input.ifPresent(logger::info);
	}

	public void singleIfBlockBody_shouldTransform(Optional<String> input) {
		// comment before isPresent
		input.ifPresent(value -> { // comment after isPresent
			// comment under isPresent
			// comment under value initialization
			if (true) {
				// comment inside inner if
				logger.info(value);
			}
			// comment at the end of isPresent block
		});
	}

	public void singleIfBlockBody_savingComments_shouldTransform(Optional<String> input) {
		// leading comment
		/* 1 */
		/* 2 */
		/* 4 */
		/* 5 */
		/* 6 */
		/* 7 */
		/* 10 */
		/* 11 */
		/* 12 */
		/* 13 */
		/* 14 */
		/* 15 */
		/* 16 */
		/* 17 */
		input /* 3 */.ifPresent(value -> /* 8 */ {/* 9.1 */
			/* 9.2 */
			/* 18 */
			/* 19 */
			if /* 20 */ ( /* 21 */ true /* 22 */) /* 23 */ { /* 24 */
				/* 25 */
				logger.info(value);/* 28 */
				/* 29 */
			} /* 30 */

			/* 31 */
		}/* 32 */
		/* 33 */);
	}

	public void defaultUseCase_savingComments_shouldTransform(Optional<String> input) {
		/*
		 * Comment 25 is still being lost...
		 */

		/* 21 */
		/* 22 */
		/* 1 */
		/* 2 */
		/* 3 */
		/* 4 */
		/* 5 */
		/* 6 */
		/* 7 */
		/* 8 */
		/* 9 */
		/* 10 */
		/* 11 */
		/* 12 */
		/* 13 */
		/* 14 */
		/* 15 */
		/* 16 */
		/* 17 */
		/* 18 */
		/* 19 */
		/* 20 */
		/* 23 */
		/* 24 */
		/* 26 */
		input/* 4 */.ifPresent(logger::info);
	}

	public void nestedOptionalIsPresent_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			logger.info(value);
			input.ifPresent(logger::info);
		});
	}

	public void singleIfStatementBody_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			if (true) {
				logger.info(value);
			}
		});
	}

	public void multipleInitialiyers_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			// comment under isPresent
			String second = "";
			logger.info(value);
			logger.info(second);
		});
	}

	public void getExpressionNotPresent_shouldNotTransform(Optional<String> input) {
		if (input.isPresent()) {
			String value = "";
			logger.info(value);
		}
	}

	public void getWithArgument_shouldNotTransform(Optional<String> input, List<String> users) {
		if (input.isPresent()) {
			String value = users.get(0);
			logger.info(value);
		}
	}

	public void getWithNullExpression_shouldNotTransform(Optional<String> input) {
		if (input.isPresent()) {
			// comment under isPresent
			String value = "";
			get();
			logger.info(value);
		}
	}

	public void throwingCheckedException_shouldNotTransform(Optional<String> input) throws Exception {
		// comment before isPresent
		if (input.isPresent()) {
			String value = input.get();
			logger.info(value);
			throwSomething();
		}
	}

	public void defaultUseCase_shouldTransform(Optional<String> input) {
		input.ifPresent(logger::info);
	}

	public void multipleBodyStatements_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			logger.info(value);
			logger.info("Print another value");
		});
	}

	public void missingDeclarationFragment_shouldTransform(Optional<String> input) {
		input.ifPresent(logger::info);
	}

	public void singleBodyStatement_shouldTransform(Optional<String> input) {
		input.ifPresent(logger::info);
		logger.info("I'm out!");
	}

	public void multipleGet_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			String value2 = value;
			logger.info(value);
			logger.info(value2);
			if (!StringUtils.isEmpty(value) && !StringUtils.isEmpty(value2)) {
				logger.info(value + value2);
			}
		});
	}

	public void multipleGetConflictingNames_shouldTransform(Optional<String> input, int i) {
		input.ifPresent(value1 -> {
			if (i == 0) {
				String value = value1;
				logger.info(value);
			} else {
				String value = value1;
				logger.info(value + i);
			}
		});
	}

	public void multipleOptionals_shouldTransform(Optional<String> input) {
		Optional<String> user = Optional.ofNullable("user-name");
		input.ifPresent(value -> {
			// this is a field access - should not be renamed
			logger.info(value2);
			// this is a local declaration - should be removed
			String value2 = value;
			String value3 = user.get();
			logger.info(value);
			logger.info(value2);
			if (!StringUtils.isEmpty(value) && !StringUtils.isEmpty(value2)) {
				logger.info(value + value2 + ":" + value3);
			}
		});
	}

	public void avoidExternalNameConflicts_shouldTransform() {
		String value = "I could crash with the lambda parameter";
		Optional<String> user = Optional.ofNullable(value);
		user.ifPresent(logger::info);
	}

	public void avoidInternalNameConflicts_shouldTransform() {
		Optional<String> user = Optional.ofNullable("John Snow");
		user.ifPresent(value1 -> {
			String value = "I could crash with the lambda parameter";
			logger.info(value + ":" + value1);
		});
	}

	public void avoidShadowingFields_shouldTransform() {
		Optional<String> user = Optional.ofNullable("John Snow");
		user.ifPresent(value -> {
			logger.info(value2 + ":" + value);
			String value2 = value;
			logger.info(value2);
		});
	}

	public void fakeOptional_shouldNotTransform(IoNonSonoOpzionale input) {
		if (input.isPresent()) {
			String value = input.get();
			logger.info(value);
		}
	}

	public void multipleConditions_shouldNotTransform(Optional<String> input) {
		boolean beTrue = true;
		if (input.isPresent() && beTrue) {
			String value = input.get();
			logger.info(value);
		}
	}

	public void nonEffectivelyFinalVariables_shouldNotTransform(Optional<String> input) {
		int i = 0;
		i++;
		if (input.isPresent()) {
			String value = input.get();
			logger.info(value + i);
		}
	}

	public void elseStatement_shouldNotTransform(Optional<String> input) {
		if (input.isPresent()) {
			String value = input.get();
			logger.info(value);
		} else {
			// do nothing
		}
	}

	public void returnStatementInBody_shouldNotTransform(Optional<String> input) {
		if (input.isPresent()) {
			String value = input.get();
			logger.info(value);
			return;
		}

		// Something here should not be done if the value is present
		logger.info("The value is not present");
	}

	public void breakStatementInBody_shouldNotTransform(List<String> users) {
		for (String user : users) {
			Optional<String> name = findUserName(user);
			if (name.isPresent()) {
				String value = name.get();
				logger.info(value);
				break;
			}
		}
	}

	public void continueStatementInBody_shouldNotTransform(List<String> users) {
		for (String user : users) {
			Optional<String> name = findUserName(user);
			if (name.isPresent()) {
				String value = name.get();
				logger.info(value);
				continue;
			}
		}
	}

	public void throwStatementInBody_shouldNotTransform(List<String> users) {
		for (String user : users) {
			Optional<String> name = findUserName(user);
			if (name.isPresent()) {
				String value = name.get();
				logger.info(value);
				if (StringUtils.isEmpty(value)) {
					throw new NoSuchElementException();
				} else {
					logger.info(value);
				}
			}
		}
	}

	public void clashingWithPropertyOnQualifiedName_shouldTransform(Optional<String> input) {
		final IoNonSonoOpzionale user = new IoNonSonoOpzionale();
		input.ifPresent(value1 -> {
			String value = value1;
			user.value.length();
			logger.info(value);
		});
	}

	public void discardedSingleOptionalGet_shouldNotTransform() {
		Optional<String> input = findUserName("");
		if (input.isPresent()) {
			input.get();
			String myVar = "somewar";
		}
	}

	public void discardedOptionalGet_shouldTransform() {
		Optional<String> input = findUserName("");
		input.ifPresent(myVar -> {
			input.get();
			findUserName(myVar);
		});
	}

	public void unusedAssignmentWithOptionalGet_shouldTransform() {
		Optional<String> input = findUserName("");
		input.ifPresent(myVar -> {
		});
	}

	private Optional<String> findUserName(String user) {
		return Optional.empty();
	}

	private void throwSomething() throws FileNotFoundException {

	}

	private void get() {

	}

	class IoNonSonoOpzionale {
		private final Logger logger1 = LoggerFactory.getLogger(IoNonSonoOpzionale.class);
		final public String value = "";

		public boolean isPresent() {
			return false;
		}

		public String get() {
			return "";
		}

		public void clashingWithFieldAccess_shouldTransform(Optional<String> input) {
			input.ifPresent(value1 -> {
				String value = value1;
				this.value.length();
				logger1.info(value);
			});
		}
	}

}

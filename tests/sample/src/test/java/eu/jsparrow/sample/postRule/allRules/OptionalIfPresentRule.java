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
		input.ifPresent(value -> logger.info(value));
	}

	public void singleIfBlockBody_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			if (true) {
				logger.info(value);
			}
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
			String value = "";
			get();
			logger.info(value);
		}
	}

	public void throwingCheckedException_shouldNotTransform(Optional<String> input) throws Exception {
		if (input.isPresent()) {
			String value = input.get();
			logger.info(value);
			throwSomething();
		}
	}

	public void defaultUseCase_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> logger.info(value));
	}

	public void multipleBodyStatements_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			logger.info(value);
			logger.info("Print another value");
		});
	}

	public void missingDeclarationFragment_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> logger.info(value));
	}

	public void singleBodyStatement_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> logger.info(value));
		logger.info("I'm out!");
	}

	public void multipleGet_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			logger.info(value);
			logger.info(value);
			if (!StringUtils.isEmpty(value) && !StringUtils.isEmpty(value)) {
				logger.info(value + value);
			}
		});
	}

	public void multipleOptionals_shouldTransform(Optional<String> input) {
		Optional<String> user = Optional.ofNullable("user-name");
		input.ifPresent(value -> {
			// this is a field access - should not be renamed
			logger.info(value2);
			// this is a local declaration - should be removed
			String value3 = user.get();
			logger.info(value);
			logger.info(value);
			if (!StringUtils.isEmpty(value) && !StringUtils.isEmpty(value)) {
				logger.info(value + value + ":" + value3);
			}
		});
	}

	public void avoidExternalNameConflicts_shouldTransform() {
		String value = "I could crash with the lambda parameter";
		Optional<String> user = Optional.ofNullable(value);
		user.ifPresent(value1 -> logger.info(value1));
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
			logger.info(value);
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
			user.value.length();
			logger.info(value1);
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
				this.value.length();
				logger1.info(value1);
			});
		}
	}

}

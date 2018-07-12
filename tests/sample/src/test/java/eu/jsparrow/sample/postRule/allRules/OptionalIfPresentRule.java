package eu.jsparrow.sample.postRule.allRules;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class OptionalIfPresentRule {

	private static final Logger logger = LoggerFactory.getLogger(OptionalIfPresentRule.class);

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
				}
			}
		}
	}

	private Optional<String> findUserName(String user) {
		return Optional.empty();
	}

	class IoNonSonoOpzionale {
		public boolean isPresent() {
			return false;
		}

		public String get() {
			return "";
		}
	}

}

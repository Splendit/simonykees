package eu.jsparrow.sample.postRule.optionalIfPresent;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@SuppressWarnings("nls")
public class OptionalIfPresentRule {

	private final String value2 = "";
	
	{
		Optional<String> input = Optional.empty();
		input.ifPresent(value -> System.out.println(value));
	}

	public void singleIfBlockBody_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			if (true) {
				System.out.println(value);
			}
		});
	}
	
	public void singleIfStatementBody_shouldTransform2(Optional<String> input) {
		input.ifPresent(value -> {
			if (true) {
				System.out.println(value);
			}
		});
	}

	public void multipleInitialiyers_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			String second = "";
			System.out.println(value);
			System.out.println(second);
		});
	}

	public void getExpressionNotPresent_shouldNotTransform(Optional<String> input) {
		if (input.isPresent()) {
			String value = "";
			System.out.println(value);
		}
	}

	public void getWithArgument_shouldNotTransform(Optional<String> input, List<String> users) {
		if (input.isPresent()) {
			String value = users.get(0);
			System.out.println(value);
		}
	}

	public void getWithNullExpression_shouldNotTransform(Optional<String> input) {
		if (input.isPresent()) {
			String value = "";
			get();
			System.out.println(value);
		}
	}

	public void throwingCheckedException_shouldNotTransform(Optional<String> input) throws Exception {
		if (input.isPresent()) {
			String value = input.get();
			System.out.println(value);
			throwSomething();
		}
	}

	public void defaultUseCase_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> System.out.println(value));
	}

	public void multipleBodyStatements_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			System.out.println(value);
			System.out.println("Print another value");
		});
	}

	public void missingDeclarationFragment_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> System.out.println(value));
	}

	public void singleBodyStatement_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> System.out.println(value));
		System.out.println("I'm out!");
	}

	public void multipleGet_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			System.out.println(value);
			System.out.println(value);
			if (!value.isEmpty() && !value.isEmpty()) {
				System.out.println(value + value);
			}
		});
	}

	public void multipleOptionals_shouldTransform(Optional<String> input) {
		Optional<String> user = Optional.ofNullable("user-name");
		input.ifPresent(value -> {
			// this is a field access - should not be renamed
			System.out.println(value2);
			// this is a local declaration - should be removed
			String value3 = user.get();
			System.out.println(value);
			System.out.println(value);
			if (!value.isEmpty() && !value.isEmpty()) {
				System.out.println(value + value + ":" + value3);
			}
		});
	}

	public void avoidExternalNameConflicts_shouldTransform() {
		String value = "I could crash with the lambda parameter";
		Optional<String> user = Optional.ofNullable(value);
		user.ifPresent(value1 -> System.out.println(value1));
	}

	public void avoidInternalNameConflicts_shouldTransform() {
		Optional<String> user = Optional.ofNullable("John Snow");
		user.ifPresent(value1 -> {
			String value = "I could crash with the lambda parameter";
			System.out.println(value + ":" + value1);
		});
	}

	public void avoidShadowingFields_shouldTransform() {
		Optional<String> user = Optional.ofNullable("John Snow");
		user.ifPresent(value2 -> {
			System.out.println(value2 + ":" + value2);
			System.out.println(value2);
		});
	}

	public void fakeOptional_shouldNotTransform(IoNonSonoOpzionale input) {
		if (input.isPresent()) {
			String value = input.get();
			System.out.println(value);
		}
	}

	public void multipleConditions_shouldNotTransform(Optional<String> input) {
		boolean beTrue = true;
		if (input.isPresent() && beTrue) {
			String value = input.get();
			System.out.println(value);
		}
	}

	public void nonEffectivelyFinalVariables_shouldNotTransform(Optional<String> input) {
		int i = 0;
		i++;
		if (input.isPresent()) {
			String value = input.get();
			System.out.println(value + i);
		}
	}

	public void elseStatement_shouldNotTransform(Optional<String> input) {
		if (input.isPresent()) {
			String value = input.get();
			System.out.println(value);
		} else {
			// do nothing
		}
	}

	public void returnStatementInBody_shouldNotTransform(Optional<String> input) {
		if (input.isPresent()) {
			String value = input.get();
			System.out.println(value);
			return;
		}

		// Something here should not be done if the value is present
		System.out.println("The value is not present");
	}

	public void breakStatementInBody_shouldNotTransform(List<String> users) {
		for (String user : users) {
			Optional<String> name = findUserName(user);
			if (name.isPresent()) {
				String value = name.get();
				System.out.println(value);
				break;
			}
		}
	}

	public void continueStatementInBody_shouldNotTransform(List<String> users) {
		for (String user : users) {
			Optional<String> name = findUserName(user);
			if (name.isPresent()) {
				String value = name.get();
				System.out.println(value);
				continue;
			}
		}
	}

	public void throwStatementInBody_shouldNotTransform(List<String> users) {
		for (String user : users) {
			Optional<String> name = findUserName(user);
			if (name.isPresent()) {
				String value = name.get();
				System.out.println(value);
				if (value.isEmpty()) {
					throw new NoSuchElementException();
				} else {
					System.out.println(value);
				}
			}
		}
	}

	public void clashingWithPropertyOnQualifiedName_shouldTransform(Optional<String> input) {
		final IoNonSonoOpzionale user = new IoNonSonoOpzionale();
		input.ifPresent(value -> {
			user.value.length();
			System.out.println(value);
		});
	}

	final String field = "";

	public void clashingWithFieldAccess_shouldTransform(Optional<String> input) {
		input.ifPresent(field -> {
			this.field.length();
			System.out.println(field);
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
		final public String value = "";

		public boolean isPresent() {
			return false;
		}

		public String get() {
			return "";
		}
	}

}

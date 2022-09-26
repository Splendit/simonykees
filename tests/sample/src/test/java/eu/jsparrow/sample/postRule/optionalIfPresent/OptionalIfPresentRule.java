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

	public void default_comment(Optional<String> input) {
		//comment after value initialization
		input.ifPresent(value -> System.out.println(value)); 
	}
	
	public void default_comment1(Optional<String> input) {
		// comment after isPresent
		input.ifPresent(value -> System.out.println(value)); 
	}
	
	public void default_comment2(Optional<String> input) {
		//comment above isPresent
		input.ifPresent(value -> System.out.println(value)); 
	}

	public void default_comment3(Optional<String> input) {
		//comment unconnected
		
		input.ifPresent(value -> System.out.println(value)); 
	}

	public void default_comment4(Optional<String> input) {
		//comment under isPresent
		input.ifPresent(value -> System.out.println(value)); 
	}
	
	public void default_comment5(Optional<String> input) {
		//comment under value initialization
		input.ifPresent(value -> System.out.println(value)); 
	}
	
	public void default_comment6(Optional<String> input) {
		//comment at the end of isPresent block
		input.ifPresent(value -> System.out.println(value)); 
	}

	public void singleIfBlockBody_shouldTransform(Optional<String> input) {
		// comment before isPresent
		input.ifPresent(value -> { // comment after isPresent
			// comment under isPresent
			// comment under value initialization
			if (true) {
				// comment inside inner if
				System.out.println(value);
			}
			// comment at the end of isPresent block
		});
	}
	
	public void singleIfBlockBody_savingComments_shouldTransform(Optional<String> input) {
		// leading comment
		/* 1 */
		/* 2 */
		/* 4 */
		/*  5 */
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
			/*9.2  */
			/* 18 */
			/* 19 */
			if /* 20 */ ( /* 21 */ true /* 22 */) /* 23 */ { /* 24 */
				/* 25 */
				System/* 26 */.out./* 27 */println(value);/* 28 */
				/* 29 */
			}/* 30 */
			
			/* 31 */
		}/* 32 */
		/* 33 */);
	}
	
	public void defaultUseCase_savingComments_shouldTransform(Optional<String> input) {
		/*
		 * Comment 25 is still being lost...
		 */
		
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
		input/* 4 */.ifPresent(value -> System.out.println(/* 21 */value/* 22 */));
	}
	
	public void nestedOptionalIsPresent_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			System.out.println(value);
			input.ifPresent(value2 -> System.out.println(value2));
		}); 
	}

	public void singleIfStatementBody_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			if (true) {
				System.out.println(value);
			}
		});
	}

	public void multipleInitialiyers_shouldTransform(Optional<String> input) {
		input.ifPresent(value -> {
			// comment under isPresent
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
			// comment under isPresent
			String value = "";
			get();
			System.out.println(value);
		}
	}

	public void throwingCheckedException_shouldNotTransform(Optional<String> input) throws Exception {
		// comment before isPresent
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
			String value2 = value;
			System.out.println(value);
			System.out.println(value2);
			if (!value.isEmpty() && !value2.isEmpty()) {
				System.out.println(value + value2);
			}
		});
	}
	
	public void multipleGetConflictingNames_shouldTransform(Optional<String> input, int i) {
		input.ifPresent(value1 -> {
			if(i == 0) {
				String value = value1;
				System.out.println(value);
			} else {
				String value = value1;
				System.out.println(value + i);
			}
		});
	}

	public void multipleOptionals_shouldTransform(Optional<String> input) {
		Optional<String> user = Optional.ofNullable("user-name");
		input.ifPresent(value -> {
			// this is a field access - should not be renamed
			System.out.println(value2);
			// this is a local declaration - should be removed
			String value2 = value;
			String value3 = user.get();
			System.out.println(value);
			System.out.println(value2);
			if (!value.isEmpty() && !value2.isEmpty()) {
				System.out.println(value + value2 + ":" + value3);
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
		user.ifPresent(value -> {
			System.out.println(value2 + ":" + value);
			String value2 = value;
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
	
	public void throwExceptionInBody_shouldNotTransform(List<String> users) throws Exception {
		for (String user : users) {
			Optional<String> name = findUserName(user);
			if (name.isPresent()) {
				String value = name.get();
				System.out.println(value);
				if (value.isEmpty()) {
					throw new Exception();
				} else {
					System.out.println(value);
				}
			}
		}
	}

	public void throwNoSuchElementExceptionInBody_shouldTransform(List<String> users) {
		for (String user : users) {
			Optional<String> name = findUserName(user);
			name.ifPresent(value -> {
				System.out.println(value);
				if (value.isEmpty()) {
					throw new NoSuchElementException();
				} else {
					System.out.println(value);
				}
			});
		}
	}

	public void clashingWithPropertyOnQualifiedName_shouldTransform(Optional<String> input) {
		final IoNonSonoOpzionale user = new IoNonSonoOpzionale();
		input.ifPresent(value1 -> {
			String value = value1;
			user.value.length();
			System.out.println(value);
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
	
	public void internalNonFinalVariables_shouldTransform() {
		Optional<String> input = findUserName("");
		input.ifPresent(value -> {
			  for (int i = 0; i < 15; i++) {
			    if (true) {
			      System.out.println("Test");
			    } else {
			      System.out.println("Test");
			    }
			  }
			});
	}

	String nonConstant = "";

	private void setNonConstant(String value) {
		nonConstant = value;
	}

	public void usingFieldsInIfPresent() {
		Optional<String> input = findUserName("");
		input.ifPresent(value -> System.out.println(value + nonConstant));
	}

	private Optional<String> findUserName(String user) {
		Optional.of(user);
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
		
		public void clashingWithFieldAccess_shouldTransform(Optional<String> input) {
			input.ifPresent(value1 -> {
				String value = value1;
				this.value.length();
				System.out.println(value);
			});
		}
	}

}

package eu.jsparrow.sample.preRule;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@SuppressWarnings("nls")
public class OptionalIfPresentRule {
	
	public void defaultUseCase_shouldTransform(Optional<String> input) {
		if (input.isPresent()) {
			String value = input.get();
			System.out.println(value);
		}
	}
	
	public void multipleBodyStatements_shouldTransform(Optional<String> input) {
		if (input.isPresent()) {
			String value = input.get();
			System.out.println(value);
			System.out.println("Print another value");
		}
	}
	
	public void missingDeclarationFragment_shouldTransform(Optional<String> input) {
		if (input.isPresent()) {
			System.out.println(input.get());
		}
	}
	
	public void singleBodyStatement_shouldTransform(Optional<String> input) {
		if (input.isPresent()) 
			System.out.println(input.get());
		System.out.println("I'm out!");
	}
	
	public void fakeOptional_shouldNotTransform(IoNonSonoOpzionale input) {
		if(input.isPresent()) {
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
		for(String user : users) {
			Optional<String> name = findUserName(user);
			if (name.isPresent()) {
				String value = name.get();
				System.out.println(value);
				break;
			}
		}
	}
	
	public void continueStatementInBody_shouldNotTransform(List<String> users) {
		for(String user : users) {
			Optional<String> name = findUserName(user);
			if (name.isPresent()) {
				String value = name.get();
				System.out.println(value);
				continue;
			}
		}
	}
	
	public void throwStatementInBody_shouldNotTransform(List<String> users) {
		for(String user : users) {
			Optional<String> name = findUserName(user);
			if (name.isPresent()) {
				String value = name.get();
				System.out.println(value);
				if(value.isEmpty()) {
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

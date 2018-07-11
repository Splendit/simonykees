package eu.jsparrow.sample.postRule.optionalIfPresent;

import java.util.Optional;

public class OptionalIfPresentRule {

	public void defaultUseCase(Optional<String> input) {
		input.ifPresent(value -> System.out.println(value));
	}

	public void skipWhenMultipleExpressionsInIf(Optional<String> input) {
		boolean beTrue = true;
		if (input.isPresent() && beTrue) {
			String value = input.get();
			System.out.println(value);
		}
	}
	
	public void skipWhenIfHasElseStatement(Optional<String> input) {
		if (input.isPresent()) {
			String value = input.get();
			System.out.println(value);
		} else {
			// do nothing
		}
	}
	
}

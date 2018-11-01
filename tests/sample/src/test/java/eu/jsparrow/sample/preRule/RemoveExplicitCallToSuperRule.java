package eu.jsparrow.sample.preRule;

public class RemoveExplicitCallToSuperRule {

	@SuppressWarnings("unused")
	public RemoveExplicitCallToSuperRule() {
		// comment above super()
		super(); // comment in-line with super()
		// comment under super()
		int i = 0;
		i++;
	}
}

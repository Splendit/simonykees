package eu.jsparrow.sample.postRule.removeExplicitCallToSuper;

@SuppressWarnings("unused")
public class RemoveExplicitCallToSuperRule extends RemoveExplicitCallToSuperRuleParent {

	public RemoveExplicitCallToSuperRule() {
		// comment above super()
		/* nothing to put in */
		/* another comment to save */
		// comment in-line with super()
		// comment under super()
		int i = 0;
		i++;
	}
	
	public RemoveExplicitCallToSuperRule(String argumentFirst) {
		super(argumentFirst/* nothing to put in */) /* another comment to save */; // comment in-line with super()
		// comment under super()
		int i = 0;
		i++;
	}
	
	public RemoveExplicitCallToSuperRule(String argumentFirst, String argumentSecond) {
		// comment above super()
		/* nothing to put in */
		/* another comment to save */
		// comment in-line with super()
		// comment under super()
		int i = 0;
		i++;
	}
	
	@Override
	public void regularMethod () {
		super.regularMethod();
		
	}
	
}

class RemoveExplicitCallToSuperRuleParent {
	public RemoveExplicitCallToSuperRuleParent() {
		// nothing to do
	}
	
	RemoveExplicitCallToSuperRuleParent(String stringArgument) {
		// nothing to do
	}
	
	public void regularMethod () {
	}
}
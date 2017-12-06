package eu.jsparrow.sample.postRule.publicFieldRenaming.renaming;

public class PublicFieldsRenamingRule2 {

	public void directAccessOfFieldFromExternalCu() {
		PublicFieldsRenamingRule rule = new PublicFieldsRenamingRule();
		rule.aPublicFieldSample = "";
		rule.referencedOnOtherClasses = "";
	}
	
	public void avoidAnonymousClasses() {
		Foo foo = new Foo() {
			
			public String foo_field;
			
			@Override
			public void foo() {
				this.foo_field = "";
			}
		};
	}
	
	abstract class Foo {
		public abstract void foo();
	}
}

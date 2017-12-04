package eu.jsparrow.sample.preRule.renaming;

public class PublicFieldsRenamingRule2 {

	public void directAccessOfFieldFromExternalCu() {
		PublicFieldsRenamingRule rule = new PublicFieldsRenamingRule();
		rule.a_public_field_sample = "";
		rule.referenced_on_other_classes = "";
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

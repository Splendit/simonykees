package eu.jsparrow.sample.preRule.renaming;

public class PublicFieldsRenamingRule2 {

	public void directAccessOfFieldFromExternalCu() {
		PublicFieldsRenamingRule rule = new PublicFieldsRenamingRule();
		rule.a_public_field_sample = "";
		rule.referenced_on_other_classes = "";
	}
}

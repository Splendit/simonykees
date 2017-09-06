package at.splendit.simonykees.sample.postRule.publicFieldRenaming.renaming;

public class PublicFieldsRenamingRule2 {

	public void directAccessOfFieldFromExternalCu() {
		PublicFieldsRenamingRule rule = new PublicFieldsRenamingRule();
		rule.aPublicFieldSample = "";
	}
}

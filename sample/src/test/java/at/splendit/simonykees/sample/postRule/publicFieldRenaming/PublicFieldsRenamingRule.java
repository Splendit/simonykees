package at.splendit.simonykees.sample.postRule.publicFieldRenaming;

@SuppressWarnings("nls")
public class PublicFieldsRenamingRule {
	
	public String aPublicFieldSample = "bad name";
	
	public String usePublicFieldSomewhere(String input) {
		return aPublicFieldSample + input;
	}
}

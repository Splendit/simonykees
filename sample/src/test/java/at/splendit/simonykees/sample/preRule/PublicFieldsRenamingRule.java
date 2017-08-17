package at.splendit.simonykees.sample.preRule;

@SuppressWarnings("nls")
public class PublicFieldsRenamingRule {
	
	public String a_public_field_sample = "bad name";
	
	public String usePublicFieldSomewhere(String input) {
		return a_public_field_sample + input;
	}
}

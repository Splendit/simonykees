package at.splendit.simonykees.sample.preRule.renaming;

public interface InterfaceHavingDefaultMethod {
	
	String VALUE = "value"; //$NON-NLS-1$
	
	default boolean referencingPublicFields(String value) {
		PublicFieldsRenamingRule r = new PublicFieldsRenamingRule();
		return VALUE.equals(r.remove$dollar$sign);
	}
}

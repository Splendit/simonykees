package at.splendit.simonykees.sample.postRule.publicFieldRenaming.renaming;

public interface InterfaceHavingDefaultMethod {
	
	String VALUE = "value"; //$NON-NLS-1$
	
	default boolean referencingPublicFields(String value) {
		PublicFieldsRenamingRule r = new PublicFieldsRenamingRule();
		return VALUE.equals(r.removeDollarSign);
	}
}

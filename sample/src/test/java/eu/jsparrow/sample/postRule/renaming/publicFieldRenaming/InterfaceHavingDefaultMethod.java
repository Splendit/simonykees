package eu.jsparrow.sample.postRule.renaming.publicFieldRenaming;

public interface InterfaceHavingDefaultMethod {
	
	String VALUE = "value"; //$NON-NLS-1$
	
	default boolean referencingPublicFields(String value) {
		PublicFieldsRenamingRule r = new PublicFieldsRenamingRule();
		return VALUE.equals(r.removeDollarSign);
	}
}

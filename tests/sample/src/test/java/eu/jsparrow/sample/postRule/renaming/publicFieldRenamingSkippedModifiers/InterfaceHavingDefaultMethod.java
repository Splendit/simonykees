package eu.jsparrow.sample.postRule.renaming.publicFieldRenamingSkippedModifiers;

public interface InterfaceHavingDefaultMethod {
	
	String VALUE = "value"; //$NON-NLS-1$
	
	default boolean referencingPublicFields(String value) {
		PublicFieldsRenamingRule r = new PublicFieldsRenamingRule();
		return VALUE.equals(r.removeDollarSign);
	}
}

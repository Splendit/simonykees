package eu.jsparrow.sample.postRule.renaming.publicFieldRenamingSkippedModifiers;

@SuppressWarnings("nls")
public class ClassWithUnreferencedFields {
	String FIELD = "toBeRenamedTo field";
	public String privateField = "toBeRenamedTo privateField";
	public static String staticField = "toBeRenamedTo staticField";
}

package at.splendit.simonykees.sample.postRule.publicFieldRenaming.renaming;

@SuppressWarnings("nls")
public class ClassWithUnreferencedFields {
	String field = "toBeRenamedTo field";
	public String privateField = "toBeRenamedTo privateField";
	public static String staticField = "toBeRenamedTo staticField";
}

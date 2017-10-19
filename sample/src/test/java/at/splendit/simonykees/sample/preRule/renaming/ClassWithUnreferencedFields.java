package at.splendit.simonykees.sample.preRule.renaming;

@SuppressWarnings("nls")
public class ClassWithUnreferencedFields {
	String FIELD = "toBeRenamedTo field";
	public String private_field = "toBeRenamedTo privateField";
	public static String Static_field = "toBeRenamedTo staticField";
}

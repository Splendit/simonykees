package at.splendit.simonykees.sample.postRule.allRules;

@SuppressWarnings("nls")
public class RearrangeClassMembersPartiallyArrangedRule {

	private static final String FIELD_1 = "field_1";

	// unbounded comment

	// second field
	public static final String FIELD_2 = "field_1";

	// to be moved in the top
	private String privateField;

	public RearrangeClassMembersPartiallyArrangedRule() {
		privateMethod();
		privateField = FIELD_1;
	}

	/**
	 * To be moved down
	 */
	private void privateMethod() {
		privateField = "";
	}

	public String getVal() {
		return privateField;
	}

	// another unbounded comment

}

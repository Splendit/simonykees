package eu.jsparrow.ui.dialog;

@SuppressWarnings("nls")
public enum JSparrowPricingLinkText {
	ADDED_LOCKED_RULES_TO_SELECTION(
			""
					+ "You have added one or more premium rules to your selection"
					+ " which cannot be applied because thy are locked (see the lock symbol)."
					+ "\r\n"
					+ "\r\n"
					+ "To unlock them, <a>visit jSparrow</a> to obtain a premium license, enter the license key and activate."),
	SELECTION_CONTAINS_LOCKED_RULES(
			""
					+ "Your selection contains one or more premium rules"
					+ " which cannot be applied because thy are locked (see the lock symbol)."
					+ "\r\n"
					+ "\r\n"
					+ "To unlock them, <a>visit jSparrow</a> to obtain a premium license, enter the license key and activate."),
	CANNOT_COMMIT_WITH_LOCKED_RULES(
			""
					+ "You cannot commit because your changes need the execution of premium rules"
					+ " which cannot be applied because thy are locked."
					+ "\r\n"
					+ "\r\n"
					+ "If you want to commit your changes, <a>visit jSparrow</a> to obtain a premium license, enter the license key and activate."),
	OBTAIN_NEW_LICENSE(
			""
					+ "To obtain a new license, <a>visit jSparrow</a>, enter the license key and activate.");

	private final String explainingText;

	private JSparrowPricingLinkText(String explainingText) {
		this.explainingText = explainingText;
	}

	public String getExplainingText() {
		return explainingText;
	}
}
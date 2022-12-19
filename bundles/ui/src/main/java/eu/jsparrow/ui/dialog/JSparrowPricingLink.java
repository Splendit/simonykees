package eu.jsparrow.ui.dialog;

@SuppressWarnings("nls")
public enum JSparrowPricingLink {
	UPGRADE_YOUR_LICENSE_TO_BE_ABLE_TO_APPLY_ALL_OUR_RULES(
			"",
			"Upgrade your license",
			" to be able to apply all our rules!"),
	TO_UNLOCK_ALL_OUR_RULES_REGISTER_FOR_A_PREMIUM_LICENSE(
			"To unlock all our rules, ",
			"register for a premium license",
			"."),
	TO_UNLOCK_THEM_REGISTER_FOR_A_PREMIUM_LICENSE(
			"To unlock them, ",
			"register for a premium license",
			"."),
	TO_UNLOCK_PREMIUM_RULES_UPGRADE_YOUR_LICENSE(
			"To unlock premium rules, ",
			"upgrade your license",
			"."),
	OBTAIN_NEW_LICENSE(
			"",
			"Obtain a new license",
			"."),
	TO_GET_FULL_ACCESS_AND_UNLOCK_ALL_RULES_UPGRADE_YOUR_LICENSE(
			"To get full access and unlock all the rules, ",
			"upgrade your license",
			"."),
	TO_BE_ABLE_TO_USE_JSPARROW_MARKERS_UPGRADE_HERE(
			"If you want to be able to use the jSparrow markers, ",
			"upgrade here",
			"."),
	UPGRADE_YOUR_LICENSE_HERE(
			"Upgrade your license ",
			"here",
			".");

	private static final String HTTPS_JSPARROW_IO_PRICING = "https://jsparrow.io/pricing/";
	private final String text;

	private JSparrowPricingLink(String textBeforeLink, String linkedText, String textAfterLink) {
		StringBuilder sb = new StringBuilder();
		sb.append(textBeforeLink);
		String linkStartTag = String.format("<a href=\"%s\">", getJSparrowPricingPageAddress());
		sb.append(linkStartTag);
		sb.append(linkedText);
		String linkEndTag = "</a>";
		sb.append(linkEndTag);
		sb.append(textAfterLink);
		this.text = sb.toString();
	}

	public static String getJSparrowPricingPageAddress() {
		return HTTPS_JSPARROW_IO_PRICING;
	}

	public String getText() {
		return text;
	}

}

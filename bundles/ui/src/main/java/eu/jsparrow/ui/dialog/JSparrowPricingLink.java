package eu.jsparrow.ui.dialog;

import eu.jsparrow.i18n.Messages;

/**
 * Needed to standardize the text for links to the jSparrpw pricing page.
 * 
 * @since 4.15.0
 * 
 */
public enum JSparrowPricingLink {
	ADDED_LOCKED_RULES_TO_SELECTION(JSparrowPricingLinkText.ADDED_LOCKED_RULES_TO_SELECTION, 80),
	SELECTION_CONTAINS_LOCKED_RULES(JSparrowPricingLinkText.SELECTION_CONTAINS_LOCKED_RULES, 80),
	CANNOT_COMMIT_WITH_LOCKED_RULES(JSparrowPricingLinkText.CANNOT_COMMIT_WITH_LOCKED_RULES, 80),
	UNLOCK_ALL_PREMIUM_RULES(JSparrowPricingLinkText.UNLOCK_ALL_PREMIUM_RULES, 30),
	OBTAIN_NEW_LICENSE(JSparrowPricingLinkText.OBTAIN_NEW_LICENSE, 30);

	private final String text;
	private final int minimumControlHeight;
	
	private JSparrowPricingLink(String template, int minimumControlHeight) {
		String linkStartTag = String.format(Messages.JSparrowPricingLink_link_startTag_formatstring,
				getJSparrowPricingPageAddress());
		this.text = template.replace(Messages.JSparrowPricingLink_link_startTag, linkStartTag);
		this.minimumControlHeight = minimumControlHeight;
	}

	public static String getJSparrowPricingPageAddress() {
		return Messages.JSparrowPricingLink_jSparrowPricingPage_url;
	}

	public String getText() {
		return text;
	}
	
	public int getMinimumControlHeight() {
		return minimumControlHeight;
	}
}

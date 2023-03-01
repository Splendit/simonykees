package eu.jsparrow.ui.dialog;

import eu.jsparrow.i18n.Messages;

/**
 * Needed to standardize the text for links to the jSparrpw pricing page.
 * 
 * @since 4.15.0
 * 
 */
public enum JSparrowPricingLink {
	TO_UNLOCK_PREMIUM_RULES_UPGRADE_LICENSE(Messages.JSparrowPricingLink_toUnlockPremiumRulesUpgradeLicense), // used!!
	OBTAIN_NEW_LICENSE(Messages.JSparrowPricingLink_obtainNewLicense),
	TO_GET_FULL_ACCESS_UPGRADE_LICENSE(Messages.JSparrowPricingLink_toGetFullAccessUpgradeLicense),
	TO_USE_JSPARROW_MARKERS_UPGRADE_HERE(Messages.JSparrowPricingLink_toUseJSparrowMarkersUpgradeHere);

	private final String text;

	private JSparrowPricingLink(String template) {
		String linkStartTag = String.format(Messages.JSparrowPricingLink_link_startTag_formatstring,
				getJSparrowPricingPageAddress());
		this.text = template.replace(Messages.JSparrowPricingLink_link_startTag, linkStartTag);
	}

	public static String getJSparrowPricingPageAddress() {
		return Messages.JSparrowPricingLink_jSparrowPricingPage_url;
	}

	public String getText() {
		return text;
	}

}

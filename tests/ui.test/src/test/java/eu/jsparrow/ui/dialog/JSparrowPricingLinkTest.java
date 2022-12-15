package eu.jsparrow.ui.dialog;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class JSparrowPricingLinkTest {

	private static final String LINK_ENDTAG = "</a>";
	private static final String LINK_TO_JSPARROW_IO_PRICING_STARTTAG = "<a href=\"https://jsparrow.io/pricing/\">";

	@Test
	void test_UPGRADE_YOUR_LICENSE_TO_BE_ABLE_TO_APPLY_ALL_OUR_RULES() {
		assertEquals(
				LINK_TO_JSPARROW_IO_PRICING_STARTTAG
						+ "Upgrade your license"
						+ LINK_ENDTAG
						+ " to be able to apply all our rules!",
				JSparrowPricingLink.UPGRADE_YOUR_LICENSE_TO_BE_ABLE_TO_APPLY_ALL_OUR_RULES.getText());
	}

	@Test
	void test_TO_UNLOCK_ALL_OUR_RULES_REGISTER_FOR_A_PREMIUM_LICENSE() {
		assertEquals(
				"To unlock all our rules, " +
						LINK_TO_JSPARROW_IO_PRICING_STARTTAG
						+ "register for a premium license"
						+ LINK_ENDTAG
						+ ".",
				JSparrowPricingLink.TO_UNLOCK_ALL_OUR_RULES_REGISTER_FOR_A_PREMIUM_LICENSE.getText());
	}

	@Test
	void test_TO_UNLOCK_THEM_REGISTER_FOR_A_PREMIUM_LICENSE() {
		assertEquals(
				"To unlock them, " +
						LINK_TO_JSPARROW_IO_PRICING_STARTTAG
						+ "register for a premium license"
						+ LINK_ENDTAG
						+ ".",
				JSparrowPricingLink.TO_UNLOCK_THEM_REGISTER_FOR_A_PREMIUM_LICENSE.getText());
	}

	@Test
	void test_TO_UNLOCK_PREMIUM_RULES_UPGRADE_YOUR_LICENSE() {
		assertEquals(
				"To unlock premium rules, " +
						LINK_TO_JSPARROW_IO_PRICING_STARTTAG
						+ "upgrade your license"
						+ LINK_ENDTAG
						+ ".",
				JSparrowPricingLink.TO_UNLOCK_PREMIUM_RULES_UPGRADE_YOUR_LICENSE.getText());
	}

}

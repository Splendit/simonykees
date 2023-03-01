package eu.jsparrow.ui.dialog;

import static org.junit.jupiter.api.Assertions.*;
import static eu.jsparrow.ui.dialog.JSparrowPricingLink.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("nls")
class JSparrowPricingLinkTest {

	private static final String LINK_ENDTAG = "</a>";
	private static final String LINK_TO_JSPARROW_IO_PRICING_STARTTAG = "<a href=\"https://jsparrow.io/pricing/\">";

	private int getSubstringCount(String string, String subString) {

		int count = 0;
		int substringIndex = string.indexOf(subString);
		while (substringIndex != -1) {
			count++;
			substringIndex = string.indexOf(subString, substringIndex + 1);
		}
		return count;
	}

	public static Stream<Arguments> testLinkTextContentArguments() throws Exception {
		return Stream.of(
				Arguments.of(
						"To unlock premium rules, ",
						"upgrade your license",
						".",
						TO_UNLOCK_PREMIUM_RULES_UPGRADE_LICENSE),
				Arguments.of(
						"",
						"Obtain a new license",
						".",
						OBTAIN_NEW_LICENSE),
				Arguments.of(
						"To get full access and unlock all our rules, ",
						"upgrade your license",
						".",
						TO_GET_FULL_ACCESS_UPGRADE_LICENSE),
				Arguments.of(
						"If you want to be able to use the jSparrow markers, ",
						"upgrade here",
						".",
						TO_USE_JSPARROW_MARKERS_UPGRADE_HERE));
	}

	@ParameterizedTest
	@MethodSource("testLinkTextContentArguments")
	void testLinkTextContent(String textBeforeLink, String linkedText, String textAfterLink,
			JSparrowPricingLink pricingLink) {
		assertEquals(textBeforeLink
				+ LINK_TO_JSPARROW_IO_PRICING_STARTTAG
				+ linkedText
				+ LINK_ENDTAG
				+ textAfterLink,
				pricingLink.getText());
	}

	@Test
	void testAllLinks_shouldContainLinkStartTagExactlyOnce() {
		assertTrue(
				Stream.of(JSparrowPricingLink.values())
					.map(JSparrowPricingLink::getText)
					.allMatch(link -> getSubstringCount(link, "<a") == 1));
	}

	@Test
	void testAllLinks_shouldContainLinkEndTagExactlyOnce() {
		assertTrue(
				Stream.of(JSparrowPricingLink.values())
					.map(JSparrowPricingLink::getText)
					.allMatch(link -> getSubstringCount(link, "</a>") == 1));
	}

	@Test
	void testAllLinks_shouldNotContainLT_CharacterOutsideLinkTRag() {
		assertTrue(
				Stream.of(JSparrowPricingLink.values())
					.map(JSparrowPricingLink::getText)
					.map(link -> link.replace(LINK_TO_JSPARROW_IO_PRICING_STARTTAG, ""))
					.map(link -> link.replace(LINK_ENDTAG, ""))
					.allMatch(link -> getSubstringCount(link, "<") == 0));
	}

	@Test
	void testAllLinks_shouldNotContainGT_CharacterOutsideLinkTRag() {
		assertTrue(
				Stream.of(JSparrowPricingLink.values())
					.map(JSparrowPricingLink::getText)
					.map(link -> link.replace(LINK_TO_JSPARROW_IO_PRICING_STARTTAG, ""))
					.map(link -> link.replace(LINK_ENDTAG, ""))
					.allMatch(link -> getSubstringCount(link, ">") == 0));
	}
}

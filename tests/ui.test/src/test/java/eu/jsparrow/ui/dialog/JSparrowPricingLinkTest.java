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
						"You have added one or more premium rules to your selection which cannot be applied because thy are locked (see the lock symbol).\r\n"
						+ "\r\n"
						+ "To unlock them, <a href=\"https://jsparrow.io/pricing/\">visit jSparrow</a> to obtain a premium license, enter the license key and activate.",
						ADDED_LOCKED_RULES_TO_SELECTION),
				Arguments.of(
						"Your selection contains one or more premium rules which cannot be applied because thy are locked (see the lock symbol).\r\n"
						+ "\r\n"
						+ "To unlock them, <a href=\"https://jsparrow.io/pricing/\">visit jSparrow</a> to obtain a premium license, enter the license key and activate.",
						SELECTION_CONTAINS_LOCKED_RULES),
				Arguments.of(
						"You cannot commit because your changes need the execution of premium rules which cannot be applied because thy are locked.\r\n"
						+ "\r\n"
						+ "If you want to commit your changes, <a href=\"https://jsparrow.io/pricing/\">visit jSparrow</a> to obtain a premium license, enter the license key and activate.",
						CANNOT_COMMIT_WITH_LOCKED_RULES),
				Arguments.of(
						"To obtain a new license, <a href=\"https://jsparrow.io/pricing/\">visit jSparrow</a>, enter the license key and activate.",
						OBTAIN_NEW_LICENSE));
	}

	@ParameterizedTest
	@MethodSource("testLinkTextContentArguments")
	void testLinkTextContent(String pricingLinkText, JSparrowPricingLink pricingLink) {
		assertEquals(pricingLinkText, pricingLink.getText());
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

package at.splendit.simonykees.sample.preRule;

import org.junit.Assert;
import org.junit.Test;

/**
 * This test is a manual test to provide tests for the StringUtils replacement
 * 
 * 
 * How to Test: Do unit Test -> All tests should pass. Apply the [Usage] on the
 * file. Test the file again -> All tests should still pass and all methods of
 * Strings should be replaced by the corresponding StrinUtils implementation.
 * 
 * Usage: [Right Click in Editor] -> [Simoneykees/SelectRuleWizardHandler] ->
 * [StringUtils auswählen] -> Finish This triggers the Event.
 * 
 * Event: All operations on a String should be replaced by the corresponding
 * StringUtils method.
 * 
 * @author mgh
 *
 */

public class StringUtilsRefactorTest {
	

	@Test
	public void testEmpty() {
		String testString = "";
		
		Assert.assertTrue("Test for empty failed",testString.isEmpty());
		
		testString = "notEmpty";
		
		Assert.assertFalse("Test for not empty failed",testString.isEmpty());
	}
	
	@Test
	public void testTrim() {
		String testString = "  trimMe  ";
		String expectedString = "trimMe";
		
		Assert.assertEquals("Test for trim failed",testString.trim(),expectedString);
	}
	
	@Test
	public void testEquals() {
		String testString = "equal";
		String expectedString = "equal";
		
		Assert.assertTrue("Test for equals failed",testString.equals(expectedString));
		
		testString = "notEqual";
		
		Assert.assertFalse("Test for false equals failed", testString.equals(expectedString));
	}
	
	@Test
	public void testEndsWith() {
		String testString = "endsWith";
		String expectedString = "With";
		
		Assert.assertTrue("Test for endsWith failed",testString.endsWith(expectedString));
		
		testString = "WithEnds";
		
		Assert.assertFalse("Test for false endsWith failed", testString.endsWith(expectedString));
	}
	
	@Test
	public void testStartWith() {
		String testString = "startWith";
		String expectedString = "start";
		
		Assert.assertTrue("Test for endsWith failed",testString.startsWith(expectedString));
		
		testString = "withstart";
		
		Assert.assertFalse("Test for false endsWith failed", testString.startsWith(expectedString));
	}
	
	@Test
	public void testIndexOf() {
		String testString = "alalelu";

		Assert.assertEquals("Test for indexOf failed",testString.indexOf("e"),4);
		Assert.assertEquals("Test for indexOf failed",testString.indexOf(""),0);
	}
	
	@Test
	public void testContains() {
		String testString = "contains";
		String expectedString = "tain";
		
		Assert.assertTrue("Test for contains failed",testString.contains(expectedString));
		
		testString = "conta";
		
		Assert.assertFalse("Test for false contains failed", testString.contains(expectedString));
	}
	
	@Test
	public void testReplace() {
		String testString = "replaceMe";
		String expectedString = "replaceme";
		
		Assert.assertEquals("Test for replace failed",testString.replace("M","m"),expectedString);
	}
	
	@Test
	public void testLowerCase() {
		String testString = "lowerCASE";
		String expectedString = "lowercase";
		
		Assert.assertEquals("Test for lowerCase failed",testString.toLowerCase(),expectedString);
	}
	
	@Test
	public void testUpperCase() {
		String testString = "UPPERcase";
		String expectedString = "UPPERCASE";
		
		Assert.assertEquals("Test for lowerCase failed",testString.toUpperCase(),expectedString);
	}
	
	@Test
	public void testSplit() {
		String testString = "please,dont,split,me";
		String[] expectedString = {"please","dont","split","me"};
		
		Assert.assertArrayEquals("Test for split String failed",testString.split(","),expectedString);
	}
}

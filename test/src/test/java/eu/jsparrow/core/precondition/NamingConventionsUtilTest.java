package eu.jsparrow.core.precondition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import eu.jsparrow.core.visitor.renaming.NamingConventionUtil;

/**
 * Testing the generation of new identifiers from existing 
 * ones containing underscores and dollar sings. 
 * 
 * @author Ardit Ymeri
 * @since 2.3.0
 *
 */
@SuppressWarnings("nls")
public class NamingConventionsUtilTest {
	
	@Test
	public void cammelCaseAfterDollar() {
		String identifierWithDollarSign = "has$dollar_sign";
		String newId = NamingConventionUtil.generateNewIdentifier(identifierWithDollarSign, true, false).orElse("");
		assertFalse("Expecting an identifier to be generated", newId.isEmpty());
		assertEquals("Expecting the generated id to have upper cases after dollar sign", "hasDollarsign", newId);
		
	}
	
	@Test
	public void cammelCaseAfterUnderscore() {
		String identifierWithDollarSign = "has_under$score";
		String newId = NamingConventionUtil.generateNewIdentifier(identifierWithDollarSign, false, true).orElse("");
		assertFalse("Expecting an identifier to be generated", newId.isEmpty());
		assertEquals("Expecting the generated id to have upper cases after '_'", "hasUnderscore", newId);
		
	}
	
	@Test
	public void cammelCaseAllways() {
		String identifierWithDollarSign = "has_under$score_and$dollar_sign";
		String newId = NamingConventionUtil.generateNewIdentifier(identifierWithDollarSign, true, true).orElse("");
		assertFalse("Expecting an identifier to be generated", newId.isEmpty());
		assertEquals("Expecting the generated id to have upper cases after '$' and '_'", "hasUnderScoreAndDollarSign", newId);
		
	}
	
	@Test
	public void complyingConvention_usingUnderscore() {
		assertFalse(NamingConventionUtil.isComplyingWithConventions("using_underscore"));
	}
	
	@Test
	public void complyingConvention_startWithUpperCase() {
		assertFalse(NamingConventionUtil.isComplyingWithConventions("StartingWithUppercase"));
	}
	
	@Test
	public void complyingConvention_usingDollarSign() {
		assertFalse(NamingConventionUtil.isComplyingWithConventions("using$DollarSign"));
	}
	
	@Test
	public void generateIdentifier() {
		String newIdentifier = NamingConventionUtil.generateNewIdentifier("using$DollarSign").orElse("");
		assertEquals("usingDollarSign", newIdentifier);
	}
	
	@Test
	public void generateIdentifier_avoidJavaKeywords() {
		assertFalse(NamingConventionUtil.generateNewIdentifier("Int").isPresent());
	}
	
	@Test
	public void generateIdentifier_avoidEmptyIdentifiers() {
		assertFalse(NamingConventionUtil.generateNewIdentifier("_").isPresent());
	}
	
	@Test
	public void generateIdentifier_avoidStartingWithDigit() {
		assertFalse(NamingConventionUtil.generateNewIdentifier("_1startingWithDigit").isPresent());
	}
}

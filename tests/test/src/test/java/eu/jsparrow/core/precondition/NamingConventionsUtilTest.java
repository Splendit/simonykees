package eu.jsparrow.core.precondition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

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
		assertFalse(newId.isEmpty(), "Expecting an identifier to be generated");
		assertEquals("hasDollarsign", newId, "Expecting the generated id to have upper cases after dollar sign");
		
	}
	
	@Test
	public void cammelCaseAfterUnderscore() {
		String identifierWithDollarSign = "has_under$score";
		String newId = NamingConventionUtil.generateNewIdentifier(identifierWithDollarSign, false, true).orElse("");
		assertFalse(newId.isEmpty(), "Expecting an identifier to be generated");
		assertEquals("hasUnderscore", newId, "Expecting the generated id to have upper cases after '_'");
		
	}
	
	@Test
	public void cammelCaseAllways() {
		String identifierWithDollarSign = "has_under$score_and$dollar_sign";
		String newId = NamingConventionUtil.generateNewIdentifier(identifierWithDollarSign, true, true).orElse("");
		assertFalse(newId.isEmpty(), "Expecting an identifier to be generated");
		assertEquals("hasUnderScoreAndDollarSign", newId,"Expecting the generated id to have upper cases after '$' and '_'");
		
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

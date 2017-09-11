package at.splendit.simonykees.core.precondition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import at.splendit.simonykees.core.visitor.renaming.NamingConventionUtil;

/**
 * Testing the generation of new identifiers from existing 
 * ones containing underscores and dollar sings. 
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
@SuppressWarnings("nls")
public class NamingConventionsUtilTest {
	
	@Test
	public void cammelCaseAfterDollar() {
		String identifierWithDollarSign = "has$dollar_sign";
		String newId = NamingConventionUtil.generateNewIdetifier(identifierWithDollarSign, true, false).orElse("");
		assertFalse("Expecting an identifier to be generated", newId.isEmpty());
		assertEquals("Expecting the generated id to have upper cases after dollar sign", "hasDollarsign", newId);
		
	}
	
	@Test
	public void cammelCaseAfterUnderscore() {
		String identifierWithDollarSign = "has_under$score";
		String newId = NamingConventionUtil.generateNewIdetifier(identifierWithDollarSign, false, true).orElse("");
		assertFalse("Expecting an identifier to be generated", newId.isEmpty());
		assertEquals("Expecting the generated id to have upper cases after '_'", "hasUnderscore", newId);
		
	}
	
	@Test
	public void cammelCaseAllways() {
		String identifierWithDollarSign = "has_under$score_and$dollar_sign";
		String newId = NamingConventionUtil.generateNewIdetifier(identifierWithDollarSign, true, true).orElse("");
		assertFalse("Expecting an identifier to be generated", newId.isEmpty());
		assertEquals("Expecting the generated id to have upper cases after '$' and '_'", "hasUnderScoreAndDollarSign", newId);
		
	}
}

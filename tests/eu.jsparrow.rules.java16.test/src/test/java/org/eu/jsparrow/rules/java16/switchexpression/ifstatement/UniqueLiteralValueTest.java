package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import eu.jsparrow.rules.java16.switchexpression.ifstatement.UniqueLiteralValueStore;

public class UniqueLiteralValueTest {

	@Test
	void testIsUniqueStringLiteral() {
		UniqueLiteralValueStore uniqueLiteralValues = new UniqueLiteralValueStore();
		assertTrue(uniqueLiteralValues.isUnique("5"));
		assertFalse(uniqueLiteralValues.isUnique("\65"));
		assertFalse(uniqueLiteralValues.isUnique("\u0035"));
	}

	@Test
	void testIsUniqueCharLiteral() {
		UniqueLiteralValueStore uniqueLiteralValues = new UniqueLiteralValueStore();

		assertTrue(uniqueLiteralValues.isUnique(Character.valueOf('5')));
		assertEquals('5', Character.valueOf('\65'));
		assertEquals('5', Character.valueOf('\u0035'));

		assertFalse(uniqueLiteralValues.isUnique(Character.valueOf('\65')));
		assertFalse(uniqueLiteralValues.isUnique(Character.valueOf('\u0035')));

		assertFalse(uniqueLiteralValues.isUnique("5"));
		assertFalse(uniqueLiteralValues.isUnique("\65"));
		assertFalse(uniqueLiteralValues.isUnique("\u0035"));
	}

	@Test
	void testIsUniqueIntLiteral() {
		UniqueLiteralValueStore uniqueLiteralValues = new UniqueLiteralValueStore();

		assertTrue(uniqueLiteralValues.isUnique(Integer.decode("16")));
		assertEquals(16, Integer.decode("0x10"));
		assertEquals(16, Integer.decode("020"));

		assertFalse(uniqueLiteralValues.isUnique(Integer.decode("0x10")));
		assertFalse(uniqueLiteralValues.isUnique(Integer.decode("020")));
		assertFalse(uniqueLiteralValues.isUnique("16"));

		assertTrue(uniqueLiteralValues.isUnique(Integer.decode("-16")));
		assertFalse(uniqueLiteralValues.isUnique("-16"));
	}

}

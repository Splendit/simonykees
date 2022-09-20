package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class which determines whether an expression found in the condition of
 * an if statement is unique or not. For example, in
 * 
 * <pre>
 * if (value == 16) {
 * 	// ...
 * } else if (value == 0x10) {
 * 	// ...
 * } else {
 * 	// ...
 * }
 * </pre>
 * 
 * the hexadecimal literal {@code 0x10} in the condition of the else if clause
 * is not any more unique because it represents the same value as the literal
 * {@code 16} in the condition of the if clause.
 * 
 * @since 4.13.0
 */
public class UniqueLiteralValueStore {

	private final List<String> uniqueLiterals = new ArrayList<>();

	public boolean isUnique(String literalValue) {
		if (uniqueLiterals.contains(literalValue)) {
			return false;
		}
		uniqueLiterals.add(literalValue);
		return true;
	}

	public boolean isUnique(Character literalValue) {
		String stringRepresentation = literalValue.toString();
		if (uniqueLiterals.contains(stringRepresentation)) {
			return false;
		}
		uniqueLiterals.add(stringRepresentation);
		return true;
	}

	public boolean isUnique(Integer literalValue) {
		String stringRepresentation = literalValue.toString();
		if (uniqueLiterals.contains(stringRepresentation)) {
			return false;
		}
		uniqueLiterals.add(stringRepresentation);
		return true;
	}
}

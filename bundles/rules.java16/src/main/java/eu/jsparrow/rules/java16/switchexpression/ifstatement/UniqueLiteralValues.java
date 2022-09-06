package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.ArrayList;
import java.util.List;

public class UniqueLiteralValues {

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

	public boolean isUnique(Long literalValue) {
		String stringRepresentation = literalValue.toString();
		if (uniqueLiterals.contains(stringRepresentation)) {
			return false;
		}
		uniqueLiterals.add(stringRepresentation);
		return true;
	}
}

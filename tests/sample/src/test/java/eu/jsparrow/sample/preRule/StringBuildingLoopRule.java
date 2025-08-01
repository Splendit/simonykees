package eu.jsparrow.sample.preRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"nls", "unused"})
public class StringBuildingLoopRule {
	
	String result = "";
	
	public String collectionOfStrings(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result // I don't want to break the result initializer
		 = "";
		for(String val : collectionOfStrings // I don't want to break anything
				) {
			// save me
			result = result + val;
		}
		return result;
	}
	
	public String missingCurlyBrackets(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "";
		for(String val : collectionOfStrings)
			result = result + val;
		
		return result;
	}
	
	public String statementsBetweenDecAndLoop(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "";
		String anotherDecl = "";
		if(collectionOfStrings.isEmpty()) {
			collectionOfStrings.add(anotherDecl);
		}
		for(String val : collectionOfStrings) {
			result = result + val;
		}
		return result;
	}
	
	public String multipleFragments(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "", anotherDecl = "";
		if(collectionOfStrings.isEmpty()) {
			collectionOfStrings.add(anotherDecl);
		}
		for(String val : collectionOfStrings) {
			result = result + val;
		}
		return result;
	}
	
	public String modifiedResultVariable(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "", anotherDecl = "";
		if(collectionOfStrings.isEmpty()) {
			collectionOfStrings.add(anotherDecl);
			result = "-";
		}
		for(String val : collectionOfStrings) {
			result = result + val;
		}
		return result;
	}
	
	public String nonemptyInitialization(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "-";
		for(String val : collectionOfStrings) {
			result = result + val;
		}
		return result;
	}
	
	public String parentBlockDeclaration(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		/*
		 * The result variable is not declared in the same block
		 * with the loop. 
		 */
		String result = "", anotherDecl = "";
		if(collectionOfStrings.isEmpty()) {
			collectionOfStrings.add(anotherDecl);

			for(String val : collectionOfStrings) {
				result = result + val;
			}
		}
		return result;
	}
	
	public String irrelevantDelaration(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		/*
		 * Another result variable is declared in a nested block
		 * which is not visible in the scope of the loop. 
		 */
		
		if(collectionOfStrings.isEmpty()) {
			String result = "-";
			collectionOfStrings.add(result);
		}
		String result = "";

		for(String val : collectionOfStrings) {
			result = result + val;
		}

		return result;
	}
	
	public String collectingToAField(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		/*
		 * The result variable is not declared in the same block
		 * with the loop. 
		 */
		String anotherDecl = "";

		collectionOfStrings.add(anotherDecl);

		for(String val : collectionOfStrings) {
			result = result + val;
		}

		return result;
	}
	
	public String plusEqualsOperator(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "";

		for(String val : collectionOfStrings) {
			result += val;
		}

		return result;
	}
	
	public String collectingArrayOfStrings(String input) {
		String[] arrayOfStrings = generateArray(input);
		String result = "";
		for(String val : arrayOfStrings) {
			result = result + val;
		}
		return result;
	}
	
	public String collectingArrayOfNumbers(String input) {
		Double[] arrayOfStrings = {2.1, 3.5};
		String result = "";
		for(Double val : arrayOfStrings) {
			result = result + val;
		}
		return result;
	}
	
	public String savingAnnotationOverArray(String input) {
		String[] arrayOfStrings = generateArray(input);
		@Deprecated
		String result = "";
		for(String val : arrayOfStrings) {
			result = result + val;
		}
		return result;
	}
	
	public String savingAnnotationsOverCollection(String input) {
		List<String> listOfStrings = generateStringList(input);
		@Deprecated
		String result = "";
		for(String val : listOfStrings) {
			result = result + val;
		}
		return result;
	}
	
	public String handleText(char[] txt, int position) {
		/*
		 * corner case in cc.mallet.pipe.CharSequenceRemoveHTML.java
		 * Using StringBuilder if conversion to stream is not possible
		 */
		String text = "";
		for (char aTxt : txt) {
			text += aTxt;
		}
		return text += "\n";
	}
	
	/*
	 * Testing the generated string builder name
	 */
	
	public String newStringBuilderName(String input) {
		String[] arrayOfStrings = generateArray(input);
		String result = "", resultSb = "";
		String resultSb1 = "";
		for(String val : arrayOfStrings) {
			result = result + val;
		}
		return result;
	}
	
	public String newStringBuilderName2(String input) {
		String[] arrayOfStrings = generateArray(input);
		String result = "", resultSb = "";
		if(resultSb.isEmpty()) {			
			String resultSb1 = "";
		}
		for(String val : arrayOfStrings) {
			result = result + val;
		}
		return result;
	}
	
	public String cascadedLoops(String input) {
		String[] arrayOfStrings = generateArray(input);
		List<String> listOfStrings = generateStringList(input);
		String result = "", resultSb = "";
		String resultSb1 = "";
		for(String val : arrayOfStrings) {
			result = result + val;
		}
		
		String anotherResult = "";
		for(String val : listOfStrings) {
			result += val;
		}
		
		return result + anotherResult;
	}
	
	public String clashWithParameterName(String resultSb) {
		String[] arrayOfStrings = generateArray(resultSb);
		List<String> listOfStrings = generateStringList(resultSb);
		String result = "";
		for(String val : arrayOfStrings) {
			result = result + val;
		}
		
		return result;
	}
	
	/*
	 * Using collect over stream of numbers
	 */
	
	public String colectionOfIntegers(String input) {
		List<Integer> collectionOfints = new ArrayList<>();
		String result = "";
		for(int val : collectionOfints) {
			result = result + val;
		}
		return result;
	}
	
	public String colectionOfDoubles(String input) {
		List<Double> collectionOfints = new ArrayList<>();
		String result = "";
		for(double val : collectionOfints) {
			result = result + val;
		}
		return result;
	}
	
	/*
	 * The following are negative test cases
	 */
	
	public String multipleStatementsInBody(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "";
		for(String val : collectionOfStrings) {
			result = result + val;
			if(val.isEmpty()) {
				result += "-";
			}
		}
		return result;
	}
	
	public String ignoringLoopVariable(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "", anotherVal = "-";
		for(String val : collectionOfStrings) {
			result = result + anotherVal;
		}
		return result;
	}
	
	public String ignoringTheCollectedResult(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "", anotherVal = "-";
		for(String val : collectionOfStrings) {
			result = anotherVal + val;
		}
		return result;
	}
	
	public String ignoringTheCollectedResult2(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "", anotherVal = "-";
		for(String val : collectionOfStrings) {
			anotherVal = result + val;
		}
		return result;
	}
	
	public String joinCharacter(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "";
		for(String val : collectionOfStrings) {
			result = result + "," + val;
		}
		return result;
	}
	
	public int distinguishBetweenMathPlusAndConcat(String input) {
		List<Integer> collectionOfints = new ArrayList<>();
		/*
		 * The operator in the loop is not concatenation,
		 * but is a normal arithmetic operation
		 */
		int result = 0;
		for(int val : collectionOfints) {
			result = result + val;
		}
		return result;
	}
	
	public String loopAsSingleBodyStatement(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "";
		/*
		 * Transformation possible only for java 8 and above
		 */
		
		if(collectionOfStrings.isEmpty())
			for(String val : collectionOfStrings) {
				result = result + val;
			}
		return result;
	}

	private List<String> generateStringList(String input) {
		return Arrays.asList(generateArray(input));
	}
	
	private String[] generateArray(String input) {
		return input.split(",");
	}
}

package eu.jsparrow.sample.postRule.stringBuildingLoopJ5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"nls", "unused"})
public class StringBuildingLoopRule {
	
	String result = "";
	
	public String collectionOfStrings(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		StringBuilder resultSb = new StringBuilder();
		for(String val : collectionOfStrings) {
			resultSb.append(val);
		}
		String result = resultSb.toString();
		return result;
	}
	
	public String missingCurlyBrackets(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		StringBuilder resultSb = new StringBuilder();
		for(String val : collectionOfStrings)
			resultSb.append(val);
		String result = resultSb.toString();
		
		return result;
	}
	
	public String statementsBetweenDecAndLoop(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String anotherDecl = "";
		if(collectionOfStrings.isEmpty()) {
			collectionOfStrings.add(anotherDecl);
		}
		StringBuilder resultSb = new StringBuilder();
		for(String val : collectionOfStrings) {
			resultSb.append(val);
		}
		String result = resultSb.toString();
		return result;
	}
	
	public String multipleFragments(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String anotherDecl = "";
		if(collectionOfStrings.isEmpty()) {
			collectionOfStrings.add(anotherDecl);
		}
		StringBuilder resultSb = new StringBuilder();
		for(String val : collectionOfStrings) {
			resultSb.append(val);
		}
		String result = resultSb.toString();
		return result;
	}
	
	public String modifiedResultVariable(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "", anotherDecl = "";
		if(collectionOfStrings.isEmpty()) {
			collectionOfStrings.add(anotherDecl);
			result = "-";
		}
		StringBuilder resultSb = new StringBuilder();
		for(String val : collectionOfStrings) {
			resultSb.append(val);
		}
		result += resultSb.toString();
		return result;
	}
	
	public String nonemptyInitialization(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "-";
		StringBuilder resultSb = new StringBuilder();
		for(String val : collectionOfStrings) {
			resultSb.append(val);
		}
		result += resultSb.toString();
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

			StringBuilder resultSb = new StringBuilder();
			for(String val : collectionOfStrings) {
				resultSb.append(val);
			}
			result += resultSb.toString();
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
		StringBuilder resultSb = new StringBuilder();
		for(String val : collectionOfStrings) {
			resultSb.append(val);
		}
		String result = resultSb.toString();

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

		StringBuilder resultSb = new StringBuilder();
		for(String val : collectionOfStrings) {
			resultSb.append(val);
		}
		result += resultSb.toString();

		return result;
	}
	
	public String plusEqualsOperator(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		StringBuilder resultSb = new StringBuilder();
		for(String val : collectionOfStrings) {
			resultSb.append(val);
		}
		String result = resultSb.toString();

		return result;
	}
	
	public String collectingArrayOfStrings(String input) {
		String[] arrayOfStrings = generateArray(input);
		StringBuilder resultSb = new StringBuilder();
		for(String val : arrayOfStrings) {
			resultSb.append(val);
		}
		String result = resultSb.toString();
		return result;
	}
	
	public String collectingArrayOfNumbers(String input) {
		Double[] arrayOfStrings = {2.1, 3.5};
		StringBuilder resultSb = new StringBuilder();
		for(Double val : arrayOfStrings) {
			resultSb.append(val);
		}
		String result = resultSb.toString();
		return result;
	}
	
	public String savingAnnotationOverArray(String input) {
		String[] arrayOfStrings = generateArray(input);
		StringBuilder resultSb = new StringBuilder();
		for(String val : arrayOfStrings) {
			resultSb.append(val);
		}
		@Deprecated
		String result = resultSb.toString();
		return result;
	}
	
	public String savingAnnotationsOverCollection(String input) {
		List<String> listOfStrings = generateStringList(input);
		StringBuilder resultSb = new StringBuilder();
		for(String val : listOfStrings) {
			resultSb.append(val);
		}
		@Deprecated
		String result = resultSb.toString();
		return result;
	}
	
	public String handleText(char[] txt, int position) {
		StringBuilder textSb = new StringBuilder();
		for (char aTxt : txt) {
			textSb.append(aTxt);
		}
		String text = textSb.toString();
		return text += "\n";
	}
	
	/*
	 * Testing the generated string builder name
	 */
	
	public String newStringBuilderName(String input) {
		String[] arrayOfStrings = generateArray(input);
		String resultSb = "";
		String resultSb1 = "";
		StringBuilder resultSb2 = new StringBuilder();
		for(String val : arrayOfStrings) {
			resultSb2.append(val);
		}
		String result = resultSb2.toString();
		return result;
	}
	
	public String newStringBuilderName2(String input) {
		String[] arrayOfStrings = generateArray(input);
		String resultSb = "";
		if(resultSb.isEmpty()) {			
			String resultSb1 = "";
		}
		StringBuilder resultSb2 = new StringBuilder();
		for(String val : arrayOfStrings) {
			resultSb2.append(val);
		}
		String result = resultSb2.toString();
		return result;
	}
	
	public String cascadedLoops(String input) {
		String[] arrayOfStrings = generateArray(input);
		List<String> listOfStrings = generateStringList(input);
		String resultSb = "";
		String resultSb1 = "";
		StringBuilder resultSb2 = new StringBuilder();
		for(String val : arrayOfStrings) {
			resultSb2.append(val);
		}
		String result = resultSb2.toString();
		
		String anotherResult = "";
		StringBuilder resultSb3 = new StringBuilder();
		for(String val : listOfStrings) {
			resultSb3.append(val);
		}
		result += resultSb3.toString();
		
		return result + anotherResult;
	}
	
	public String clashWithParameterName(String resultSb) {
		String[] arrayOfStrings = generateArray(resultSb);
		List<String> listOfStrings = generateStringList(resultSb);
		StringBuilder resultSb1 = new StringBuilder();
		for(String val : arrayOfStrings) {
			resultSb1.append(val);
		}
		String result = resultSb1.toString();
		
		return result;
	}
	
	/*
	 * Using collect over stream of numbers
	 */
	
	public String colectionOfIntegers(String input) {
		List<Integer> collectionOfints = new ArrayList<>();
		StringBuilder resultSb = new StringBuilder();
		for(int val : collectionOfints) {
			resultSb.append(val);
		}
		String result = resultSb.toString();
		return result;
	}
	
	public String colectionOfDoubles(String input) {
		List<Double> collectionOfints = new ArrayList<>();
		StringBuilder resultSb = new StringBuilder();
		for(double val : collectionOfints) {
			resultSb.append(val);
		}
		String result = resultSb.toString();
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

package at.splendit.simonykees.sample.preRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
@SuppressWarnings({"nls"})
public class EnhancedForLoopToStreamFindFirstRule {
	
	private String globalKey = "";
	
	public String convertToFindFirstBreak(String input) {
		StringBuilder sb = new StringBuilder();
		String key = "";
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	key = value;
		        break;
		    }
		}
		sb.append(key);
		
		return sb.toString();
	}
	
	public String focingTailingMap(String input) {
		StringBuilder sb = new StringBuilder();
		String key = "";
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	key = value + " sth to force a tailing map";
		        break;
		    }
		}
		sb.append(key);
		
		return sb.toString();
	}
	
	public String methodInvocationAsInitializer(String input) {
		List<String> values = generateList(input);
		String key = values.get(0);
		for(String value : values) {
		    if(value.length() > 4) {
		    	key = value;
		        break;
		    }
		}
		
		return key;
	}
	
	public String irrelevantAssignment(String input) {
		StringBuilder sb = new StringBuilder();
		String key = "";
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	key = "sth irrelevant";
		        break;
		    }
		}
		sb.append(key);
		
		return sb.toString();
	}
	
	public String multipleDeclarationFragment(String input) {
		StringBuilder sb = new StringBuilder();
		String key = "", anotherKey = input;
		List<String> values = generateList(anotherKey);
		for(String value : values) {
		    if(value.length() > 4) {
		    	key = value;
		        break;
		    }
		}
		sb.append(key + anotherKey);
		
		return sb.toString();
	}
	
	public String referencedVariable(String input) {
		StringBuilder sb = new StringBuilder();
		String key = "";
		List<String> values = generateList(input);
		key = "key";
		for(String value : values) {
		    if(value.length() > 4) {
		    	key = value;
		        break;
		    }
		}
		sb.append(key);
		
		return sb.toString();
	}
	
	public String multipleIfThenBodyStatements(String input) {
		StringBuilder sb = new StringBuilder();
		String key = "";
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	sb.append(value.length());
		    	key = value;
		        break;
		    }
		}
		sb.append(key);
		
		return sb.toString();
	}
	
	public String multipleLoopBodyStatements(String input) {
		StringBuilder sb = new StringBuilder();
		String key = "";
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	key = value;
		        break;
		    }
		    sb.append(value.length());
		}
		sb.append(key);
		
		return sb.toString();
	}
	
	public String elseClause(String input) {
		StringBuilder sb = new StringBuilder();
		String key = "";
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	key = value;
		        break;
		    } else {
		    	sb.append(value.length());
		    }
		}
		sb.append(key);
		
		return sb.toString();
	}
	
	public String missingDeclarationFragment(String input) {
		StringBuilder sb = new StringBuilder();

		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	globalKey = value;
		        break;
		    }
		}
		sb.append(globalKey);
		
		return sb.toString();
	}
	
	public String missingBreakStatement(String input) {
		String localKey = "localKey";
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	localKey = value;
		    }
		}
		
		return localKey;
	}
	
	public String missingIfStatement(String input) {
		String localKey = "localKey";
		List<String> values = generateList(input);
		for(String value : values) {
	    	localKey = value;
	    	break;
		}
		
		return localKey;
	}
	
	public String nonEffectivelyFinalCondition(String input) {
		List<String> values = generateList(input);
		String localKey = "localKey";
		String key = "key";
		key = "";
		for(String value : values) {
		    if(value.equals(key)) {
		    	localKey = value;
		    	break;
		    }
		}
		
		return localKey;
	}
	
	public String referencingFinalVariable(String input) {
		List<String> values = generateList(input);
		String localKey = "localKey";
		final String key = "key";
		for(String value : values) {
		    if(value.equals(key)) {
		    	localKey = value;
		    	break;
		    }
		}
		
		return localKey;
	}
	
	public String throwException(String input) throws Exception {
		List<String> values = generateList(input);
		String localKey = "localKey";
		String key = "key";
		for(String value : values) {
		    if(compareAndThrowException(value, key)) {
		    	localKey = value;
		    	break;
		    }
		}
		
		return localKey;
	}
	
	public String assigningNullValue(String input) {
		List<String> values = generateList(input);
		String localKey = "localKey";
		final String key = "key";
		for(String value : values) {
		    if(value.equals(key)) {
		    	localKey = null;
		    	break;
		    }
		}
		
		return localKey;
	}
	
	public double implicitBreakCasting00(String input) {
		int defaultIndex = -1;
		List<Integer> values = new ArrayList<>();
		for(int value : values) {
		    if(value > 4) {
		    	defaultIndex = value;
		    	break;
		    }
		}
		
		return defaultIndex;
	}
	
	public double implicitBreakCasting01(String input) {
		int defaultIndex = -1;
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	defaultIndex = value.length();
		    	break;
		    }
		}
		
		return defaultIndex;
	}
	
	public double implicitBreakCasting20(String input) {
		double defaultValue = -1.0;
		double defaultIndex = defaultValue;
		List<Integer> values = new ArrayList<>();
		for(int value : values) {
		    if(value > 4) {
		    	defaultIndex = value;
		    	break;
		    }
		}
		
		return defaultIndex;
	}
	
	public double implicitBreakCasting21(String input) {
		double defaultValue = -1.0;
		double defaultIndex = defaultValue;
		List<Integer> values = new ArrayList<>();
		for(int value : values) {
		    if(value > 4) {
		    	defaultIndex = value + 1;
		    	break;
		    }
		}
		
		return defaultIndex;
	}
	
	public double implicitBreakCasting30(String input) {
		double defaultValue = -1.0;
		double defaultIndex = defaultValue;
		List<Double> values = new ArrayList<>();
		for(double value : values) {
		    if(value > 4) {
		    	return value;
		    }
		}
		return defaultIndex;
	}
	
	public double implicitBreakCasting31(String input) {
		double defaultValue = -1.0;
		double defaultIndex = defaultValue;
		List<Double> values = new ArrayList<>();
		for(double value : values) {
		    if(value > 4) {
		    	defaultIndex = value * 2;
		    	break;
		    }
		}
		return defaultIndex;
	}



	/*
	 * ************* Loops with return statement ***************
	 */
	
	public String convertToFindFirstReturn(String input) {
		List<String> values = generateList(input);
		System.out.println("I dont care what happens next!");
		for(String value : values) {
		    if(value.length() > 4) {
		    	return value;
		    }
		}
		
		return "";
	}
	
	public String missingBrackets(String input) {
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4)
		    	return value;
		}
		
		return "";
	}
	
	public String missingBrackets2(String input) {
		List<String> values = generateList(input);
		for(String value : values)
		    if(value.length() > 4)
		    	return value;
		
		return "";
	}
	
	public String forcingTailingMap(String input) {
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	return value + "sth to force a tailing map";
		    }
		}
		
		return "nothing long was found";
	}
	
	public String returningIrrelevantValue(String input) {
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	return "nothingToDo with 'value'";
		    }
		}
		
		return "";
	}
	
	public String missingReturnValue(String input) {
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	 //missing body
		    }
		}
		
		return "";
	}
	
	public String throwException2(String input) throws Exception {
		List<String> values = generateList(input);
		String localKey = "localKey";
		String key = "key";
		for(String value : values) {
		    if(compareAndThrowException(value, key)) {
		    	return value;
		    }
		}
		
		return localKey;
	}
	
	public String methodInvocationAsReturnExpression(String input) {
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	return value;
		    }
		}
		
		return values.get(0);
	}
	
	public String returningNullValue(String input) {
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	return null;
		    }
		}
		
		return values.get(0);
	}
	
	public double implicitReturnCasting00(String input) {
		int defaultIndex = -1;
		List<Integer> values = new ArrayList<>();
		for(int value : values) {
		    if(value > 4) {
		    	return value;
		    }
		}
		
		return defaultIndex;
	}
	
	public double implicitReturnCasting01(String input) {
		int defaultIndex = -1;
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	return value.length();
		    }
		}
		
		return defaultIndex;
	}
	
	public double implicitReturnCasting10(String input) {
		int defaultIndex = -1;
		List<Double> values = new ArrayList<>();
		for(double value : values) {
		    if(value > 4) {
		    	return value;
		    }
		}
		return defaultIndex;
	}
	
	public double implicitReturnCasting11(String input) {
		int defaultIndex = -1;
		List<Double> values = new ArrayList<>();
		for(double value : values) {
		    if(value > 4) {
		    	return value * 2;
		    }
		}
		return defaultIndex;
	}
	
	public double implicitReturnCasting20(String input) {
		double defaultIndex = -1;
		List<Integer> values = new ArrayList<>();
		for(int value : values) {
		    if(value > 4) {
		    	return value;
		    }
		}
		
		return defaultIndex;
	}
	
	public double implicitReturnCasting21(String input) {
		double defaultIndex = -1;
		List<Integer> values = new ArrayList<>();
		for(int value : values) {
		    if(value > 4) {
		    	return value + 1;
		    }
		}
		
		return defaultIndex;
	}
	
	public double implicitReturnCasting30(String input) {
		double defaultIndex = -1;
		List<Double> values = new ArrayList<>();
		for(double value : values) {
		    if(value > 4) {
		    	return value;
		    }
		}
		return defaultIndex;
	}
	
	public double implicitReturnCasting31(String input) {
		double defaultIndex = -1;
		List<Double> values = new ArrayList<>();
		for(double value : values) {
		    if(value > 4) {
		    	return value * 2;
		    }
		}
		return defaultIndex;
	}

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(","));
	}
	
	private boolean compareAndThrowException(String value, String key) throws Exception {
		if(value == null || key == null) {
			throw new Exception();
		}
		return value.equals(key);
	}
}

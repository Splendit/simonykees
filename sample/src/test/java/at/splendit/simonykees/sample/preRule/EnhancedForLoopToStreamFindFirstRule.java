package at.splendit.simonykees.sample.preRule;

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
	
	public String irrelevantAssignment(String input) {
		StringBuilder sb = new StringBuilder();
		String key = "";
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	key = value + " - sth just to stop the rule";
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
	

	/*
	 * Loops with return statement
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
	
	public String returningIrelevantValue(String input) {
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

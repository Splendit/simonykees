package at.splendit.simonykees.sample.postRule.enhancedForLoopToStreamAnyMatch;

import java.util.List;

/**
 * 
 * @author Ardit Ymeri
 * @since 2.0.2
 *
 */
@SuppressWarnings({"unused", "nls"})
public class EnhancedForLoopToStreamAnyMatchRule {
	
	boolean missingBoolDecl = false;
	
	public void usingAnyMatch(List<String> strings) {
		boolean containsEmpty = strings.stream().anyMatch(value -> value.isEmpty());
	}
	
	public void statementsInBetween(List<String> strings) {
	    String b = "b";
	    if(strings.contains("a")) {
	    	strings.add(b);
	    }
	    boolean containsEmpty = strings.stream().anyMatch(value -> value.isEmpty());
	}
	
	public void statementsBefore(List<String> strings) {
		String b = "b";
	    if(strings.contains("a")) {
	    	strings.add(b);
	    }
	    boolean containsEmpty = strings.stream().anyMatch(value -> value.isEmpty());
	}
	
	public void statementsAfter(List<String> strings) {
	    boolean containsEmpty = strings.stream().anyMatch(value -> value.isEmpty());
	    
		String b = "b";
	    if(strings.contains("a")) {
	    	strings.add(b);
	    }
	}
	
	public void modifiedBoolVariable(List<String> strings) {
	    boolean containsEmpty = false;
	    if(strings.contains("")) {
	    	containsEmpty = true;
	    	return;
	    }
	    
	    for(String value : strings) {
	        if(value.isEmpty()) {
	            containsEmpty = true;
	            break;
	        }
	    }
	}
	
	public void multipleDeclarationFragments(List<String> strings) {
	    boolean containsA = false;
	    if(strings.contains("a")) {
	    	containsA = true;
	    }
	    
	    boolean containsEmpty = strings.stream().anyMatch(value -> value.isEmpty());
	}
	
	public void missingBreakStatement(List<String> strings) {
	    boolean containsEmpty = false;
	    for(String value : strings) {
	        if(value.isEmpty()) {
	            containsEmpty = true;
	        }
	    }
	}
	
	public void existingElseBranch(List<String> strings) {
	    boolean containsEmpty = false;
	    for(String value : strings) {
	        if(value.isEmpty()) {
	            containsEmpty = true;
	            break;
	        } else {
	        	value.split("\\.");
	        }
	    }
	}
	
	public void multipleLoopBodyStatements(List<String> strings) {
	    boolean containsEmpty = false;
	    for(String value : strings) {
	        if(value.isEmpty()) {
	            containsEmpty = true;
	            break;
	        }
	        String[] parts = value.split("\\.");
	        if(parts.length == 0) {
	        	return;
	        }
	    }
	}
	
	public void missingIfStatement(List<String> strings) {
	    boolean containsEmpty = false;
	    for(String value : strings) {
            containsEmpty = true;
            break;
	    }
	}
	
	public void missingBooleanDeclaration(List<String> strings) {
	    for(String value : strings) {
	        if(value.isEmpty()) {
	            missingBoolDecl = true;
	            break;
	        }
	    }
	}
	
	public void swappedBooleanValues(List<String> strings) {
		boolean containsNonEmpty = true;
	    for(String value : strings) {
	        if(value.isEmpty()) {
	        	containsNonEmpty = false;
	            break;
	        }
	    }
	}
	
	public void sameBooleanValues1(List<String> strings) {
		boolean containsNonEmpty = true;
	    for(String value : strings) {
	        if(value.isEmpty()) {
	        	containsNonEmpty = true;
	            break;
	        }
	    }
	}
	
	public void sameBooleanValues2(List<String> strings) {
		boolean containsNonEmpty = false;
	    for(String value : strings) {
	        if(value.isEmpty()) {
	        	containsNonEmpty = false;
	            break;
	        }
	    }
	}
	
	public void compoundCondition(List<String> strings) {
		String emptyString = "";
	    boolean containsEmpty = strings.stream().anyMatch(value -> emptyString.equals(value));
	}
	
	public void nonEffectivelyFinalCondition(List<String> strings) {
		boolean containsEmpty = false;
		String emptyString = "";
		emptyString = "";
	    for(String value : strings) {
	        if(emptyString.equals(value)) {
	        	containsEmpty = true;
	            break;
	        }
	    }
	}
	
	public void loopWithSingleBodyStatement(List<String> strings) {
		String emptyString = "";
	    boolean containsEmpty = strings.stream().anyMatch(value -> emptyString.equals(value));
	}
	
	/*
	 * Testing loops with return statement
	 */
	
	public boolean loopWithReturnStatement(List<String> strings) {
		String emptyString = "";
	    return strings.stream().anyMatch(value -> emptyString.equals(value));
	}
	
	public boolean nonEffectivelyFinalReturnStatement(List<String> strings) {
		String emptyString = "";
		emptyString = "";
	    for(String value : strings) {
	        if(emptyString.equals(value)) {
	        	return true;
	        }
	    }
	    return false;
	}
	
	public boolean statementsBetweenLoopAndReturnStatement(List<String> strings) {
		String emptyString = "";
	    for(String value : strings) {
	        if(emptyString.equals(value)) {
	        	return true;
	        }
	    }
	    String nonEmptyString = "I dont let you convert to anyMatch";
	    return false;
	}
	
	public boolean mixedReturnValues(List<String> strings) {
		String emptyString = "";
	    for(String value : strings) {
	        if(emptyString.equals(value)) {
	        	return false;
	        }
	    }
	    return false;
	}
	
	public boolean mixedReturnValues2(List<String> strings) {
		String emptyString = "";
	    for(String value : strings) {
	        if(emptyString.equals(value)) {
	        	return true;
	        }
	    }
	    return true;
	}
	
	public boolean mixedReturnValues3(List<String> strings) {
		String emptyString = "";
	    for(String value : strings) {
	        if(emptyString.equals(value)) {
	        	return false;
	        }
	    }
	    return true;
	}
	
	public boolean irrelevantStatementsBeforeLoop(List<String> strings) {
		String emptyString = "";
		String nonEmpty = "I dont stop you from converting to anyMatch";
	    return strings.stream().anyMatch(value -> emptyString.equals(value));
	}
	
	public boolean noIfWrapperAroundReturn(List<String> strings) {
		String emptyString = "";
	    for(String value : strings) {
	        	return true;
	    }
	    return false;
	}
	
	public boolean noReturnStatementInsideIf(List<String> strings) {
		String emptyString = "";
	    strings.stream().forEach((value) -> {
	        if(!emptyString.equals(value)) {
	        	String prefix = value.substring(0, 1);
	        }
	    });
	    return false;
	}
	
	public boolean multipleStatementsInsideIf(List<String> strings) {
		String emptyString = "";
	    for(String value : strings) {
	        if(!emptyString.equals(value)) {
	        	String prefix = value.substring(0, 1);
	        	return true;
	        }
	    }
	    return false;
	}
	
	public boolean ifWithSingleBodyStatement(List<String> strings) {
		String emptyString = "";
	    return strings.stream().anyMatch(value -> emptyString.equals(value));
	}
	
	public boolean singleBodyStatementEverywhere(List<String> strings) {
		String emptyString = "";
	    return strings.stream().anyMatch(value -> emptyString.equals(value));
	}
}

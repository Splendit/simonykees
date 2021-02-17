package eu.jsparrow.sample.postRule.multiCatch;

public class TestMultiCatchLUBRule {

	public void methodsNotDefinedInLUB() {
		
		try {
			throwEverything();
		} catch (FirstChildException e) {
			e.childMethod();
		} catch (SecondChildException e) {
			e.childMethod();
		}
		
		try {
			throwEverything();
		} catch (FirstChildException e) {
			e.childMethod();
		} catch (SecondChildException e) {
			e.childMethod();
		}
	}
	
	public void invokingMethodsFromChildChild() {
		try {
			throwChildChildExceptions();
		} catch (FirstChildChildException e) {
			e.childMethod();
		} catch (SecondChildChildException e) {
			e.childMethod();
		}
		
		try {
			throwChildChildExceptions();
		} catch (SecondChildChildException | FirstChildChildException e) {
			e.parentMethod();
		}
		
		try {
			throwChildChildExceptions();
		} catch (SecondChildChildException | FirstChildChildException e) {
			e.interfaceMethod();
		}
	}
	
	public void invokingMethodsInThrowable_shouldTransform() {
		try {
			throwChildChildExceptions();
		} catch (SecondChildException | FirstChildException e) {
			e.printStackTrace();
		}
	}
	
	public void methodsDefinedInLUB_shouldTransform() {
		try {
			throwEverything();
		} catch (FirstChildException e) {
			e.childMethod();
		} catch (SecondChildException e) {
			e.childMethod();
		}
	}
	
	public void throwEverything() throws FirstChildException, SecondChildException, SecondChildChildException, FirstChildChildException {}
	
	public void throwChildChildExceptions() throws SecondChildChildException, FirstChildChildException {}

}

class AppExcpetion extends Exception implements ExceptionType {
	public void parentMethod() {}
}

class FirstChildException extends AppExcpetion {
	public void childMethod() {}
	public void parentMethod() {}
	public void interfaceMethod(){}
}

class FirstChildChildException extends FirstChildException {
	public void childMethod() {}
	public void childChildMethod() {}
}

class SecondChildException extends AppExcpetion {
	public void childMethod() {}
	public void parentMethod() {}
	public void interfaceMethod(){}
}
class SecondChildChildException extends SecondChildException {
	public void childMethod() {}
	public void childChildMethod() {}
}

interface ExceptionType {
	public default void interfaceMethod(){}
}


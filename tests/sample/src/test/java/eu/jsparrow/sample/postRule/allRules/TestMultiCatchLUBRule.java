package eu.jsparrow.sample.postRule.allRules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMultiCatchLUBRule {

	private static final Logger logger = LoggerFactory.getLogger(TestMultiCatchLUBRule.class);

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
			logger.error(e.getMessage(), e);
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

	public void throwEverything() throws FirstChildException, SecondChildException {
	}

	public void throwChildChildExceptions() throws SecondChildChildException, FirstChildChildException {
	}

}

class AppExcpetion extends Exception implements ExceptionType {
	public void parentMethod() {
	}
}

class FirstChildException extends AppExcpetion {
	public void childMethod() {
	}

	@Override
	public void parentMethod() {
	}

	@Override
	public void interfaceMethod() {
	}
}

class FirstChildChildException extends FirstChildException {
	@Override
	public void childMethod() {
	}

	public void childChildMethod() {
	}
}

class SecondChildException extends AppExcpetion {
	public void childMethod() {
	}

	@Override
	public void parentMethod() {
	}

	@Override
	public void interfaceMethod() {
	}
}

class SecondChildChildException extends SecondChildException {
	@Override
	public void childMethod() {
	}

	public void childChildMethod() {
	}
}

interface ExceptionType {
	default void interfaceMethod() {
	}
}

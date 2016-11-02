package at.splendit.simonykees.sample.preRule;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

@SuppressWarnings("nls")
public class TestMultiCatchRule {

	private static Logger log = LogManager.getLogger(TestMultiCatchRule.class);

	@Test
	public void tryWithResourceCommentBugTest() {
		// TODO meaningful Asserts?
		try {
			String.class.getConstructor(String.class).newInstance("aa");
		} catch (InstantiationException e) {
			log.log(Level.TRACE, e);
		} catch (IllegalAccessException e) {
			log.log(Level.TRACE, e);
		} catch (IllegalArgumentException e) {
			log.log(Level.TRACE, e);
		} catch (InvocationTargetException e) {
			log.log(Level.TRACE, e);
		} catch (NoSuchMethodException e) {
			log.log(Level.TRACE, e);
		} catch (SecurityException e) {
			log.log(Level.TRACE, e);
		}
	}

	public int cornerCaseInheritance(int i) {
		try {
			throwSomethingWithInheritance(i);
		} catch (SecondChildChildException e) {
			log.log(Level.TRACE, e);
			i++;
		} catch (SecondChildException e) {
			log.log(Level.TRACE, e);
			i++;
		} catch (SecondChildSecondException e) {
			log.log(Level.TRACE, e);
			i++;
		} catch (FirstException e) {
			log.log(Level.TRACE, e);
			i++;
		} catch (SecondException e) {
			log.log(Level.TRACE, e);
			i++;
		}
		return i;
	}

	/**
	 * Same as cornerCaseInheritance but with different Exception names
	 */
	public int cornerCaseDifferentExceptionNames(int i) {
		try {
			throwSomethingWithInheritance(i);
		} catch (SecondChildChildException e) {
			log.log(Level.TRACE, e);
			i++;
		} catch (SecondChildException e2) {
			log.log(Level.TRACE, e2);
			i++;
		} catch (SecondChildSecondException e3) {
			log.log(Level.TRACE, e3);
			i++;
		} catch (FirstException e4) {
			log.log(Level.TRACE, e4);
			i++;
		} catch (SecondException e5) {
			log.log(Level.TRACE, e5);
			i++;
		}
		return i;
	}
	
	/**
	 * Same as cornerCaseDifferentExceptionNames but without reference to the
	 * Exception variable in the Exception body
	 */
	public int cornerCaseDifferentExceptionNamesNoReferenceInBody(int i) {
		try {
			throwSomethingWithInheritance(i);
		} catch (SecondChildChildException e) {
			i++;
		} catch (SecondChildException e2) {
			i++;
		} catch (SecondChildSecondException e3) {
			i++;
		} catch (FirstException e4) {
			i++;
		} catch (SecondException e5) {
			i++;
		}
		return i;
	}

	public int cornerCaseDifferentBodies(int i) {
		try {
			throwSomething(i);
		} catch (FirstException e) {
			i++; 						// A
		} catch (SecondException e) {
			i++; 						// A
		} catch (ThirdException e) {
			i += 10; 					// B
		} catch (FourthException e) {
			i--; 						// C
		} catch (FifthException e) {
			i--; 						// C
		} catch (SixthException e) {
			i -= 10; 					// D
		}
		return i;
	}
	
	public int cornerCaseMixedCheckedUnchecked(int i) {
		try {
			throwSomethingMixedCheckedAndUnchecked(i);
		} catch (FirstException e) {
			i++;
		} catch (FirstUncheckedException e) {
			i++;
		} catch (SecondException e) {
			i++;
		} catch (SecondtUncheckedException e) {
			i++;
		} catch (ThirdException e) {
			i++;
		} catch (ThirdUncheckedException e) {
			i++;
		}
		return i;
	}

	private void throwSomething(int i)
			throws FirstException, SecondException, ThirdException, FourthException, FifthException, SixthException {
		switch (i) {
		case 1:
			throw new FirstException();
		case 2:
			throw new SecondException();
		case 3:
			throw new ThirdException();
		case 4:
			throw new FourthException();
		case 5:
			throw new FifthException();
		case 6:
			throw new SixthException();

		default:
			break;
		}
	}

	private void throwSomethingWithInheritance(int i) throws FirstException, SecondChildChildException,
			SecondChildException, SecondChildSecondException, SecondException {
		switch (i) {
		case 1:
			throw new FirstException();
		case 2:
			throw new SecondException();
		case 3:
			throw new SecondChildException();
		case 4:
			throw new SecondChildChildException();
		case 5:
			throw new SecondChildSecondException();

		default:
			break;
		}
	}

	private void throwSomethingMixedCheckedAndUnchecked(int i) throws FirstException, FirstUncheckedException,
			SecondException, SecondtUncheckedException, ThirdException, ThirdUncheckedException {
		switch (i) {
		case 1:
			throw new FirstException();
		case 2:
			throw new FirstUncheckedException();
		case 3:
			throw new SecondException();
		case 4:
			throw new SecondtUncheckedException();
		case 5:
			throw new ThirdException();
		case 6:
			throw new ThirdUncheckedException();

		default:
			break;
		}
	}

	@SuppressWarnings("serial")
	class FirstException extends Exception {
	}

	@SuppressWarnings("serial")
	class SecondException extends Exception {
	}

	/**
	 * Child of {@link SecondException}
	 */
	@SuppressWarnings("serial")
	class SecondChildException extends SecondException {
	}

	/**
	 * Child of {@link SecondChildChildException}
	 */
	@SuppressWarnings("serial")
	class SecondChildChildException extends SecondChildException {
	}

	/**
	 * Child of {@link SecondException}
	 */
	@SuppressWarnings("serial")
	class SecondChildSecondException extends SecondException {
	}

	@SuppressWarnings("serial")
	class ThirdException extends Exception {
	}

	@SuppressWarnings("serial")
	class FourthException extends Exception {
	}

	@SuppressWarnings("serial")
	class FifthException extends Exception {
	}

	@SuppressWarnings("serial")
	class SixthException extends Exception {
	}

	@SuppressWarnings("serial")
	class FirstUncheckedException extends RuntimeException {
	}

	@SuppressWarnings("serial")
	class SecondtUncheckedException extends RuntimeException {
	}

	@SuppressWarnings("serial")
	class ThirdUncheckedException extends RuntimeException {
	}
}

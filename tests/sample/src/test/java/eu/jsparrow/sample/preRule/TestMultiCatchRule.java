package eu.jsparrow.sample.preRule;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class TestMultiCatchRule {

	private static Logger log = LoggerFactory.getLogger(TestMultiCatchRule.class);

	@Test
	public void tryWithResourceCommentBugTest() {
		// TODO meaningful Asserts?
		try {
			String.class.getConstructor(String.class).newInstance("aa");
		} catch (InstantiationException e) {
			log.trace(e.getLocalizedMessage(), e);
		} catch (IllegalAccessException e) {
			// keep me
			log.trace(e.getLocalizedMessage(), e);
		} catch (IllegalArgumentException e) {
			// dont duplicate me
			log.trace(e.getLocalizedMessage(), e);
		} catch (InvocationTargetException // I don't want to break anything...
				e) {
			// dont duplicate me
			log.trace(e.getLocalizedMessage(), e);
		} catch (NoSuchMethodException e) {
			log.trace(e.getLocalizedMessage(), e);
		} catch (SecurityException e) {
			log.trace(e.getLocalizedMessage(), e);
		}
	}

	public int cornerCaseInheritance(int i) {
		try {
			throwSomethingWithInheritance(i);
		} catch (SecondChildChildException e) {
			log.trace(e.getLocalizedMessage(), e);
			i++;
		} catch (SecondChildException e) {
			log.trace(e.getLocalizedMessage(), e);
			i++;
		} catch (SecondChildSecondException e) {
			log.trace(e.getLocalizedMessage(), e);
			i++;
		} catch (FirstException e) {
			log.trace(e.getLocalizedMessage(), e);
			i++;
		} catch (SecondException e) {
			log.trace(e.getLocalizedMessage(), e);
			i++;
		}
		return i;
	}
	
	/*
	 * UnionType cornercase
	 */
	public int unionTypeCornerCaseInheritance(int i) {
		try {
			throwSomethingWithInheritance(i);
		} catch (SecondChildChildException | FirstException e) {
			log.trace(e.getLocalizedMessage(), e);
			i++;
		} catch (SecondChildException e) {
			log.trace(e.getLocalizedMessage(), e);
			i++;
		} catch (SecondChildSecondException e) {
			log.trace(e.getLocalizedMessage(), e);
			i++;
		} catch (SecondException e) {
			log.trace(e.getLocalizedMessage(), e);
			i++;
		}
		return i;
	}

	/*
	 * Same as cornerCaseInheritance but with different Exception names
	 */
	public int cornerCaseDifferentExceptionNames(int i) {
		try {
			throwSomethingWithInheritance(i);
		} catch (SecondChildChildException e) {
			log.trace(e.getLocalizedMessage(), e);
			i++;
		} catch (SecondChildException e2) {
			log.trace(e2.getLocalizedMessage(), e2);
			i++;
		} catch (SecondChildSecondException e3) {
			log.trace(e3.getLocalizedMessage(), e3);
			i++;
		} catch (FirstException e4) {
			log.trace(e4.getLocalizedMessage(), e4);
			i++;
		} catch (SecondException e5) {
			log.trace(e5.getLocalizedMessage(), e5);
			i++;
		}
		return i;
	}
	
	/*
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
	
	public void movingTopExceptionToBottom(int i) {
		
		try {
			if(i == 0) {
				throwSomethingWithInheritance(4);
			} else throw new ThirdException();
		} catch (SecondChildSecondException e) {
			log.debug("Same as the most general exception");
		} catch(SecondChildChildException e) {
			log.warn(e.getMessage());
		} catch(ThirdException e) {
			log.warn(e.getMessage());
		} catch(Exception e) {
			log.debug("Same as the most general exception");
		}
	}
	
	public void avoidMovingTopExceptionToBottom(int i) {
		
		try {
			if(i == 0) {
				throwSomethingWithInheritance(4);
			} else throw new ThirdException();
		} catch(SecondChildChildException e) {
			log.warn(e.getMessage());
		} catch(ThirdException e) {
			log.debug(e.getMessage());
		} catch (SecondException e) {
			log.trace(e.getMessage());
		} catch (FirstException e) {
			log.warn(e.getMessage());
		} catch(Exception e) {
			log.debug("Same as the most general exception");
		}
	}
	
	public void avoidOnlyClausesJumpingUnderSuperTypes(int i) {
		
		try {
			if(i == 0) {
				throwSomethingWithInheritance(4);
			} else throw new ThirdException();
		} catch(SecondChildChildException e) {
			log.warn(e.getMessage());
		} catch(ThirdException e) {
			log.warn(e.getMessage());
		} catch (SecondException e) {
			log.trace(e.getMessage());
		} catch (FirstException e) {
			log.warn(e.getMessage());
		} catch(Exception e) {
			log.debug("Same as the most general exception");
		}
	}
	
	public void invokingChildSpecificMethod(int i) {
		try {
			throwSomethingWithInheritance(i);
		} catch (SecondChildChildException e) {
			e.secondChildMethod();
		} catch (SecondChildException e) {
			e.secondChildMethod();
		} catch (SecondChildSecondException e) {
			e.secondChildMethod();
		} catch (FirstException e) {
			e.printStackTrace();
		} catch (SecondException e) {
			e.printStackTrace();
		}
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
	
	public void reporingWithGenericMethods_shouldNotTransform(int i) throws FirstException, SecondException {
		try {
			if(i == 0) {
				throw new FirstException();
			} else {
				throw new SecondException();
			}
			
		} catch (FirstException e) {
			report(e);
		} catch (SecondException e) {
			report(e);
		}
	}
	
	private <T extends Throwable> void report(T e) throws T {
		e.getCause();
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
		public void secondChildMethod() {}
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
		public void secondChildMethod() {}
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

package eu.jsparrow.sample.postRule.allRules;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class TestMultiCatchRule {

	private static final Logger log = LoggerFactory.getLogger(TestMultiCatchRule.class);

	@Test
	public void tryWithResourceCommentBugTest() {
		// TODO meaningful Asserts?
		try {
			String.class.getConstructor(String.class)
				.newInstance("aa");
		} catch (SecurityException | NoSuchMethodException | InvocationTargetException // I
																						// don't
																						// want
																						// to
																						// break
																						// anything...
				| IllegalArgumentException | IllegalAccessException | InstantiationException e) {
			// keep me
			// dont duplicate me
			// I don't want to break anything...
			log.trace(e.getLocalizedMessage(), e);
		}
	}

	public int cornerCaseInheritance(int i) {
		try {
			throwSomethingWithInheritance(i);
		} catch (SecondException | FirstException e) {
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
		} catch (SecondException | FirstException e) {
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
		} catch (SecondException | FirstException e5) {
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
		} catch (SecondException | FirstException e5) {
			log.error(e5.getMessage(), e5);
			i++;
		}
		return i;
	}

	public int cornerCaseDifferentBodies(int i) {
		try {
			throwSomething(i);
		} catch (SecondException | FirstException e) {
			log.error(e.getMessage(), e);
			i++; // A
		} catch (ThirdException e) {
			log.error(e.getMessage(), e);
			i += 10; // B
		} catch (FifthException | FourthException e) {
			log.error(e.getMessage(), e);
			i--; // C
		} catch (SixthException e) {
			log.error(e.getMessage(), e);
			i -= 10; // D
		}
		return i;
	}

	public int cornerCaseMixedCheckedUnchecked(int i) {
		try {
			throwSomethingMixedCheckedAndUnchecked(i);
		} catch (ThirdUncheckedException | ThirdException | SecondtUncheckedException | SecondException
				| FirstUncheckedException | FirstException e) {
			log.error(e.getMessage(), e);
			i++;
		}
		return i;
	}

	public void movingTopExceptionToBottom(int i) {

		try {
			if (i == 0) {
				throwSomethingWithInheritance(4);
			} else {
				throw new ThirdException();
			}
		} catch (ThirdException | SecondChildChildException e) {
			log.warn(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			log.debug("Same as the most general exception");
		}
	}

	public void avoidMovingTopExceptionToBottom(int i) {

		try {
			if (i == 0) {
				throwSomethingWithInheritance(4);
			} else {
				throw new ThirdException();
			}
		} catch (SecondChildChildException e) {
			log.warn(e.getMessage());
		} catch (ThirdException e) {
			log.debug(e.getMessage());
		} catch (SecondException e) {
			log.trace(e.getMessage());
		} catch (FirstException e) {
			log.warn(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			log.debug("Same as the most general exception");
		}
	}

	public void avoidOnlyClausesJumpingUnderSuperTypes(int i) {

		try {
			if (i == 0) {
				throwSomethingWithInheritance(4);
			} else {
				throw new ThirdException();
			}
		} catch (SecondChildChildException e) {
			log.warn(e.getMessage());
		} catch (SecondException e) {
			log.trace(e.getMessage());
		} catch (FirstException | ThirdException e) {
			log.warn(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			log.debug("Same as the most general exception");
		}
	}

	public void invokingChildSpecificMethod(int i) {
		try {
			throwSomethingWithInheritance(i);
		} catch (SecondChildException e) {
			e.secondChildMethod();
		} catch (SecondChildSecondException e) {
			e.secondChildMethod();
		} catch (SecondException | FirstException e) {
			log.error(e.getMessage(), e);
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

	private void throwSomethingWithInheritance(int i) throws FirstException, SecondException {
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

	private void throwSomethingMixedCheckedAndUnchecked(int i) throws FirstException, SecondException, ThirdException {
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
			if (i == 0) {
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
		public void secondChildMethod() {
		}
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
		public void secondChildMethod() {
		}
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

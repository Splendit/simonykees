package at.splendit.simonykees.sample.preRule;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

@SuppressWarnings("nls")
public class TestMultiCatchRule {

	@Test
	public void tryWithResourceCommentBugTest() {
		// TODO meaningful Asserts?
		try {
			String.class.getConstructor(String.class).newInstance("aa");
		} catch (InstantiationException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int cornerCase(int i) {
		try {
			throwSomething(i);
		} catch (SecondChildChildException e) {
			e.printStackTrace();
		} catch (SecondChildException e) {
			e.printStackTrace();
		} catch (SecondChildSecondException e) {
			e.printStackTrace();
		} catch (FirstException e) {
			e.printStackTrace();
		} catch (SecondException e) {
			e.printStackTrace();
		}
		return i;
	}

	public void throwSomething(int i) throws FirstException, SecondChildChildException, SecondChildException,
			SecondChildSecondException, SecondException {
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

	class FirstException extends Exception {
	}

	class SecondException extends Exception {
	}

	class SecondChildException extends SecondException {
	}

	class SecondChildChildException extends SecondChildException {
	}

	class SecondChildSecondException extends SecondException {
	}
	
}

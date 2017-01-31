package at.splendit.simonykees.sample.postRule.serial;

import java.io.Serializable;

public class TestSerialVersionUidRule implements Serializable {

	private class Test01 implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private long otherField;
		private Object objectField;
	}

	private class Test02 implements Serializable {
		private static final long serialVersionUID = 1L;
		/**
		 * 
		 */
		private long testLong;
	}
}

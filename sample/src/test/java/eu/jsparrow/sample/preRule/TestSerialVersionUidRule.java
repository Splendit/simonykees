package eu.jsparrow.sample.preRule;

import java.io.Serializable;

@SuppressWarnings({"serial", "unused"})
public class TestSerialVersionUidRule implements Serializable {

	private class Test01 implements Serializable {
		/**
		 * 
		 */
		private long serialVersionUID = 1L;
		private long otherField;
		private Object objectField;
	}

	private class Test02 implements Serializable {
		/**
		 * 
		 */
		private long testLong, serialVersionUID = 1L;
	}
}

package eu.jsparrow.sample.utilities;

import junit.framework.TestCase;

public class UnexpectedTestCase extends TestCase {

	String helloWorld = "Hello World!";

	public void test() {
		assertEquals("Hello World!", helloWorld);
	}
}

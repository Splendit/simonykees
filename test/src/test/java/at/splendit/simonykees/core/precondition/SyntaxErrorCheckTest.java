package at.splendit.simonykees.core.precondition;

import org.eclipse.jdt.core.IJavaProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.splendit.simonykees.core.util.RulesTestUtil;

@SuppressWarnings("nls")
public class SyntaxErrorCheckTest {

	IJavaProject testproject = null;

	@Before
	public void setUp() throws Exception {
		testproject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@After
	public void tearDown() {
		testproject = null;
	}

	@Test
	public void fileWithErrorPresent() throws Exception {
		
		

	}

}

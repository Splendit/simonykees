
package eu.jsparrow.sample.utilities;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ClassUsingJUnit4TestRule {
	protected final TestRule testRule = new TestRule() {

		@Override
		public Statement apply(Statement base, Description description) {
			return null;
		}
	};
}

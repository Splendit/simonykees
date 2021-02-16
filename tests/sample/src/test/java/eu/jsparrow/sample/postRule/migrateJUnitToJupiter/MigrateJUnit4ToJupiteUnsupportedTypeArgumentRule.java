package eu.jsparrow.sample.postRule.migrateJUnitToJupiter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;

public class MigrateJUnit4ToJupiteUnsupportedTypeArgumentRule {

	private Map<String, List<eu.jsparrow.sample.utilities.DummyTestRule>> rules;
	
	@Before
	public void before() {
		rules = new HashMap<>();
	}
}

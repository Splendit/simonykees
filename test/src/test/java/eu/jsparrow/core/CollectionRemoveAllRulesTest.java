package eu.jsparrow.core;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.impl.CollectionRemoveAllRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.CollectionRemoveAllASTVisitor;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class CollectionRemoveAllRulesTest extends AbstractRulesTest {

	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.collectionRemoveAll";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/collectionRemoveAll";

	private String fileName;
	private Path preRule, postRule;

	public CollectionRemoveAllRulesTest(String fileName, Path preRule, Path postRule) {
		super();
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		rulesList.add(new CollectionRemoveAllRule(CollectionRemoveAllASTVisitor.class));
	}

	@Parameters(name = "{index}: test file[{0}]")
	public static Collection<Object[]> data() throws Exception {
		return AbstractRulesTest.load(POSTRULE_DIRECTORY);
	}

	@Test
	public void testTransformation() throws Exception {
		super.testTransformation(postRule, preRule, fileName, POSTRULE_PACKAGE);
	}
}
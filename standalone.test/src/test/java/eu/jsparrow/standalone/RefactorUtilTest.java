package eu.jsparrow.standalone;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.impl.CodeFormatterRule;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * test class for {@link RefactorUtil}
 * 
 * @author Matthias Webhofer, Hans-Jörg Schrödl
 * @since 2.5.0
 */
public class RefactorUtilTest {

	private final RefactorUtil refactorUtil = new TestableRefactorUtil();

	@SuppressWarnings("unchecked")
	@Test
	public void startRefactoring() throws Exception {
		BundleContext context = mock(BundleContext.class);
		RefactoringPipeline refactoringPipeline = mock(RefactoringPipeline.class);

		refactorUtil.startRefactoring(context, refactoringPipeline);

		verify(refactoringPipeline).createRefactoringStates(anyList());
		verify(refactoringPipeline).doRefactoring(any(NullProgressMonitor.class));
		verify(refactoringPipeline).commitRefactoring();
	}

	class TestableRefactorUtil extends RefactorUtil {

		@Override
		protected YAMLConfig getYamlConfig(String configFilePath, String profile) throws YAMLConfigException {
			return new YAMLConfig();
		}

		@Override
		protected void loadStandaloneConfig(BundleContext context) {
			standaloneConfig = mock(StandaloneConfig.class);
		}

		@Override
		protected List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getProjectRules() {
			return Collections.emptyList();
		}

		@Override
		protected List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getSelectedRules(YAMLConfig config,
				List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> projectRules) throws YAMLConfigException {
			return Collections.singletonList(new CodeFormatterRule());
		}
	}
}

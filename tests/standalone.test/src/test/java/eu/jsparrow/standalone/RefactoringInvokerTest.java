package eu.jsparrow.standalone;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.impl.CodeFormatterRule;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * test class for {@link RefactoringInvoker}
 * 
 * @author Matthias Webhofer, Hans-Jörg Schrödl
 * @since 2.5.0
 */
public class RefactoringInvokerTest {

	private IJavaProject javaProject;
	private RefactoringInvoker refactoringInvoker;

	@Before
	public void setUp() {
		javaProject = mock(IJavaProject.class);
		refactoringInvoker = new TestableRefactoringInvoker();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void startRefactoring() throws Exception {
		BundleContext context = mock(BundleContext.class);
		RefactoringPipeline refactoringPipeline = mock(RefactoringPipeline.class);

		when(javaProject.getElementName()).thenReturn(""); //$NON-NLS-1$
		when(refactoringPipeline.getRulesWithChangesAsString()).thenReturn(""); //$NON-NLS-1$

		refactoringInvoker.startRefactoring(context, refactoringPipeline);

		verify(refactoringPipeline).createRefactoringStates(anyList());
		verify(refactoringPipeline).doRefactoring(any(NullProgressMonitor.class));
		verify(refactoringPipeline).commitRefactoring();
	}

	class TestableRefactoringInvoker extends RefactoringInvoker {

		@Override
		protected YAMLConfig getYamlConfig(String configFilePath, String profile) throws YAMLConfigException {
			return new YAMLConfig();
		}

		@Override
		protected List<StandaloneConfig> loadStandaloneConfig(BundleContext context) {
			standaloneConfigs = Arrays.asList(mock(StandaloneConfig.class));
			return standaloneConfigs;
		}

		@Override
		protected List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getProjectRules(
				StandaloneConfig config) {
			return Collections.emptyList();
		}

		@Override
		protected List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getSelectedRules(YAMLConfig config,
				List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> projectRules) throws YAMLConfigException {
			return Collections.singletonList(new CodeFormatterRule());
		}

		@Override
		protected IJavaProject getJavaProject(StandaloneConfig standaloneConfig) {
			return javaProject;
		}
	}
}

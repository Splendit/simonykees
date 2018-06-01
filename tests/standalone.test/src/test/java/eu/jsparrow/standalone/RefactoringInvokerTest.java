package eu.jsparrow.standalone;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.impl.CodeFormatterRule;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.standalone.exceptions.StandaloneException;

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

	@SuppressWarnings("unchecked")
	@Test(expected = StandaloneException.class)
	public void startRefactoring_exceptinsInCreateRefactoringState_shouldNotCommit() throws Exception {
		BundleContext context = mock(BundleContext.class);
		RefactoringPipeline refactoringPipeline = mock(RefactoringPipeline.class);

		when(javaProject.getElementName()).thenReturn(""); //$NON-NLS-1$
		when(refactoringPipeline.getRulesWithChangesAsString()).thenReturn(""); //$NON-NLS-1$
		when(refactoringPipeline.createRefactoringStates(anyList()))
			.thenThrow(new JavaModelException(new CoreException(Status.CANCEL_STATUS)));

		refactoringInvoker.startRefactoring(context, refactoringPipeline);

		verify(refactoringPipeline, never()).commitRefactoring();
	}

	@Test(expected = StandaloneException.class)
	public void startRefactoring_exceptinsInDoRefactoring_shouldNotCommit() throws Exception {
		BundleContext context = mock(BundleContext.class);
		RefactoringPipeline refactoringPipeline = mock(RefactoringPipeline.class);

		when(javaProject.getElementName()).thenReturn(""); //$NON-NLS-1$
		when(refactoringPipeline.getRulesWithChangesAsString()).thenReturn(""); //$NON-NLS-1$
		doThrow(RefactoringException.class).when(refactoringPipeline)
			.doRefactoring(any(NullProgressMonitor.class));

		refactoringInvoker.startRefactoring(context, refactoringPipeline);

		verify(refactoringPipeline, never()).commitRefactoring();
	}

	@Test
	public void startRefactoring_RuleExceptinsInDoRefactoring_shouldCommit() throws Exception {
		BundleContext context = mock(BundleContext.class);
		RefactoringPipeline refactoringPipeline = mock(RefactoringPipeline.class);

		when(javaProject.getElementName()).thenReturn(""); //$NON-NLS-1$
		when(refactoringPipeline.getRulesWithChangesAsString()).thenReturn(""); //$NON-NLS-1$
		doThrow(RuleException.class).when(refactoringPipeline)
			.doRefactoring(any(NullProgressMonitor.class));

		refactoringInvoker.startRefactoring(context, refactoringPipeline);

		verify(refactoringPipeline).commitRefactoring();
	}

	class TestableRefactoringInvoker extends RefactoringInvoker {

		@Override
		protected YAMLConfig getYamlConfig(String configFilePath, String profile) throws StandaloneException {
			return new YAMLConfig();
		}

		@Override
		protected void loadStandaloneConfig(BundleContext context) {
			IJavaProject javaProject = mock(IJavaProject.class);
			when(javaProject.getElementName()).thenReturn("projectName");//$NON-NLS-1$
			StandaloneConfig standaloneConfig = mock(StandaloneConfig.class);
			when(standaloneConfig.getJavaProject()).thenReturn(javaProject);
			standaloneConfigs = Arrays.asList(standaloneConfig);
		}

		@Override
		protected List<RefactoringRule> getProjectRules(StandaloneConfig config) {
			return Collections.emptyList();
		}

		@Override
		protected List<RefactoringRule> getSelectedRules(YAMLConfig config, List<RefactoringRule> projectRules)
				throws StandaloneException {
			return Collections.singletonList(new CodeFormatterRule());
		}
	}
}

package eu.jsparrow.standalone;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.standalone.exceptions.StandaloneException;

/**
 * test class for {@link RefactoringInvoker}
 * 
 * @author Matthias Webhofer, Hans-Jörg Schrödl
 * @since 2.5.0
 */
public class RefactoringInvokerTest {

	private RefactoringInvoker refactoringInvoker;
	private StandaloneConfig standaloneConfig;

	private MavenProjectImporter mavenImporter;

	@Before
	public void setUp() throws Exception {
		refactoringInvoker = new TestableRefactoringInvoker();
		mavenImporter = mock(MavenProjectImporter.class);

		IJavaProject javaProject = mock(IJavaProject.class);
		when(javaProject.getElementName()).thenReturn("projectName"); //$NON-NLS-1$

		standaloneConfig = mock(StandaloneConfig.class);
		when(standaloneConfig.getJavaProject()).thenReturn(javaProject);

		when(mavenImporter.importProjects(any(File.class), any(String.class)))
			.thenReturn(Collections.singletonList(javaProject));
		refactoringInvoker.setImporter(mavenImporter);
	}

	@Test
	public void startRefactoring() throws Exception {
		BundleContext context = mock(BundleContext.class);
		RefactoringPipeline refactoringPipeline = mock(RefactoringPipeline.class);

		IJavaProject javaProject = mock(IJavaProject.class);
		when(javaProject.getElementName()).thenReturn(""); //$NON-NLS-1$
		when(refactoringPipeline.getRulesWithChangesAsString()).thenReturn(""); //$NON-NLS-1$

		refactoringInvoker.startRefactoring(context);

		verify(standaloneConfig).createRefactoringStates();
		verify(standaloneConfig).computeRefactoring();
		verify(standaloneConfig).commitRefactoring();
	}

	@Test
	public void runInDemoMode_shouldNotCommit() throws Exception {
		BundleContext context = mock(BundleContext.class);
		RefactoringPipeline refactoringPipeline = mock(RefactoringPipeline.class);

		IJavaProject javaProject = mock(IJavaProject.class);
		when(javaProject.getElementName()).thenReturn(""); //$NON-NLS-1$
		when(refactoringPipeline.getRulesWithChangesAsString()).thenReturn(""); //$NON-NLS-1$

		refactoringInvoker.runInDemoMode(context);

		verify(standaloneConfig).createRefactoringStates();
		verify(standaloneConfig).computeRefactoring();
		verify(standaloneConfig, never()).commitRefactoring();
	}

	@Test
	public void startRefactoring_exceptionsInCreateRefactoringState_shouldNotCommit() throws Exception {
		BundleContext context = mock(BundleContext.class);

		doThrow(StandaloneException.class).when(standaloneConfig)
			.createRefactoringStates();

		assertThrows(StandaloneException.class, () -> refactoringInvoker.startRefactoring(context));

		verify(standaloneConfig, never()).computeRefactoring();
		verify(standaloneConfig, never()).commitRefactoring();
	}

	@Test
	public void startRefactoring_exceptionsInDoRefactoring_shouldNotCommit() throws Exception {
		BundleContext context = mock(BundleContext.class);
		doThrow(StandaloneException.class).when(standaloneConfig)
			.computeRefactoring();

		assertThrows(StandaloneException.class, () -> refactoringInvoker.startRefactoring(context));

		verify(standaloneConfig, never()).commitRefactoring();
	}

	class TestableRefactoringInvoker extends RefactoringInvoker {

		@Override
		protected void loadStandaloneConfig(List<IJavaProject> importedProjects, BundleContext context) {
			super.standaloneConfigs = Arrays.asList(standaloneConfig);
		}

	}
}

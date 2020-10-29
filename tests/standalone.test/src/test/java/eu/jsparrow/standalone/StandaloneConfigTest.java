package eu.jsparrow.standalone;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLExcludes;
import eu.jsparrow.core.config.YAMLLoggerRule;
import eu.jsparrow.core.config.YAMLRenamingRule;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.StandaloneStatisticsData;
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.core.rule.impl.CodeFormatterRule;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.standalone.exceptions.StandaloneException;

/**
 * test class for {@link StandaloneConfig}
 * 
 * @author Matthias Webhofer
 * @since 2.5.0
 */
public class StandaloneConfigTest {

	private static final String PROJECT_NAME = "project-name"; //$NON-NLS-1$

	private static Path path;

	private IWorkspace workspace;
	private IJavaProject javaProject;
	private StandaloneConfig standaloneConfig;
	private RefactoringPipeline pipeline;
	private boolean hasRefactoringStates;
	private CompilationUnitProvider iCompilationUnitsProvider;
	private YAMLConfig config;

	@BeforeClass
	public static void setUpClass() throws IOException {
		path = Files.createTempDirectory("jsparrow-standlaone-test-"); //$NON-NLS-1$
	}

	@AfterClass
	public static void tearDownClass() throws IOException {

		Files.deleteIfExists(path);
	}

	@Before
	public void setUp() throws Exception {

		workspace = mock(IWorkspace.class);
		IProject project = mock(IProject.class);
		when(project.getName()).thenReturn(PROJECT_NAME);
		javaProject = mock(IJavaProject.class);
		when(javaProject.getProject()).thenReturn(project);
		pipeline = mock(RefactoringPipeline.class);

		config = mock(YAMLConfig.class);

		standaloneConfig = new TestableStandaloneConfig(path.toString(), "1.8"); //$NON-NLS-1$
		hasRefactoringStates = true;
	}

	@Test
	public void getCompilationUnits_noCompilationUnitsExist_returnsEmptyList() throws Exception {
		when(javaProject.getPackageFragments()).thenReturn(new IPackageFragment[] {});

		List<ICompilationUnit> units = standaloneConfig.findProjectCompilationUnits();

		assertTrue(units.isEmpty());
	}

	@Test
	public void getCompilationUnits_compilationUnitsExist_returnsNonEmptyList() throws Exception {
		IPackageFragment packageFragment = mock(IPackageFragment.class);
		ICompilationUnit compilationUnit = mock(ICompilationUnit.class);

		when(javaProject.getPackageFragments()).thenReturn(new IPackageFragment[] { packageFragment });
		when(packageFragment.containsJavaResources()).thenReturn(true);
		when(packageFragment.getCompilationUnits()).thenReturn(new ICompilationUnit[] { compilationUnit });

		List<ICompilationUnit> units = standaloneConfig.findProjectCompilationUnits();

		verify(packageFragment).open(any());
		assertFalse(units.isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void createRefactoringStates_shouldThrowStandaloneException() throws Exception {
		doThrow(JavaModelException.class).when(pipeline)
			.createRefactoringState(any(ICompilationUnit.class), any(List.class));

		assertThrows(StandaloneException.class, () -> standaloneConfig.createRefactoringStates());

	}

	@Test
	public void createRefactoringStates_abortFlag_shouldThrowStandaloneException() throws Exception {
		standaloneConfig.setAbortFlag();
		YAMLExcludes excludes = mock(YAMLExcludes.class);
		when(excludes.getExcludeClasses()).thenReturn(Collections.emptyList());
		when(excludes.getExcludePackages()).thenReturn(Collections.emptyList());
		when(config.getExcludes()).thenReturn(excludes);

		assertThrows(StandaloneException.class, 
				() -> standaloneConfig.createRefactoringStates());
	}

	@Test
	public void computeRefactoring_shouldCallDoRefactoring() throws Exception {
		hasRefactoringStates = true;
		when(javaProject.getElementName()).thenReturn(PROJECT_NAME);
		when(javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true)).thenReturn("1.1"); //$NON-NLS-1$
		when(pipeline.getRulesWithChangesAsString()).thenReturn("changes-as-string"); //$NON-NLS-1$
		when(config.getRenamingRule()).thenReturn(new YAMLRenamingRule());
		when(config.getLoggerRule()).thenReturn(new YAMLLoggerRule());
		when(config.getRules()).thenReturn(Arrays.asList("CodeFormatter", "FieldRenaming", "StandardLogger"));//$NON-NLS-1$ , //$NON-NLS-2$ //$NON-NLS-3$

		standaloneConfig.computeRefactoring();

		verify(pipeline).doRefactoring(any(NullProgressMonitor.class));
	}

	@Test
	public void computeRefactoring_emptyRefactoringStates() throws Exception {
		hasRefactoringStates = false;

		standaloneConfig.computeRefactoring();

		verify(pipeline, never()).doRefactoring(any(IProgressMonitor.class));
	}

	@Test
	public void computeRefactoring_shouldThrowStandaloneException() throws Exception {
		hasRefactoringStates = true;
		doThrow(RefactoringException.class).when(pipeline)
			.doRefactoring(any(IProgressMonitor.class));
		when(config.getRules()).thenReturn(Collections.singletonList("CodeFormatter"));//$NON-NLS-1$

		assertThrows(StandaloneException.class, () -> standaloneConfig.computeRefactoring());
	}

	@Test
	public void commitRefactoring_emptyRefactoringStates() throws Exception {
		hasRefactoringStates = false;

		standaloneConfig.commitRefactoring();

		verify(pipeline, never()).commitRefactoring();
	}

	@Test
	public void commitChanges_shouldThrowStandaloneException() throws Exception {
		hasRefactoringStates = true;
		doThrow(RefactoringException.class).when(pipeline)
			.commitRefactoring();

		assertThrows(StandaloneException.class, () -> standaloneConfig.commitRefactoring());
	}

	class TestableStandaloneConfig extends StandaloneConfig {

		public TestableStandaloneConfig(String path, String compilerCompliance) throws Exception {
			super(javaProject, path, config, new StandaloneStatisticsMetadata());
			super.refactoringPipeline = pipeline;

		}

		@Override
		public void setUp() {
			ICompilationUnit iCompilationUnit = mock(ICompilationUnit.class);
			iCompilationUnitsProvider = mock(CompilationUnitProvider.class);
			when(iCompilationUnitsProvider.getFilteredCompilationUnits())
				.thenReturn(Collections.singletonList(iCompilationUnit));
			super.compilationUnitsProvider = iCompilationUnitsProvider;

			super.statisticsData = new StandaloneStatisticsData(1, "TestProject", super.statisticsMetadata, //$NON-NLS-1$
					refactoringPipeline);
		}

		@Override
		protected IWorkspace getWorkspace() {
			return workspace;
		}

		@Override
		protected boolean hasRefactoringStates() {
			return hasRefactoringStates;
		}

		@Override
		protected List<RefactoringRule> getProjectRules() {
			return Collections.singletonList(new CodeFormatterRule());
		}

	}
}

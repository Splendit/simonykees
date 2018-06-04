package eu.jsparrow.standalone;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
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
import eu.jsparrow.core.refactorer.RefactoringPipeline;
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

	private File projectFile;
	private File projectFileTmp;
	private File classpathFile;
	private File classpathFileTmp;
	private File settingsDirFile;
	private File settingsDirFileTmp;

	private IWorkspace workspace;
	private IProjectDescription projectDescription;
	private IProject project;
	private IJavaProject javaProject;
	private File mavenDepsFolder;
	private IClasspathEntry classpathEntry;
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

		projectFile = mock(File.class);
		projectFileTmp = mock(File.class);
		classpathFile = mock(File.class);
		classpathFileTmp = mock(File.class);
		settingsDirFile = mock(File.class);
		settingsDirFileTmp = mock(File.class);

		workspace = mock(IWorkspace.class);
		projectDescription = mock(IProjectDescription.class);
		project = mock(IProject.class);
		javaProject = mock(IJavaProject.class);
		mavenDepsFolder = mock(File.class);
		classpathEntry = mock(IClasspathEntry.class);
		pipeline = mock(RefactoringPipeline.class);

		config = mock(YAMLConfig.class);

		standaloneConfig = new TestableStandaloneConfig("id", path.toString(), "1.8"); //$NON-NLS-1$ , //$NON-NLS-2$
		hasRefactoringStates = true;
	}

	@Test
	public void getProjectDescription_projectDescriptionLoaded() throws Exception {

		when(workspace.newProjectDescription(any(String.class))).thenReturn(projectDescription);
		when(projectDescription.getBuildSpec()).thenReturn(new ICommand[] {});
		when(projectFile.getAbsolutePath()).thenReturn("/jsparrow-test"); //$NON-NLS-1$

		standaloneConfig.getProjectDescription();

		verify(projectDescription).setLocation(any(IPath.class));
		verify(projectDescription).setNatureIds(any());
	}

	@Test
	public void initProject() throws Exception {
		standaloneConfig.initProject(projectDescription);

		verify(project).create(eq(projectDescription), any());
		verify(project).open(any());
	}

	@Test
	public void initJavaProject_optionsSetAndProjectInitialized() throws Exception {
		String javaVersion = "1.8"; //$NON-NLS-1$

		when(javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true)).thenReturn(javaVersion);
		when(project.isOpen()).thenReturn(true);

		standaloneConfig.initJavaProject(project);

		verify(javaProject).setOption(eq(JavaCore.COMPILER_COMPLIANCE), eq(javaVersion));
		verify(javaProject).setOption(eq(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM), eq(javaVersion));
		verify(javaProject).setOption(eq(JavaCore.COMPILER_SOURCE), eq(javaVersion));

		verify(javaProject).open(any());
	}

	@Test
	public void getCompilationUnits_noCompilationUnitsExist_returnsEmptyList() throws Exception {
		standaloneConfig.setJavaProject(javaProject);
		when(javaProject.getPackageFragments()).thenReturn(new IPackageFragment[] {});

		List<ICompilationUnit> units = standaloneConfig.findProjectCompilationUnits();

		assertTrue(units.isEmpty());
	}

	@Test
	public void getCompilationUnits_compilationUnitsExist_returnsNonEmptyList() throws Exception {
		IPackageFragment packageFragment = mock(IPackageFragment.class);
		ICompilationUnit compilationUnit = mock(ICompilationUnit.class);

		standaloneConfig.setJavaProject(javaProject);
		when(javaProject.getPackageFragments()).thenReturn(new IPackageFragment[] { packageFragment });
		when(packageFragment.containsJavaResources()).thenReturn(true);
		when(packageFragment.getCompilationUnits()).thenReturn(new ICompilationUnit[] { compilationUnit });

		List<ICompilationUnit> units = standaloneConfig.findProjectCompilationUnits();

		verify(packageFragment).open(any());
		assertFalse(units.isEmpty());
	}

	@Test
	public void collectMavenDependenciesAsClasspathEntries_fileListIsNull_returnsEmptyList() {
		when(mavenDepsFolder.listFiles()).thenReturn(null);

		List<IClasspathEntry> entries = standaloneConfig.collectMavenDependenciesAsClasspathEntries();

		assertTrue(entries.isEmpty());
	}

	@Test
	public void collectMavenDependenciesAsClasspathEntries_fileListIsEmpty_returnsEmptyList() {
		when(mavenDepsFolder.listFiles()).thenReturn(new File[] {});

		List<IClasspathEntry> entries = standaloneConfig.collectMavenDependenciesAsClasspathEntries();

		assertTrue(entries.isEmpty());
	}

	@Test
	public void collectMavenDependenciesAsClasspathEntries_fileListIsNotEmpty_returnsNonEmptyList() {
		File jarFile = mock(File.class);
		when(mavenDepsFolder.listFiles()).thenReturn(new File[] { jarFile });

		List<IClasspathEntry> entries = standaloneConfig.collectMavenDependenciesAsClasspathEntries();

		assertFalse(entries.isEmpty());
	}

	@Test
	public void addToClasspath_emptyClasspathList_doesNotCallAnyMethods() throws Exception {
		standaloneConfig.setJavaProject(javaProject);

		standaloneConfig.addToClasspath(Collections.emptyList());

		verifyZeroInteractions(javaProject);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = StandaloneException.class)
	public void createRefactoringStates_shouldThrowStandaloneException() throws Exception {
		standaloneConfig.setProject(project);
		when(project.getName()).thenReturn(PROJECT_NAME);
		doThrow(JavaModelException.class).when(pipeline)
			.createRefactoringState(any(ICompilationUnit.class), any(List.class));

		standaloneConfig.createRefactoringStates();

		assertTrue(false);
	}

	@Test(expected = StandaloneException.class)
	public void createRefactoringStates_aboardFlag_shouldThrowStandaloneException() throws Exception {
		standaloneConfig.setProject(project);
		when(project.getName()).thenReturn(PROJECT_NAME);
		standaloneConfig.setAboardFlag();
		YAMLExcludes excludes = mock(YAMLExcludes.class);
		when(excludes.getExcludeClasses()).thenReturn(Collections.emptyList());
		when(excludes.getExcludePackages()).thenReturn(Collections.emptyList());
		when(config.getExcludes()).thenReturn(excludes);

		standaloneConfig.createRefactoringStates();

		assertTrue(false);
	}

	@Test
	public void computeRefactoring_emptyRefactoringStates() throws Exception {
		hasRefactoringStates = false;
		standaloneConfig.setProject(project);
		when(project.getName()).thenReturn(PROJECT_NAME);

		standaloneConfig.computeRefactoring();

		verify(pipeline, never()).doRefactoring(any(IProgressMonitor.class));
	}

	@Test(expected = StandaloneException.class)
	public void computeRefactoring_shouldThrowStandaloneException() throws Exception {
		hasRefactoringStates = true;
		standaloneConfig.setProject(project);
		when(project.getName()).thenReturn(PROJECT_NAME);
		doThrow(RefactoringException.class).when(pipeline)
			.doRefactoring(any(IProgressMonitor.class));

		standaloneConfig.computeRefactoring();

		assertTrue(false);
	}

	@Test
	public void commitrefactoring_emptyRefactoringStates() throws Exception {
		hasRefactoringStates = false;
		standaloneConfig.setProject(project);
		when(project.getName()).thenReturn(PROJECT_NAME);

		standaloneConfig.commitRefactoring();

		verify(pipeline, never()).commitRefactoring();
	}

	@Test(expected = StandaloneException.class)
	public void commitChanges_shouldThrowStandaloneException() throws Exception {
		hasRefactoringStates = true;
		standaloneConfig.setProject(project);
		when(project.getName()).thenReturn(PROJECT_NAME);
		doThrow(RefactoringException.class).when(pipeline)
			.commitRefactoring();

		standaloneConfig.commitRefactoring();

		assertTrue(false);
	}

	class TestableStandaloneConfig extends StandaloneConfig {

		public TestableStandaloneConfig(String id, String path, String compilerCompliance) throws Exception {
			super("projectName", path, compilerCompliance, "", new String[] {}, config); //$NON-NLS-1$ //$NON-NLS-2$
			super.refactoringPipeline = pipeline;

		}

		@Override
		public void setUp() {
			ICompilationUnit iCompilationUnit = mock(ICompilationUnit.class);
			iCompilationUnitsProvider = mock(CompilationUnitProvider.class);
			when(iCompilationUnitsProvider.getFilteredCompilationUnits())
				.thenReturn(Collections.singletonList(iCompilationUnit));
			super.compilationUnitsProvider = iCompilationUnitsProvider;
		}

		@Override
		protected IWorkspace getWorkspace() {
			return workspace;
		}

		@Override
		protected File getProjectDescriptionFile() {
			return projectFile;
		}

		@Override
		protected File getProjectDescriptionRenameFile() {
			return projectFileTmp;
		}

		@Override
		protected File getClasspathFileRenameFile() {
			return classpathFileTmp;
		}

		@Override
		protected File getClasspathFileFile() {
			return classpathFile;
		}

		@Override
		protected File getSettingsDirectoryRenameFile() {
			return settingsDirFileTmp;
		}

		@Override
		protected File getSettingsDirectoryFile() {
			return settingsDirFile;
		}

		@Override
		protected IProject getProject(IWorkspace workspace, String name) {
			return project;
		}

		@Override
		protected IJavaProject createJavaProject(IProject project) {
			return javaProject;
		}

		@Override
		protected File getMavenDependencyFolder() {
			return mavenDepsFolder;
		}

		@Override
		protected IClasspathEntry createLibraryClasspathEntry(String jarPath) {
			return classpathEntry;
		}

		@Override
		protected boolean hasRefactoringStates() {
			return hasRefactoringStates;
		}

		@Override
		protected List<RefactoringRule> getProjectRules() {
			return Collections.singletonList(new CodeFormatterRule());
		}

		@Override
		protected List<RefactoringRule> getSelectedRules(List<RefactoringRule> projectRules)
				throws StandaloneException {
			return Collections.singletonList(new CodeFormatterRule());
		}

	}
}

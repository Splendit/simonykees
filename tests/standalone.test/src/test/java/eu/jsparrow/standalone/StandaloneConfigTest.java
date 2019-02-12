package eu.jsparrow.standalone;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
	private static final String DOT_PROJECT = ".project"; //$NON-NLS-1$
	private static final String DOT_CLASSPATH = ".classpath"; //$NON-NLS-1$
	private static final String DOT_SETTINGS = ".settings"; //$NON-NLS-1$
	private static final String DOT_TEMP = ".tmp"; //$NON-NLS-1$

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
	private static final String PROJECT_ROOT_DIR = "project-root-dir"; //$NON-NLS-1$
	private boolean existingProjectFileMoved = false;
	private boolean existingClasspathFileMoved = false;
	private boolean existingSettingsDirectoryMoved = false;

	@Rule
	public TemporaryFolder directory = new TemporaryFolder();
	private File baseDir;

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

		baseDir = directory.newFolder(PROJECT_ROOT_DIR);
		projectFile = directory.newFile(PROJECT_ROOT_DIR + File.separator + DOT_PROJECT);
		classpathFile = directory.newFile(PROJECT_ROOT_DIR + File.separator + DOT_CLASSPATH);
		settingsDirFile = directory.newFolder(PROJECT_ROOT_DIR, DOT_SETTINGS);
		projectFileTmp = new File(baseDir.getPath() + File.separator + DOT_PROJECT + DOT_TEMP);
		classpathFileTmp = new File(baseDir.getPath() + File.separator + DOT_CLASSPATH + DOT_TEMP);
		settingsDirFileTmp = new File(baseDir.getPath() + File.separator + DOT_SETTINGS + DOT_TEMP);

		workspace = mock(IWorkspace.class);
		projectDescription = mock(IProjectDescription.class);
		project = mock(IProject.class);
		javaProject = mock(IJavaProject.class);
		mavenDepsFolder = mock(File.class);
		classpathEntry = mock(IClasspathEntry.class);
		pipeline = mock(RefactoringPipeline.class);

		config = mock(YAMLConfig.class);

		standaloneConfig = new TestableStandaloneConfig(path.toString(), "1.8"); //$NON-NLS-1$
		hasRefactoringStates = true;
	}

	@Test
	public void getProjectDescription_projectDescriptionLoaded() throws Exception {

		when(workspace.newProjectDescription(any(String.class))).thenReturn(projectDescription);
		when(projectDescription.getBuildSpec()).thenReturn(new ICommand[] {});

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

	@Test(expected = StandaloneException.class)
	public void initJavaProject_iProjectNotOpen_shouldThrowException() throws StandaloneException {
		String javaVersion = "1.8"; //$NON-NLS-1$

		when(javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true)).thenReturn(javaVersion);
		when(project.isOpen()).thenReturn(false);

		standaloneConfig.initJavaProject(project);

		assertTrue(false);
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
	public void createRefactoringStates_abortFlag_shouldThrowStandaloneException() throws Exception {
		standaloneConfig.setProject(project);
		when(project.getName()).thenReturn(PROJECT_NAME);
		standaloneConfig.setAbortFlag();
		YAMLExcludes excludes = mock(YAMLExcludes.class);
		when(excludes.getExcludeClasses()).thenReturn(Collections.emptyList());
		when(excludes.getExcludePackages()).thenReturn(Collections.emptyList());
		when(config.getExcludes()).thenReturn(excludes);

		standaloneConfig.createRefactoringStates();

		assertTrue(false);
	}

	@Test
	public void computeRefactoring_shouldCallDoRefactoring() throws Exception {
		hasRefactoringStates = true;
		standaloneConfig.setProject(project);
		standaloneConfig.setJavaProject(javaProject);
		when(project.getName()).thenReturn(PROJECT_NAME);
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
		when(config.getRules()).thenReturn(Collections.singletonList("CodeFormatter"));//$NON-NLS-1$

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

	@Test
	public void backupExistingEclipseFiles_dotProjectExists() throws StandaloneException, IOException {

		projectFileTmp = new File(baseDir.getPath() + File.separator + DOT_PROJECT + DOT_TEMP);
		classpathFile = new File(baseDir.getPath() + File.separator + DOT_CLASSPATH);
		settingsDirFile = new File(baseDir.getPath(), DOT_SETTINGS);

		standaloneConfig.backupExistingEclipseFiles();

		assertTrue(Files.exists(projectFileTmp.toPath()));

	}

	@Test
	public void backupExistingEclipseFiles_dotClasspathExists() throws StandaloneException, IOException {
		classpathFileTmp = new File(baseDir.getPath() + File.separator + DOT_CLASSPATH + DOT_TEMP);
		projectFile = new File(baseDir.getPath() + File.separator + DOT_PROJECT);
		settingsDirFile = new File(baseDir.getPath(), DOT_SETTINGS);

		standaloneConfig.backupExistingEclipseFiles();

		assertTrue(Files.exists(classpathFileTmp.toPath()));

	}

	@Test
	public void backupExistingEclipseFiles_dotSettingsExists() throws StandaloneException, IOException {
		projectFile = new File(baseDir.getPath() + File.separator + DOT_PROJECT);
		classpathFile = new File(baseDir.getPath() + File.separator + DOT_CLASSPATH);
		settingsDirFileTmp = new File(baseDir.getPath() + File.separator + DOT_SETTINGS + DOT_TEMP);

		standaloneConfig.backupExistingEclipseFiles();

		assertTrue(Files.exists(settingsDirFileTmp.toPath()));

	}

	@Test
	public void restoreExistingEclipseFiles_projectFileMoved() throws IOException, CoreException {
		existingProjectFileMoved = true;
		standaloneConfig.setProject(project);
		when(project.getName()).thenReturn(PROJECT_NAME);

		projectFileTmp = directory.newFile(PROJECT_ROOT_DIR + File.separator + DOT_PROJECT + DOT_TEMP);

		standaloneConfig.revertEclipseProjectFiles();

		assertTrue(projectFile.exists());
		assertFalse(projectFileTmp.exists());
	}

	@Test
	public void restoreExistingEclipseFiles_classPathFileMoved() throws IOException, CoreException {
		existingClasspathFileMoved = true;
		standaloneConfig.setProject(project);
		when(project.getName()).thenReturn(PROJECT_NAME);
		classpathFileTmp = directory.newFile(PROJECT_ROOT_DIR + File.separator + DOT_PROJECT + DOT_TEMP);

		standaloneConfig.revertEclipseProjectFiles();

		assertTrue(classpathFile.exists());
		assertFalse(classpathFileTmp.exists());
	}

	@Test
	public void restoreExistingEclipseFiles_settingsFolderMoved() throws IOException, CoreException {
		existingSettingsDirectoryMoved = true;
		standaloneConfig.setProject(project);
		when(project.getName()).thenReturn(PROJECT_NAME);
		settingsDirFileTmp = directory.newFolder(PROJECT_ROOT_DIR, DOT_SETTINGS + DOT_TEMP);

		standaloneConfig.revertEclipseProjectFiles();

		assertTrue(settingsDirFile.exists());
		assertFalse(settingsDirFileTmp.exists());
	}

	class TestableStandaloneConfig extends StandaloneConfig {

		public TestableStandaloneConfig(String path, String compilerCompliance) throws Exception {
			super("projectName", path, compilerCompliance, "", new String[] {}, config, true, //$NON-NLS-1$ //$NON-NLS-2$
					new StandaloneStatisticsMetadata());
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
		protected boolean isExistingProjectFileMoved() {
			return existingProjectFileMoved;
		}

		@Override
		protected boolean isExistingClasspathFileMoved() {
			return existingClasspathFileMoved;
		}

		@Override
		protected boolean isExistingSettingsDirectoryMoved() {
			return existingSettingsDirectoryMoved;
		}
	}
}

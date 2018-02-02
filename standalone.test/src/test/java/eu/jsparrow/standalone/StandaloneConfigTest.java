package eu.jsparrow.standalone;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * test class for {@link StandaloneConfig}
 * 
 * @author Matthias Webhofer
 * @since 2.5.0
 */
public class StandaloneConfigTest {

	private static Path path;
	private static Path tempFilePath;

	private IWorkspace workspace;
	private IProjectDescription projectDescription;
	private IProject project;
	private IJavaProject javaProject;
	private File mavenDepsFolder;
	private IClasspathEntry classpathEntry;
	private StandaloneConfig standaloneConfig;

	@BeforeClass
	public static void setUpClass() throws IOException {
		path = Files.createTempDirectory("jsparrow-standlaone-test-"); //$NON-NLS-1$

	}

	@AfterClass
	public static void tearDownClass() throws IOException {

		Files.deleteIfExists(path);
	}

	@Before
	public void setUp() {
		workspace = mock(IWorkspace.class);
		projectDescription = mock(IProjectDescription.class);
		project = mock(IProject.class);
		javaProject = mock(IJavaProject.class);
		mavenDepsFolder = mock(File.class);
		classpathEntry = mock(IClasspathEntry.class);
		standaloneConfig = new TestableStandaloneConfig("standaloneTest", path.toString(), true); //$NON-NLS-1$
	}

	@Test
	public void getProjectDescription_projectDescriptionExists_projectDescriptionLoaded() throws Exception {
		tempFilePath = Files.createTempFile(path, "jsparrow-standalone-test-", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$

		standaloneConfig.getProjectDescription();

		verify(workspace).loadProjectDescription(any(IPath.class));

		Files.deleteIfExists(tempFilePath);
	}

	@Test
	public void getProjectDescription_projectDescriptionDoesNotExist_projectDescriptionCreated() throws Exception {
		tempFilePath = Paths.get("/", ".jsparrow-project"); //$NON-NLS-1$//$NON-NLS-2$

		when(workspace.newProjectDescription(anyString())).thenReturn(projectDescription);
		when(projectDescription.getNatureIds()).thenReturn(new String[] {});

		standaloneConfig.getProjectDescription();

		verify(workspace).newProjectDescription(anyString());
		verify(projectDescription).getNatureIds();

		ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
		verify(projectDescription).setNatureIds(captor.capture());
		assertTrue(captor.getValue().length == 2);

		verify(projectDescription).setLocation(eq(new org.eclipse.core.runtime.Path(path.toString())));

		assertTrue(standaloneConfig.isDescriptionGenerated());
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

		List<ICompilationUnit> units = standaloneConfig.getCompilationUnits();

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

		List<ICompilationUnit> units = standaloneConfig.getCompilationUnits();

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
		when(mavenDepsFolder.listFiles()).thenReturn(new File[] {jarFile});

		List<IClasspathEntry> entries = standaloneConfig.collectMavenDependenciesAsClasspathEntries();

		assertFalse(entries.isEmpty());
	}
	
	@Test
	public void addToClasspath_emptyClasspathList_doesNotCallAnyMethods() throws Exception {
		standaloneConfig.setJavaProject(javaProject);
		
		standaloneConfig.addToClasspath(Collections.emptyList());
		
		verifyZeroInteractions(javaProject);
	}
	
	@Test
	public void addToClasspath_nonEmptyClasspathList_oldEntriesIsEmpty_setRawClasspathCalled() throws Exception {
		standaloneConfig.setJavaProject(javaProject);
		when(javaProject.getRawClasspath()).thenReturn(new IClasspathEntry[] {});
		
		standaloneConfig.addToClasspath(Collections.singletonList(classpathEntry));
		
		verify(javaProject).getRawClasspath();
		
		ArgumentCaptor<IClasspathEntry[]> captor = ArgumentCaptor.forClass(IClasspathEntry[].class);
		verify(javaProject).setRawClasspath(captor.capture(), any());
		assertTrue(captor.getValue().length == 1);
	}
	
	@Test
	public void addToClasspath_nonEmptyClasspathList_oldEntriesIsNotEmpty_setRawClasspathCalled() throws Exception {
		IClasspathEntry oldEntry = mock(IClasspathEntry.class);
		
		standaloneConfig.setJavaProject(javaProject);
		when(javaProject.getRawClasspath()).thenReturn(new IClasspathEntry[] {oldEntry});
		
		standaloneConfig.addToClasspath(Collections.singletonList(classpathEntry));
		
		verify(javaProject).getRawClasspath();
		
		ArgumentCaptor<IClasspathEntry[]> captor = ArgumentCaptor.forClass(IClasspathEntry[].class);
		verify(javaProject).setRawClasspath(captor.capture(), any());
		assertTrue(captor.getValue().length > 1);
	}

	class TestableStandaloneConfig extends StandaloneConfig {

		public TestableStandaloneConfig(String name, String path) {
			this(name, path, false);
		}

		public TestableStandaloneConfig(String name, String path, boolean testMode) {
			super(name, path, testMode);
		}

		@Override
		protected IWorkspace getWorkspace() {
			return workspace;
		}

		@Override
		protected String getProjectDescriptionPath() {
			return tempFilePath.toString();
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
	}
}

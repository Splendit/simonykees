package eu.jsparrow.maven.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@SuppressWarnings("nls")
public class MavenProjectUtilTest {
	
	private MavenProject project;
	private File projectHomeFolder;
	private File projectSourceFolder;
	
	@Rule
	public TemporaryFolder directory = new TemporaryFolder();
	
	@Before
	public void setUp() throws Exception {
		project = mock(MavenProject.class);
		projectHomeFolder = directory.newFolder("project-home");
		projectSourceFolder = directory.newFolder("project-home", "source-folder");
	}
	
	@Test
	public void findNatureIds_eclipsePlugin_shouldReturnMavenEclipseJavaNatures() {
		String expectedNatureId = "org.eclipse.m2e.core.maven2Nature,org.eclipse.pde.PluginNature,org.eclipse.jdt.core.javanature";
		when(project.getPackaging()).thenReturn("eclipse-plugin");
		
		String natureIds = MavenProjectUtil.findNatureIds(project);
		
		assertEquals(expectedNatureId, natureIds);
	}
	
	@Test
	public void findNatureIds_mavenProject_shouldReturnMavenJavaNatures() {
		String expectedNatureId = "org.eclipse.m2e.core.maven2Nature,org.eclipse.jdt.core.javanature";
		when(project.getPackaging()).thenReturn("jar");
		
		String natureIds = MavenProjectUtil.findNatureIds(project);
		
		assertEquals(expectedNatureId, natureIds);
	}
	
	@Test
	public void findProjectIdentifier_groupAndArtifactId() {
		String expectedProjectId = "group.id.artifact.id";

		when(project.getGroupId()).thenReturn("group.id");
		when(project.getArtifactId()).thenReturn("artifact.id");

		String actualValue = MavenProjectUtil.findProjectIdentifier(project);
		assertTrue(expectedProjectId.equals(actualValue));

	}
	
	@Test
	public void isAggregateProject_hasPomPckage() {
		when(project.getPackaging()).thenReturn("pom");
		assertTrue(MavenProjectUtil.isAggregateProject(project));
	}

	@Test
	public void isAggregateProject_hasListOfModules() {
		when(project.getPackaging()).thenReturn("");
		when(project.getModules()).thenReturn(Collections.singletonList("module"));
		assertTrue(MavenProjectUtil.isAggregateProject(project));
	}

	@Test
	public void isAggregateProject_shouldReturnFalse_jarPackagingNoModules() {
		when(project.getPackaging()).thenReturn("jar");
		when(project.getModules()).thenReturn(Collections.emptyList());
		assertFalse(MavenProjectUtil.isAggregateProject(project));
	}
	
	@Test
	public void findSourceDirectory_undefinedBuild_shouldReturnSrcMainJava() {
		String expectedSource = "src/main/java";
		when(project.getBuild()).thenReturn(null);
		when(project.getParent()).thenReturn(null);
		
		String sourceDirectory = MavenProjectUtil.findSourceDirectory(project);
		
		assertEquals(expectedSource, sourceDirectory);
	}
	
	@Test
	public void findSourceDirectory_definedSource_shouldReturnRelativeSourcePath() {
		String expectedSource = projectSourceFolder.getName();
		Build build = new Build();
		build.setSourceDirectory(projectSourceFolder.getAbsolutePath());
		when(project.getBuild()).thenReturn(build);
		when(project.getBasedir()).thenReturn(projectHomeFolder);
		
		String sourceDirectory = MavenProjectUtil.findSourceDirectory(project);
		
		assertEquals(expectedSource, sourceDirectory);
	}
	
	@Test
	public void findSourceDirectory_undefinedInChild_shouldReturnRelativePathDefinedInParent() {
		String expectedSource = projectSourceFolder.getName();
		Build build = new Build();
		build.setSourceDirectory(projectSourceFolder.getAbsolutePath());
		when(project.getBuild()).thenReturn(build);
		when(project.getBasedir()).thenReturn(projectHomeFolder);
		MavenProject child = new MavenProject();
		child.setParent(project);
		
		String sourceDirectory = MavenProjectUtil.findSourceDirectory(child);
		
		assertEquals(expectedSource, sourceDirectory);
	}
	
	@Test
	public void getCompilerCompliance_shouldReturnComplianceFormBuildPlugins() {
		String expectedCompilerCompliance = "1.8";
		Plugin plugin = new Plugin();
		plugin.setArtifactId("maven-compiler-plugin");
		Xpp3Dom configuration = new Xpp3Dom("configuration") ;
		Xpp3Dom child = new Xpp3Dom("source");
		child.setValue(expectedCompilerCompliance);
		configuration.addChild(child);
		plugin.setConfiguration(configuration);
		when(project.getBuildPlugins()).thenReturn(Collections.singletonList(plugin));
		
		String compilerCompliance = MavenProjectUtil.getCompilerCompliance(project);
		
		assertEquals(expectedCompilerCompliance, compilerCompliance);
	}
	
	@Test
	public void getCompilerCompliance_shouldReturnComplianceFormProjectProperties() {
		String expectedCompilerCompliance = "1.8";
		Properties properties = new Properties();
		properties.setProperty("maven.compiler.source", expectedCompilerCompliance);
		when(project.getBuildPlugins()).thenReturn(Collections.emptyList());
		when(project.getProperties()).thenReturn(properties);
		
		String compilerCompliance = MavenProjectUtil.getCompilerCompliance(project);
		
		assertEquals(expectedCompilerCompliance, compilerCompliance);
	}
	
	@Test
	public void getCompilerCompliance_noComplianceDefined_shouldReturnDefaultJavaVersion() {
		String expectedCompilerCompliance = "1.5"; // our defined default
		Properties properties = new Properties();
		when(project.getBuildPlugins()).thenReturn(Collections.emptyList());
		when(project.getProperties()).thenReturn(properties);
		
		String compilerCompliance = MavenProjectUtil.getCompilerCompliance(project);
		
		assertEquals(expectedCompilerCompliance, compilerCompliance);
	}

}

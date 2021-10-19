package eu.jsparrow.maven.adapter;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

@SuppressWarnings("nls")
public class MavenParametersTest {
	
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

	private MavenParameters parameters;
	private MavenProject project;
	private Build projectBuild;
	private Log log;
	
	@Before
	public void setUp() {
		parameters = new MavenParameters("test");
		project = mock(MavenProject.class);
		projectBuild = mock(Build.class);
		log = mock(Log.class);
		when(project.getBuild()).thenReturn(projectBuild);
		when(projectBuild.getDirectory()).thenReturn("default");
	}
	
	@Test
	public void validateReportDestination_shouldReturnProvided() throws Exception {
		File temp = folder.newFolder("someTarget");
		String provided = temp.getPath();
		String expected = provided;
		
		String actual = parameters.computeValidateReportDestinationPath(project, provided, log);
		assertEquals(expected, actual);
	}
	
	@Test
	public void validateReportDestination_shouldReturnDefault() throws Exception {
		String provided = "not/existing/1934792";
		String expected = projectBuild.getDirectory();
		
		String actual = parameters.computeValidateReportDestinationPath(project, provided, log);
		assertEquals(expected, actual);
	}
	
	@Test
	public void validateReportDestination_noProvidedValue_shouldReturnDefault() throws Exception {
		String provided = projectBuild.getDirectory();
		String expected = provided;
		
		String actual = parameters.computeValidateReportDestinationPath(project, provided, log);
		assertEquals(expected, actual);
	}
}

package eu.jsparrow.adapter;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings({ "nls" })
public class DependencyManagerTest {

	private Log log;
	private DependencyManager dependencyManager;

	@Before
	public void setUp() {
		log = mock(Log.class);
		dependencyManager = new DependencyManager(log);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void prepareDefaultRequest() {
		InvocationRequest request = mock(InvocationRequest.class);
		Properties props = mock(Properties.class);
		MavenProject project = mock(MavenProject.class);
		File projectBaseDir = mock(File.class);
		String baseDirPath = "project/base/directory";

		when(project.getBasedir()).thenReturn(projectBaseDir);
		when(projectBaseDir.getAbsolutePath()).thenReturn(baseDirPath);

		dependencyManager.prepareDefaultRequest(project, request, props);

		ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
		verify(request).setPomFile(fileCaptor.capture());
		assertTrue(fileCaptor.getValue()
			.getAbsolutePath()
			.endsWith("pom.xml"));

		ArgumentCaptor<List> goalsCaptor = ArgumentCaptor.forClass(List.class);
		verify(request).setGoals(goalsCaptor.capture());
		assertTrue(goalsCaptor.getValue()
			.size() == 1);

		verify(props).setProperty(eq(DependencyManager.OUTPUT_DIRECTORY_OPTION_KEY), anyString());

		verify(request).setProperties(eq(props));
	}
}

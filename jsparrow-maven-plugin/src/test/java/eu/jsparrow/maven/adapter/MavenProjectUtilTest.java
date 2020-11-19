package eu.jsparrow.maven.adapter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.maven.util.MavenProjectUtil;

@SuppressWarnings("nls")
public class MavenProjectUtilTest {

	private MavenProject project;

	@Before
	public void setUp() throws Exception {
		project = mock(MavenProject.class);
	}

	@Test
	public void findProjectIdentifier_groupAndArtifactId() {
		String expectedProjectId = "group.id.artifact.id";

		when(project.getGroupId()).thenReturn("group.id");
		when(project.getArtifactId()).thenReturn("artifact.id");

		String actualValue = MavenProjectUtil.findProjectIdentifier(project);
		assertEquals(expectedProjectId, actualValue);

	}

}

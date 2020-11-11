package eu.jsparrow.standalone;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.LocalProjectScanner;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MavenProjectImporterTest {

	@Mock
	LocalProjectScanner localProjectScanner;

	@Mock
	IProjectConfigurationManager projectConfigurationManager;

	@Mock
	EclipseProjectFileManager eclipseProjectFileManager;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	MavenProjectInfo mavenProjectInfo;

	@Mock
	IMavenProjectImportResult mavenProjectImportResult;

	@Mock
	File workspaceRoot;

	@Mock
	IProject project;

	@Mock
	IJavaProject javaProject;

	private MavenProjectImporter importer;

	@Before
	public void setUp() {
		importer = new TestableMavenProjectImporter(eclipseProjectFileManager);
	}

	@Test
	public void importProjects_shouldReturnImportedJavaProjects() throws Exception {
		String folders = "";

		when(workspaceRoot.getAbsolutePath()).thenReturn("");

		given(mavenProjectInfo.getPomFile()
			.getParentFile()
			.getAbsolutePath()).willReturn("");

		when(localProjectScanner.getProjects()).thenReturn(Collections.singletonList(mavenProjectInfo));

		when(project.isOpen()).thenReturn(false);
		when(project.hasNature(anyString())).thenReturn(true);

		when(javaProject.isOpen()).thenReturn(false);

		when(mavenProjectImportResult.getProject()).thenReturn(project);
		when(projectConfigurationManager.importProjects(any(), any(), any()))
			.thenReturn(Collections.singletonList(mavenProjectImportResult));

		List<IJavaProject> importedProjects = importer.importProjects(workspaceRoot, folders);

		verify(localProjectScanner).run(any(IProgressMonitor.class));
		verify(eclipseProjectFileManager).addProjects(ArgumentMatchers.<List<String>>any());
		verify(eclipseProjectFileManager).backupExistingEclipseFiles();
		verify(projectConfigurationManager).importProjects(ArgumentMatchers.<List<MavenProjectInfo>>any(),
				any(ProjectImportConfiguration.class), any(IProgressMonitor.class));
		verify(project).open(any());
		verify(javaProject).open(any());

		assertEquals(1, importedProjects.size());
	}

	class TestableMavenProjectImporter extends MavenProjectImporter {

		public TestableMavenProjectImporter(EclipseProjectFileManager eclipseProjectFileManager) {
			super(eclipseProjectFileManager);
		}

		@Override
		protected LocalProjectScanner getLocalProjectScanner(File workspaceRoot, List<String> folders,
				boolean basedirRenameRequired, MavenModelManager modelManager) {
			return localProjectScanner;
		}

		@Override
		protected IProjectConfigurationManager getProjectConfigurationManager() {
			return projectConfigurationManager;
		}

		@Override
		protected IJavaProject createJavaProject(IProject project) {
			return javaProject;
		}
	}
}

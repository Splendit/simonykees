package eu.jsparrow.standalone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.LocalProjectScanner;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

public class MavenProjectImporterTest {

	LocalProjectScanner localProjectScanner;

	IProjectConfigurationManager projectConfigurationManager;

	EclipseProjectFileManager eclipseProjectFileManager;

	MavenProjectInfo mavenProjectInfo;

	IMavenProjectImportResult mavenProjectImportResult;

	File workspaceRoot;

	IProject project;

	IJavaProject javaProject;

	private MavenProjectImporter importer;

	@BeforeEach
	public void setUp() {

		localProjectScanner = mock(LocalProjectScanner.class);

		projectConfigurationManager = mock(IProjectConfigurationManager.class);

		eclipseProjectFileManager = mock(EclipseProjectFileManager.class);

		mavenProjectInfo = mock(MavenProjectInfo.class);

		mavenProjectImportResult = mock(IMavenProjectImportResult.class);

		workspaceRoot = mock(File.class);

		project = mock(IProject.class);

		javaProject = mock(IJavaProject.class);

		importer = new TestableMavenProjectImporter(eclipseProjectFileManager);
	}

	@Test
	public void importProjects_shouldReturnImportedJavaProjects() throws Exception {
		String folders = "";

		when(workspaceRoot.getAbsolutePath()).thenReturn("");
		
		when(mavenProjectInfo.getPomFile()).thenReturn(mock(File.class));
		given(mavenProjectInfo.getPomFile().getParentFile()).willReturn(mock(File.class));
		given(mavenProjectInfo.getPomFile().getParentFile().getAbsolutePath()).willReturn("");

		when(localProjectScanner.getProjects()).thenReturn(Collections.singletonList(mavenProjectInfo));

		when(project.isOpen()).thenReturn(false);
		when(project.hasNature(anyString())).thenReturn(true);

		when(javaProject.isOpen()).thenReturn(false);

		when(mavenProjectImportResult.getProject()).thenReturn(project);
		when(mavenProjectImportResult.getMavenProjectInfo()).thenReturn(mavenProjectInfo);
		when(projectConfigurationManager.importProjects(any(), any(), any()))
				.thenReturn(Collections.singletonList(mavenProjectImportResult));
		when(mavenProjectInfo.getModel()).thenReturn(new Model());

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

		@Override
		protected void logProjectInfo(IProject project) {

		}
	}
}

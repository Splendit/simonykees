package eu.jsparrow.standalone;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.LocalProjectScanner;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.standalone.exceptions.MavenImportException;

public class MavenProjectImporter {

	private static final Logger logger = LoggerFactory.getLogger(MavenProjectImporter.class);

	private static final String POM_FILE_NAME = "pom.xml"; //$NON-NLS-1$

	private List<MavenProjectInfo> projectInfos = new LinkedList<>();
	private Map<String, MavenProjectInfo> parentProjects = new HashMap<>();

	private Map<String, IProject> projects;
	private Map<String, IJavaProject> javaProjects = new HashMap<>();
	private Map<String, String> compilerCompliances = new HashMap<>();

	public MavenProjectImporter() {
	}

	public Map<String, IJavaProject> importProjectsUsingLocalProjectScanner(File workspaceRoot, String folder,
			String defaultGroupId) throws MavenImportException {
		return importProjectsUsingLocalProjectScanner(workspaceRoot, Collections.singletonList(folder), defaultGroupId);
	}

	public Map<String, IJavaProject> importProjectsUsingLocalProjectScanner(File workspaceRoot, List<String> folders,
			String defaultGroupId) throws MavenImportException {
		try {
			MavenModelManager modelManager = MavenPlugin.getMavenModelManager();

			LocalProjectScanner lps = new LocalProjectScanner(workspaceRoot, folders, false, modelManager);
			lps.run(new NullProgressMonitor());

			// this.projectInfos = lps.getProjects();

			List<MavenProjectInfo> projects = lps.getProjects();
			this.projectInfos = getProjects(projects);

			Map<String, IJavaProject> imported = importMavenProjects(defaultGroupId);
			return imported;
		} catch (InterruptedException e) {
			throw new MavenImportException(e.getMessage(), e);
		}
	}

	// this collects all projects for analyzing..
	List<MavenProjectInfo> getProjects(Collection<MavenProjectInfo> input) {
		List<MavenProjectInfo> toRet = new ArrayList<MavenProjectInfo>();
		for (MavenProjectInfo info : input) {
			toRet.add(info);
			toRet.addAll(getProjects(info.getProjects()));
		}
		return toRet;
	}

	public void addProjectInfo(String path, String projectName, String compilerCompliance, String parentPath)
			throws CoreException {
		// MavenProjectInfo parentMpi = null;
		// if (parentPath != null) {
		// File parentPom = Paths.get(parentPath, POM_FILE_NAME)
		// .toFile();
		// if (parentProjects.containsKey(parentPom.getAbsolutePath())) {
		// parentMpi = parentProjects.get(parentPom.getAbsolutePath());
		// } else {
		//
		// Model parentModel = MavenPlugin.getMavenModelManager()
		// .readMavenModel(parentPom);
		//
		// parentMpi = new MavenProjectInfo(parentModel.getArtifactId(),
		// parentPom, parentModel, null);
		// parentProjects.put(parentPom.getAbsolutePath(), parentMpi);
		// }
		// }

		File pomFile = Paths.get(path, POM_FILE_NAME)
			.toFile();

		Model model = MavenPlugin.getMavenModelManager()
			.readMavenModel(pomFile);
		MavenProjectInfo mpi = new MavenProjectInfo(projectName, pomFile, model, null);
		projectInfos.add(mpi);
		compilerCompliances.put(projectName, compilerCompliance);
	}

	public Map<String, IJavaProject> importMavenProjects(String defaultGroupId) throws MavenImportException {
		try {
			projects = doImportMavenProject(defaultGroupId);

			for (Map.Entry<String, IProject> entry : projects.entrySet()) {
				String id = entry.getKey();
				IProject project = entry.getValue();

				if (!project.isOpen()) {
					project.open(new NullProgressMonitor());
				}

				createJavaProject(project).ifPresent(p -> javaProjects.put(id, p));
			}

			return javaProjects;
		} catch (CoreException e) {
			throw new MavenImportException("The maven project could not be imported!", e); //$NON-NLS-1$
		}
	}

	private Map<String, IProject> doImportMavenProject(String defaultGroupId) throws CoreException {

		ProjectImportConfiguration pic = new ProjectImportConfiguration();

		List<IMavenProjectImportResult> results = MavenPlugin.getProjectConfigurationManager()
			.importProjects(projectInfos, pic, new NullProgressMonitor());

		Map<String, IProject> projectsMap = results.stream()
			.collect(Collectors.toMap(r -> {
				Model m = ((IMavenProjectImportResult) r).getMavenProjectInfo()
					.getModel();
				String group = m.getGroupId();
				group = (group != null && !group.isEmpty()) ? group : defaultGroupId;
				
				String artifact = m.getArtifactId();
				return group + "." + artifact;
			}, r -> ((IMavenProjectImportResult) r).getProject()));

		// List<IProject> projects = results.stream()
		// .map(IMavenProjectImportResult::getProject)
		// .collect(Collectors.toList());

		// ResourcesPlugin.getWorkspace()
		// .build(IncrementalProjectBuilder.FULL_BUILD, null);

		return projectsMap;
	}

	private Optional<IJavaProject> createJavaProject(IProject project) throws CoreException {
		if (project.hasNature(JavaCore.NATURE_ID)) {
			IJavaProject javaProject = JavaCore.create(project);

			String compilerCompliance = compilerCompliances.get(project.getName());

			javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, compilerCompliance);
			javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, compilerCompliance);
			javaProject.setOption(JavaCore.COMPILER_SOURCE, compilerCompliance);

			if (!javaProject.isOpen()) {
				javaProject.open(new NullProgressMonitor());
			}

			return Optional.ofNullable(javaProject);
		}

		return Optional.empty();
	}
}

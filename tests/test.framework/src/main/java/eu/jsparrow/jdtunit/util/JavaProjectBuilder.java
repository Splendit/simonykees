package eu.jsparrow.jdtunit.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;

import eu.jsparrow.jdtunit.JdtUnitException;

public class JavaProjectBuilder {

	private String name = "DefaultProject"; 

	private HashMap<String, String> options = new HashMap<>();

	public JavaProjectBuilder name(String name) {
		this.name = name;
		return this;
	}

	public JavaProjectBuilder option(String key, String value) {
		this.options.put(key, value);
		return this;
	}

	public JavaProjectBuilder options(Map<String, String> options) {
		this.options.putAll(options);
		return this;
	}

	public IJavaProject build() throws JdtUnitException {
		IProject project = createIProject();
		
		IProjectDescription description;
		try {
			description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.setDescription(description, null);
		} catch (CoreException e) {
			throw new JdtUnitException("Failed to create project description", e);
		}

		return createJavaProject(project);
	}

	private IProject createIProject() throws JdtUnitException {
		IProject project = ResourcesPlugin.getWorkspace()
			.getRoot()
			.getProject(name);
		try {
			project.create(null);
			project.open(null);
		} catch (CoreException e) {
			throw new JdtUnitException("Failed to create project.", e);
		}
		return project;
	}
	
	private IJavaProject createJavaProject(IProject project) throws JdtUnitException {
		IJavaProject javaProject = JavaCore.create(project);

		// build path is: project as source folder and JRE container
		IClasspathEntry[] cpentry = new IClasspathEntry[] { JavaCore.newSourceEntry(javaProject.getPath()),
				JavaRuntime.getDefaultJREContainerEntry() };
		try {
			javaProject.setRawClasspath(cpentry, javaProject.getPath(), null);
		} catch (JavaModelException e) {
			throw new JdtUnitException("Failed to set set project classpath", e);
		}
		javaProject.setOptions(options);
		return javaProject;
	}

}

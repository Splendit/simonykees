package eu.jsparrow.ui.wizard.projects.wrapper;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import eu.jsparrow.ui.wizard.projects.JavaCompilationUnitsCollector;

/**
 * @since 4.17.0
 */
public class JavaProjectWrapper {

	private final IJavaProject javaProject;
	// TODO: use a list of PackageRootNode as child list!!!
	private List<PackageFragmentWrapper> javaPackages;

	public JavaProjectWrapper(IJavaProject javaProject) {
		this.javaProject = javaProject;
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}

	/**
	 * Replace this getter by a getter which returns a list of PackageRootNode
	 * objects.
	 */
	@Deprecated
	public List<PackageFragmentWrapper> getJavaPackages() {
		if (javaPackages == null) {
			try {
				javaPackages = new JavaCompilationUnitsCollector().loadJavaPackageNodeList(javaProject);
			} catch (JavaModelException e) {
				e.printStackTrace();
				javaPackages = Collections.emptyList();
			}
		}
		return javaPackages;
	}
}

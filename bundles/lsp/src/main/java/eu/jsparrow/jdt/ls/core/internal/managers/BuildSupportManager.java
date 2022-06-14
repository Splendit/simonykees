package eu.jsparrow.jdt.ls.core.internal.managers;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.core.resources.IProject;

import eu.jsparrow.jdt.ls.core.internal.ExtensionsExtractor;
import eu.jsparrow.jdt.ls.core.internal.IConstants;


public class BuildSupportManager {
	private static final BuildSupportManager instance = new BuildSupportManager();
	private List<IBuildSupport> lazyLoadedBuildSupportList;

	private BuildSupportManager() {}

	public static List<IBuildSupport> obtainBuildSupports() {
		if (instance.lazyLoadedBuildSupportList == null) {
			instance.lazyLoadedBuildSupportList = ExtensionsExtractor.extractOrderedExtensions(IConstants.PLUGIN_ID, "buildSupport");
		}

		return instance.lazyLoadedBuildSupportList;
	}

	public static Optional<IBuildSupport> find(IProject project) {
		return instance.find(bs -> bs.applies(project));
	}

	public static Optional<IBuildSupport> find(String buildToolName) {
		return instance.find(bs -> bs.buildToolName().equalsIgnoreCase(buildToolName));
	}

	private Optional<IBuildSupport> find(Predicate<? super IBuildSupport> predicate) {
		return obtainBuildSupports().stream().filter(predicate).findFirst();
	}
}

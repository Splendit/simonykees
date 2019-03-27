package eu.jsparrow.core.config;

import java.util.LinkedList;
import java.util.List;

/**
 * model class for excludes data
 * 
 * @since 2.6.0
 */
public class YAMLExcludes {

	private List<String> excludeModules;

	private List<String> excludePackages;

	private List<String> excludeClasses;

	public YAMLExcludes() {
		excludeModules = new LinkedList<>();
		excludePackages = new LinkedList<>();
		excludeClasses = new LinkedList<>();
	}
	
	public List<String> getExcludeModules() {
		return excludeModules;
	}

	public void setExcludeModules(List<String> excludeModules) {
		if (null == excludeModules) {
			return;
		}
		this.excludeModules = excludeModules;
	}

	public List<String> getExcludePackages() {
		return excludePackages;
	}

	public void setExcludePackages(List<String> excludePackages) {
		if (null == excludePackages) {
			return;
		}
		this.excludePackages = excludePackages;
	}

	public List<String> getExcludeClasses() {
		return excludeClasses;
	}

	public void setExcludeClasses(List<String> excludeClasses) {
		if (null == excludeClasses) {
			return;
		}
		this.excludeClasses = excludeClasses;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "YAMLExcludes [excludeModules=" + excludeModules + ", excludePackages=" + excludePackages
				+ ", excludeClasses=" + excludeClasses + "]";
	}
}

package eu.jsparrow.core.config;

import java.util.LinkedList;
import java.util.List;

public class YAMLExcludes {

	private List<String> excludeModules = new LinkedList<>();

	private List<String> excludePackages = new LinkedList<>();
	
	private List<String> excludeClasses = new LinkedList<>();

	public List<String> getExcludeModules() {
		return excludeModules;
	}

	public void setExcludeModules(List<String> excludeModules) {
		this.excludeModules = excludeModules;
	}
	
	public List<String> getExcludePackages() {
		return excludePackages;
	}

	public void setExcludePackages(List<String> excludePackages) {
		this.excludePackages = excludePackages;
	}
	
	public List<String> getExcludeClasses() {
		return excludeClasses;
	}

	public void setExcludeClasses(List<String> excludeClasses) {
		this.excludeClasses = excludeClasses;
	}
	
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "YAMLExcludes [excludeModules=" + excludeModules + ", excludePackages=" + excludePackages + ", excludeClasses=" + excludeClasses + "]";
	}
}

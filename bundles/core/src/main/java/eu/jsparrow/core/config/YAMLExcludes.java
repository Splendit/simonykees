package eu.jsparrow.core.config;

import java.util.LinkedList;
import java.util.List;

public class YAMLExcludes {

	private List<String> excludeModules;

	private List<String> excludePackages;
	
	private List<String> excludeClasses;

	public YAMLExcludes() {
		excludeModules = new LinkedList<>();
		excludePackages = new LinkedList<>();
		excludeClasses = new LinkedList<>();
	}
	
	public YAMLExcludes(List<String> excludeModules, List<String> excludePackages, List<String> excludeClasses) {
		this.excludeModules = excludeModules;
		this.excludePackages = excludePackages;
		this.excludeClasses = excludeClasses;
	}
	
	public List<String> getExcludeModules() {
		if (excludeModules == null) {
			excludeModules = new LinkedList<>();
		}
		return excludeModules;
	}

	public void setExcludeModules(List<String> excludeModules) {
		this.excludeModules = excludeModules;
	}
	
	public List<String> getExcludePackages() {
		if (excludePackages == null) {
			excludePackages = new LinkedList<>();
		}
		return excludePackages;
	}

	public void setExcludePackages(List<String> excludePackages) {
		this.excludePackages = excludePackages;
	}
	
	public List<String> getExcludeClasses() {
		if (excludeClasses == null) {
			excludeClasses = new LinkedList<>();
		}
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

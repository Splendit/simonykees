package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import eu.jsparrow.core.rule.impl.unused.Constants;
import eu.jsparrow.ui.wizard.IValueChangeListener;

public class RemoveUnusedCodeWizardPageModel {

	Set<IValueChangeListener> listeners = new HashSet<>();

	private List<String> classMemberTypes;
	private String searchScope;
	private boolean removeTestCode;
	private boolean removeInitializersWithSideEffects;
	

	public RemoveUnusedCodeWizardPageModel() {
		classMemberTypes = initClassMemberTypes();
		searchScope = "Project";

	}

	private List<String> initClassMemberTypes() {
		List<String> options = new ArrayList<>();
		options.add("private fields");
		options.add("protected fields");
		options.add("package-private fields");
		options.add("public fields");
		options.add("private methods");
		options.add("protected methods");
		options.add("package-private methods");
		options.add("public methods");
		options.add("private classes");
		options.add("protected classes");
		options.add("package-protected classes");
		options.add("public classes");
		return Collections.unmodifiableList(options);
	}

	public void notifyListeners() {
		listeners.forEach(IValueChangeListener::valueChanged);
	}
	
	public void addListener(IValueChangeListener listener) {
		listeners.add(listener);
	}

	public List<String> getClassMemberTypes() {
		return this.classMemberTypes;
	}

	public List<String> getDefaultClassMemberTypes() {
		return this.initClassMemberTypes().stream()
				.filter(name -> name.startsWith("private")) //$NON-NLS-1$
				.collect(Collectors.toList());
	}

	public void setClassMemberTypes(List<String> classMemberTypes) {
		this.classMemberTypes = Collections.unmodifiableList(classMemberTypes);
		notifyListeners();
	}

	
	public void setSearchScope(String newValue) {
		this.searchScope = newValue;
		notifyListeners();
	}
	
	public void setRemoveTestCode(boolean newValue) {
		this.removeTestCode = newValue;
		notifyListeners();
	}
	
	public void setRemoveInitializersWithSideEffects(boolean newValue) {
		this.removeInitializersWithSideEffects = newValue;
		notifyListeners();
	}
	
	public List<String> getSearchScopeOptions() {
		List<String> searchScopes = new ArrayList<>();
		searchScopes.add("Project");
		searchScopes.add("Workspace");
		return searchScopes;
	}
	
	public Map<String, Boolean> getOptionsMap() {
		Map<String, Boolean> map = new HashMap<>();
		boolean removePrivateFields = classMemberTypes.contains("private fields");
		boolean removeProtectedFields = classMemberTypes.contains("protected fields");
		boolean removePublicFields = classMemberTypes.contains("public fields");
		boolean removePackagePrivateFields = classMemberTypes.contains("package-private fields");
		map.put(Constants.PRIVATE_FIELDS, removePrivateFields);
		map.put(Constants.PROTECTED_FIELDS, removeProtectedFields);
		map.put(Constants.PUBLIC_FIELDS, removePublicFields);
		map.put(Constants.PACKAGE_PRIVATE_FIELDS, removePackagePrivateFields);
		
		map.put(Constants.PRIVATE_METHODS, false);
		map.put(Constants.PROTECTED_METHODS, false);
		map.put(Constants.PUBLIC_METHODS, false);
		map.put(Constants.PACKAGE_PRIVATE_METHODS, false);
		
		map.put(Constants.PRIVATE_CLASSES, false);
		map.put(Constants.PROTECTED_CLASSES, false);
		map.put(Constants.PUBLIC_CLASSES, false);
		map.put(Constants.PACKAGE_PRIVATE_CLASSES, false);
		
		map.put(Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS, removeInitializersWithSideEffects);
		map.put(Constants.REMOVE_TEST_CODE, removeTestCode);
		return map;
	}
	
	public String getSearchScope() {
		return this.searchScope;
	}
}

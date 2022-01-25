package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.jsparrow.ui.wizard.IValueChangeListener;

public class RemoveDeadCodeWizardPageModel {

	Set<IValueChangeListener> listeners = new HashSet<>();

	private List<String> classMemberTypes;
	private String searchScope;
	private String removeTestCode;
	private String removeInitializersWithSideEffects;
	

	public RemoveDeadCodeWizardPageModel() {
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

	public void setClasMemberTypes(List<String> classMemberTypes) {
		this.classMemberTypes = Collections.unmodifiableList(classMemberTypes);
		notifyListeners();
	}

	public void setSearchScope(String newValue) {
		this.searchScope = newValue;
		notifyListeners();
	}
	
	public void setRemoveTestCode(String newValue) {
		this.removeTestCode = newValue;
		notifyListeners();
	}
	
	public void setRemoveInitializersWithSideEffects(String newValue) {
		this.removeInitializersWithSideEffects = newValue;
		notifyListeners();
	}
	
	public List<String> getSearchScopeOptions() {
		List<String> searchScopes = new ArrayList<>();
		searchScopes.add("Project");
		searchScopes.add("Workspace");
		return searchScopes;
	}
}

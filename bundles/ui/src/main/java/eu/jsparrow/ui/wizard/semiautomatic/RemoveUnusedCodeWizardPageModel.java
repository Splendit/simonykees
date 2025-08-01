package eu.jsparrow.ui.wizard.semiautomatic;

import static eu.jsparrow.ui.wizard.semiautomatic.RemoveUnusedCodeWizardPageConstants.*;

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

/**
 * The configuration model for removing used code. Let's user choose: <br/>
 * <ul>
 * <li>class members they want to remove by the access modifier</li>
 * <li>the scope to search references for</li>
 * <li>whether to remove initializers that may potentially have side effects
 * </li>
 * </ul>
 * 
 * @since 4.8.0
 *
 */
public class RemoveUnusedCodeWizardPageModel {

	Set<IValueChangeListener> listeners = new HashSet<>();

	private List<String> selectedClassMemberTypes;
	private String searchScope;
	private boolean removeTestCode;
	private boolean removeInitializersWithSideEffects;

	public RemoveUnusedCodeWizardPageModel() {
		selectedClassMemberTypes = getDefaultClassMemberTypes();
		searchScope = PROJECT;
	}

	public List<String> getAllClassMemberTypes() {
		List<String> options = new ArrayList<>();
		options.add(PRIVATE_FIELDS);
		options.add(PROTECTED_FIELDS);
		options.add(PACKAGE_PRIVATE_FIELDS);
		options.add(PUBLIC_FIELDS);
		options.add(PRIVATE_METHODS);
		options.add(PROTECTED_METHODS);
		options.add(PACKAGE_PRIVATE_METHODS);
		options.add(PUBLIC_METHODS);
		options.add(LOCAL_CLASSES);
		options.add(PRIVATE_CLASSES);
		options.add(PROTECTED_CLASSES);
		options.add(PACKAGE_PRIVATE_CLASSES);
		options.add(PUBLIC_CLASSES);
		return Collections.unmodifiableList(options);
	}

	public void notifyListeners() {
		listeners.forEach(IValueChangeListener::valueChanged);
	}

	public void addListener(IValueChangeListener listener) {
		listeners.add(listener);
	}

	public List<String> getClassMemberTypes() {
		return this.selectedClassMemberTypes;
	}

	public List<String> getDefaultClassMemberTypes() {
		return this.getAllClassMemberTypes()
			.stream()
			.filter(name -> name.startsWith("private")) //$NON-NLS-1$
			.collect(Collectors.toList());
	}

	public void setClassMemberTypes(List<String> classMemberTypes) {
		this.selectedClassMemberTypes = Collections.unmodifiableList(classMemberTypes);
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
		searchScopes.add(PROJECT);
		searchScopes.add(WORKSPACE);
		return searchScopes;
	}

	public Map<String, Boolean> getOptionsMap() {
		Map<String, Boolean> map = new HashMap<>();
		boolean removePrivateFields = selectedClassMemberTypes.contains(PRIVATE_FIELDS);
		boolean removeProtectedFields = selectedClassMemberTypes.contains(PROTECTED_FIELDS);
		boolean removePublicFields = selectedClassMemberTypes.contains(PUBLIC_FIELDS);
		boolean removePackagePrivateFields = selectedClassMemberTypes.contains(PACKAGE_PRIVATE_FIELDS);
		map.put(Constants.PRIVATE_FIELDS, removePrivateFields);
		map.put(Constants.PROTECTED_FIELDS, removeProtectedFields);
		map.put(Constants.PUBLIC_FIELDS, removePublicFields);
		map.put(Constants.PACKAGE_PRIVATE_FIELDS, removePackagePrivateFields);

		boolean removePrivateMethods = selectedClassMemberTypes.contains(PRIVATE_METHODS);
		boolean removeProtectedMeMethods = selectedClassMemberTypes.contains(PROTECTED_METHODS);
		boolean removePublicMethods = selectedClassMemberTypes.contains(PUBLIC_METHODS);
		boolean removePackagePrivateMethods = selectedClassMemberTypes.contains(PACKAGE_PRIVATE_METHODS);
		map.put(Constants.PRIVATE_METHODS, removePrivateMethods);
		map.put(Constants.PROTECTED_METHODS, removeProtectedMeMethods);
		map.put(Constants.PUBLIC_METHODS, removePublicMethods);
		map.put(Constants.PACKAGE_PRIVATE_METHODS, removePackagePrivateMethods);

		boolean removeLocalClasses = selectedClassMemberTypes.contains(LOCAL_CLASSES);
		boolean removePrivateClasses = selectedClassMemberTypes.contains(PRIVATE_CLASSES);
		boolean removeProtectedClasses = selectedClassMemberTypes.contains(PROTECTED_CLASSES);
		boolean removePublicClasses = selectedClassMemberTypes.contains(PUBLIC_CLASSES);
		boolean removePackagePrivateClasses = selectedClassMemberTypes.contains(PACKAGE_PRIVATE_CLASSES);
		map.put(Constants.LOCAL_CLASSES, removeLocalClasses);
		map.put(Constants.PRIVATE_CLASSES, removePrivateClasses);
		map.put(Constants.PROTECTED_CLASSES, removeProtectedClasses);
		map.put(Constants.PUBLIC_CLASSES, removePublicClasses);
		map.put(Constants.PACKAGE_PRIVATE_CLASSES, removePackagePrivateClasses);

		map.put(Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS, removeInitializersWithSideEffects);
		map.put(Constants.REMOVE_TEST_CODE, removeTestCode);
		return map;
	}

	public String getSearchScope() {
		return this.searchScope;
	}
}

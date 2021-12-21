package eu.jsparrow.ui.markers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;

/**
 * A provider for available jSparrow marker IDs. Contains functionality to find
 * the markers that satisfy the rule requirements on a given compilation unit
 * and filtering markers based on the available credit.
 * 
 * @since 4.7.0
 *
 */
public class MarkerIdProvider {

	private Map<String, RuleDescription> allMarkerDescriptions;

	public MarkerIdProvider(Map<String, RuleDescription> allMarkerDescriptions) {
		this.allMarkerDescriptions = allMarkerDescriptions;
	}

	/**
	 * 
	 * @param compilationUnit
	 *            the compilation unit to be analyzed
	 * @return the active marker IDs that satisfy the rule requirements for the
	 *         given {@link ICompilationUnit}. The active markers are retrieved
	 *         from the preference store.
	 */
	public List<String> findAvailableFor(ICompilationUnit compilationUnit) {
		Map<String, RefactoringRule> resolverToRule = ResolverToRuleMap.get();
		List<String> satisfiedRequirementIds = findMarkersWithSatisfiedRequirements(compilationUnit, resolverToRule);
		return SimonykeesPreferenceManager.getAllActiveMarkers()
			.stream()
			.filter(satisfiedRequirementIds::contains)
			.collect(Collectors.toList());
	}

	/**
	 * 
	 * @param availableCredit
	 *            a credit threshold
	 * @param availableMarkers
	 *            the available markers
	 * @return a subset of the given markers whose defined credit is smaller
	 *         than the available credit.
	 */
	public List<String> filterWithSufficientCredit(int availableCredit,
			List<String> availableMarkers) {

		if (availableCredit <= 0) {
			return Collections.emptyList();
		}
		List<String> filtered = allMarkerDescriptions
			.entrySet()
			.stream()
			.filter(entry -> entry.getValue()
				.getCredit() <= availableCredit)
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());
		return availableMarkers.stream()
			.filter(filtered::contains)
			.collect(Collectors.toList());
	}

	private List<String> findMarkersWithSatisfiedRequirements(ICompilationUnit cu,
			Map<String, RefactoringRule> resolverToRule) {
		List<String> result = new ArrayList<>();
		IJavaProject project = cu.getJavaProject();
		if (project == null) {
			return Collections.emptyList();
		}
		for (Map.Entry<String, RefactoringRule> entry : resolverToRule.entrySet()) {
			String resolverId = entry.getKey();
			RefactoringRule rule = entry.getValue();
			rule.calculateEnabledForProject(project);
			if (rule.isEnabled()) {
				result.add(resolverId);
			}
		}
		return Collections.unmodifiableList(result);
	}
}

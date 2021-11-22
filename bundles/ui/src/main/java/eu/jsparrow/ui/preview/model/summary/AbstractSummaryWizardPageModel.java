package eu.jsparrow.ui.preview.model.summary;

import java.time.Duration;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.statistic.DurationFormatUtil;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.ui.preview.model.BaseModel;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;


public abstract class AbstractSummaryWizardPageModel extends BaseModel {

	protected final RefactoringPipeline refactoringPipeline;

	private IObservableList<RuleTimesModel> ruleTimes = new WritableList<>();

	private RefactoringPreviewWizardModel wizardModel;

	protected AbstractSummaryWizardPageModel(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel) {
		this.refactoringPipeline = refactoringPipeline;
		this.wizardModel = wizardModel;
		initialize();
	}

	public IObservableList<RuleTimesModel> getRuleTimes() {
		return ruleTimes;
	}

	// Needed because we don't have full databinding/models yet, so we need to
	// update the data manually :(
	public void updateData() {
		// Fields depend on contents of RuleTimes list, so we update that first!
		updateRuleTimes();
	}

	protected void initialize() {
		addRuleTimes();
	}

	private void addRuleTimes() {
		refactoringPipeline.getRules()
			.stream()
			.filter(rule -> RuleApplicationCount.getFor(rule)
				.toInt() > 0)
			.forEach(rule -> {
				String name = rule.getRuleDescription()
					.getName();
				int times = getApplicationTimesForRule(rule);
				Duration savedTime = EliminatedTechnicalDebt.get(rule, times);
				String timeSavedString = DurationFormatUtil.formatTimeSaved(savedTime);
				RuleTimesModel ruleTimesModel = new RuleTimesModel(name, times, timeSavedString);
				ruleTimesModel.setTimeSavedDuration(savedTime);
				ruleTimes.add(ruleTimesModel);
			});
	}

	private int getApplicationTimesForRule(RefactoringRule rule) {
		return RuleApplicationCount.getFor(rule)
			.getApplicationsForFiles(wizardModel.getFilesForRule(rule));
	}

	protected String getPathString(ICompilationUnit compilationUnit) {
		String temp = compilationUnit.getParent()
			.getPath()
			.toString();
		return StringUtils.startsWith(temp, "/") ? StringUtils.substring(temp, 1) : temp; //$NON-NLS-1$
	}

	protected boolean hasChanges(Entry<RefactoringState, String> entry) {
		RefactoringState state = entry.getKey();
		// Filter out those refactoring states that were deselected or
		// have no changes present.
		if (!state.hasChange()) {
			return false;
		}
		boolean allRulesIgnored = refactoringPipeline.getRules()
			.stream()
			.allMatch(rule -> state.getIgnoredRules()
				.contains(rule));
		if (allRulesIgnored) {
			return false;
		}
		boolean noChangePresent = refactoringPipeline.getRules()
			.stream()
			.allMatch(rule -> null == state.getChangeIfPresent(rule));
		return !noChangePresent;
	}

	private void updateRuleTimes() {
		ruleTimes.clear();
		addRuleTimes();
	}

	public RefactoringPipeline getRefactoringPipeline() {
		return refactoringPipeline;
	}

	/**
	 * 
	 * @return an array of all the rules and the file names in the summary page.
	 */
	public abstract String[] getProposalProviderContents();

}

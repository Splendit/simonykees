package eu.jsparrow.ui.preview.model.summary;

import java.time.Duration;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.statistic.DurationFormatUtil;
import eu.jsparrow.core.statistic.StopWatchUtil;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.ui.preview.model.BaseModel;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.PayPerUseCreditCalculator;


public abstract class AbstractSummaryWizardPageModel extends BaseModel {

	protected final RefactoringPipeline refactoringPipeline;

	private Long runDuration;
	private Integer issuesFixed;
	private Duration timeSaved;
	private Integer requiredCredit;
	private Integer availableCredit;

	private IObservableList<RuleTimesModel> ruleTimes = new WritableList<>();

	private RefactoringPreviewWizardModel wizardModel;

	protected AbstractSummaryWizardPageModel(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel) {
		this.refactoringPipeline = refactoringPipeline;
		this.wizardModel = wizardModel;
		initialize();
	}

	public Long getRunDuration() {
		return runDuration;
	}

	public void setRunDuration(Long runDuration) {
		Long oldValue = this.runDuration;
		this.runDuration = runDuration;
		firePropertyChange("runDuration", oldValue, runDuration); //$NON-NLS-1$
	}

	public IObservableList<RuleTimesModel> getRuleTimes() {
		return ruleTimes;
	}

	public Integer getIssuesFixed() {
		return this.issuesFixed;
	}

	public Integer getRequiredCredit() {
		return this.requiredCredit;
	}
	
	public Integer getAvailableCredit() {
		return this.availableCredit;
	}

	public void setIssuesFixed(Integer issuesFixed) {
		Integer oldValue = this.issuesFixed;
		this.issuesFixed = issuesFixed;
		firePropertyChange("issuesFixed", oldValue, issuesFixed); //$NON-NLS-1$
	}
	
	public void setRequiredCredit(Integer requiredCredit) {
		Integer oldValue = this.requiredCredit;
		this.requiredCredit = requiredCredit;
		firePropertyChange("requiredCredit", oldValue, requiredCredit); //$NON-NLS-1$
	}
	
	public void setAvailableCredit(Integer availableCredit) {
		Integer oldValue = this.availableCredit;
		this.availableCredit = availableCredit;
		firePropertyChange("availableCredit", oldValue, availableCredit); //$NON-NLS-1$
	}

	public Duration getTimeSaved() {
		return timeSaved;
	}

	public void setTimeSaved(Duration timeSaved) {
		Duration oldValue = this.timeSaved;
		this.timeSaved = timeSaved;
		firePropertyChange("timeSaved", oldValue, timeSaved); //$NON-NLS-1$
	}

	// Needed because we don't have full databinding/models yet, so we need to
	// update the data manually :(
	public void updateData() {
		// Fields depend on contents of RuleTimes list, so we update that first!
		updateRuleTimes();
		updateTimeSaved();
		updateIssuesFixed();
		updateRequiredCredit();
		updateAvailableCredit();
	}

	protected void initialize() {

		addRuleTimes();

		/*
		 * Set initial values to something big so labels have enough size This
		 * is easiert hat resizing/layouting labels dynamically based on their
		 * contents
		 */
		setRunDuration(StopWatchUtil.getTime());
		setIssuesFixed(99999);
		setTimeSaved(Duration.ofSeconds(999999999));
		setRequiredCredit(999999);
		setAvailableCredit(999999);
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

	private void updateIssuesFixed() {
		int totalIssuesFixed = ruleTimes.stream()
			.mapToInt(RuleTimesModel::getTimes)
			.sum();
		setIssuesFixed(totalIssuesFixed);
	}
	
	private void updateRequiredCredit() {
		List<RefactoringRule> rules = refactoringPipeline.getRules();
		PayPerUseCreditCalculator calculator = new PayPerUseCreditCalculator();
		int credit = calculator.findTotalRequiredCredit(rules);
		setRequiredCredit(credit);
	}
	
	private void updateAvailableCredit() {
		LicenseUtil.get().getValidationResult().getCredit()
			.ifPresent(this::setAvailableCredit);
	}

	private void updateTimeSaved() {
		Duration totalTimeSaved = ruleTimes.stream()
			.map(RuleTimesModel::getTimeSavedDuration)
			.reduce(Duration.ZERO, Duration::plus);
		setTimeSaved(totalTimeSaved);
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

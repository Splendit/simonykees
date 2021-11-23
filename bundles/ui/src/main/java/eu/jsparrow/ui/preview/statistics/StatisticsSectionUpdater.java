package eu.jsparrow.ui.preview.statistics;

import java.time.Duration;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.ui.preview.model.PayPerUseRuleStatisticsSectionPageModel;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.preview.model.RuleStatisticsSectionPageModel;

/**
 * This is a helper class to update the statistics sections after selecting or un-selecting changes in the preview wizard.   
 *
 * @since 4.6.0
 */
public class StatisticsSectionUpdater {

	private StatisticsSection statisticsSection;
	private StatisticsSection summary;

	public StatisticsSectionUpdater(StatisticsSection statisticsSection, StatisticsSection summary) {
		this.statisticsSection = statisticsSection;
		this.summary = summary;
	}

	public void update(RuleStatisticsSection ruleStatisticsSection, RefactoringRule rule,
			RefactoringPreviewWizardModel wizardModel) {
		if (statisticsSection instanceof TotalPayPerUseStatisticsSection) {
			TotalPayPerUseStatisticsSection totalStatisticsSection = (TotalPayPerUseStatisticsSection) statisticsSection;
			TotalPayPerUseStatisticsSection totalSummary = (TotalPayPerUseStatisticsSection) summary;
			updateIssuesTimeSavedAndCreditForSelected(totalStatisticsSection, totalSummary, ruleStatisticsSection, rule,
					wizardModel);
		} else {
			updateIssuesAndTimeSavedForSelected(ruleStatisticsSection, rule, wizardModel);
		}

	}

	private void updateIssuesAndTimeSavedForSelected(RuleStatisticsSection ruleStatisticsSection,
			RefactoringRule rule,
			RefactoringPreviewWizardModel wizardModel) {
		RuleStatisticsSectionPageModel model = ruleStatisticsSection.getModel();
		int timesApplied = RuleApplicationCount.getFor(rule)
			.getApplicationsForFiles(wizardModel.getFilesForRule(rule));
		model.setIssuesFixed(timesApplied);

		Duration timeSaved = rule.getRuleDescription()
			.getRemediationCost()
			.multipliedBy(timesApplied);
		model.setTimeSaved(timeSaved);
	}

	private void updateIssuesTimeSavedAndCreditForSelected(
			TotalPayPerUseStatisticsSection totalStatisticsSection,
			TotalPayPerUseStatisticsSection summary,
			RuleStatisticsSection ruleStatisticsSection,
			RefactoringRule rule,
			RefactoringPreviewWizardModel wizardModel) {
		PayPerUseRuleStatisticsSectionPageModel model = (PayPerUseRuleStatisticsSectionPageModel) ruleStatisticsSection
			.getModel();
		int timesApplied = RuleApplicationCount.getFor(rule)
			.getApplicationsForFiles(wizardModel.getFilesForRule(rule));
		int deltaTimesApplied = model.getIssuesFixed() - timesApplied;
		Duration timeSaved = rule.getRuleDescription()
			.getRemediationCost()
			.multipliedBy(timesApplied);
		Duration deltaTimeSaved = model.getTimeSaved()
			.minus(timeSaved);
		updateIssuesAndTimeSavedForSelected(ruleStatisticsSection, rule, wizardModel);
		int deltaCredit = deltaTimesApplied * rule.getRuleDescription()
			.getCredit();
		int newCredit = model.getRequiredCredit() - deltaCredit;
		model.setRequiredCredit(newCredit);
		totalStatisticsSection.updateForSelected(deltaTimesApplied, deltaTimeSaved, deltaCredit);
		summary.updateForSelected();
	}
}

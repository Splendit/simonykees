package eu.jsparrow.ui.preview.statistics;

import java.time.Duration;
import java.util.List;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.statistic.StopWatchUtil;
import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.ui.preview.model.PayPerUseRuleStatisticsSectionPageModel;
import eu.jsparrow.ui.preview.model.RuleStatisticsSectionPageModel;
import eu.jsparrow.ui.preview.model.StatisticsSectionPageModel;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.PayPerUseCreditCalculator;

public class StatisticsSectionFactory {
	
	private StatisticsSectionFactory() {
		/*
		 * Hide default constructor
		 */
	}
	
	public static StatisticsSection createStatisticsSection(RefactoringPipeline refactoringPipeline) {
		LicenseUtil licenseUtil = LicenseUtil.get();
		LicenseValidationResult validationResult = licenseUtil.getValidationResult();
		LicenseType licenseType = validationResult.getLicenseType();
		if(licenseType == LicenseType.PAY_PER_USE) {
			return new TotalPayPerUseStatisticsSection(refactoringPipeline, StatisticsSectionFactory.createStatisticsSectionModel(refactoringPipeline.getRules()));
		} else {
			return new EmptyStatisticsSection();
		}
	}
	
	public static StatisticsSection createStatisticsSectionForSummaryPage(RefactoringPipeline refactoringPipeline) {
		LicenseUtil licenseUtil = LicenseUtil.get();
		LicenseValidationResult validationResult = licenseUtil.getValidationResult();
		LicenseType licenseType = validationResult.getLicenseType();
		if(licenseType == LicenseType.PAY_PER_USE) {
			return new TotalPayPerUseStatisticsSection(refactoringPipeline, StatisticsSectionFactory.createStatisticsSectionModel(refactoringPipeline.getRules()));
		} else {
			return new MinimalStatisticsSection(refactoringPipeline, StatisticsSectionFactory.createStatisticsSectionModel(refactoringPipeline.getRules()));
		}
	}

	public static StatisticsSectionPageModel createStatisticsSectionModel(List<RefactoringRule> allRules) {
		LicenseUtil licenseUtil = LicenseUtil.get();
		LicenseValidationResult validationResult = licenseUtil.getValidationResult();
		LicenseType licenseType = validationResult.getLicenseType();
		int issuesFixedCount = allRules.stream()
				.map(RuleApplicationCount::getFor)
				.mapToInt(RuleApplicationCount::toInt)
				.sum();
		Duration timeSaved = allRules.stream()
				.map(EliminatedTechnicalDebt::get)
				.reduce(Duration.ZERO, Duration::plus);
		if(licenseType == LicenseType.PAY_PER_USE) {
			Integer totalAvailable = validationResult.getCredit().orElse(0);
			Long runDuration = StopWatchUtil.getTime();
			PayPerUseCreditCalculator calculator = new PayPerUseCreditCalculator();
			int totalRequired = calculator.findTotalRequiredCredit(allRules);
			return new StatisticsSectionPageModel(runDuration, issuesFixedCount, timeSaved, totalRequired, totalAvailable);
		} else {
			Long runDuration = StopWatchUtil.getTime();
			return new StatisticsSectionPageModel(runDuration, issuesFixedCount, timeSaved, 0, Integer.MAX_VALUE);
		}

		
	}
	
	public static RuleStatisticsSection createRuleStatisticsSection(RefactoringRule rule) {
		LicenseUtil licenseUtil = LicenseUtil.get();
		LicenseValidationResult validatoinResult = licenseUtil.getValidationResult();
		LicenseType licenseType = validatoinResult.getLicenseType();
		if(licenseType == LicenseType.PAY_PER_USE) {
			PayPerUseRuleStatisticsSectionPageModel model = new PayPerUseRuleStatisticsSectionPageModel(rule);
			return  new PayPerUseRuleStatisticsSection(model);
			
		} else {
			RuleStatisticsSectionPageModel model = new RuleStatisticsSectionPageModel(rule);
			return new RuleStatisticsSection(model);
		}		
	}

}

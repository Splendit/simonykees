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
import eu.jsparrow.ui.preview.model.PayPerUseRuleStatisticsArePageModel;
import eu.jsparrow.ui.preview.model.RuleStatisticsAreaPageModel;
import eu.jsparrow.ui.preview.model.StatisticsAreaPageModel;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.PayPerUseCreditCalculator;

public class StatisticsAreaFactory {
	
	private StatisticsAreaFactory() {
		/*
		 * Hide default constructor
		 */
	}
	
	public static StatisticsSection createStatisticsArea(RefactoringPipeline refactoringPipeline) {
		LicenseUtil licenseUtil = LicenseUtil.get();
		LicenseValidationResult validationResult = licenseUtil.getValidationResult();
		LicenseType licenseType = validationResult.getLicenseType();
		if(licenseType == LicenseType.PAY_PER_USE) {
			return new TotalPayPerUseStatisticsArea(refactoringPipeline, StatisticsAreaFactory.createStatisticsAreaModel(refactoringPipeline.getRules()));
		} else {
			return new EmptyStatisticsArea();
		}
	}
	
	public static StatisticsSection createStatisticsAreaForSummaryPage(RefactoringPipeline refactoringPipeline) {
		LicenseUtil licenseUtil = LicenseUtil.get();
		LicenseValidationResult validationResult = licenseUtil.getValidationResult();
		LicenseType licenseType = validationResult.getLicenseType();
		if(licenseType == LicenseType.PAY_PER_USE) {
			return new TotalPayPerUseStatisticsArea(refactoringPipeline, StatisticsAreaFactory.createStatisticsAreaModel(refactoringPipeline.getRules()));
		} else {
			return new MinimalStatisticsSection(refactoringPipeline, StatisticsAreaFactory.createStatisticsAreaModel(refactoringPipeline.getRules()));
		}
	}

	public static StatisticsAreaPageModel createStatisticsAreaModel(List<RefactoringRule> allRules) {
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
			return new StatisticsAreaPageModel(runDuration, issuesFixedCount, timeSaved, totalRequired, totalAvailable);
		} else {
			Long runDuration = StopWatchUtil.getTime();
			return new StatisticsAreaPageModel(runDuration, issuesFixedCount, timeSaved, 0, Integer.MAX_VALUE);
		}

		
	}
	
	public static RuleStatisticsArea createRuleStatisticsArea(RefactoringRule rule) {
		LicenseUtil licenseUtil = LicenseUtil.get();
		LicenseValidationResult validatoinResult = licenseUtil.getValidationResult();
		LicenseType licenseType = validatoinResult.getLicenseType();
		if(licenseType == LicenseType.PAY_PER_USE) {
			PayPerUseRuleStatisticsArePageModel model = new PayPerUseRuleStatisticsArePageModel(rule);
			PayPerUseRuleStatisticsArea ruleStatisticsArea =  new PayPerUseRuleStatisticsArea(model);
			return ruleStatisticsArea;
			
		} else {
			RuleStatisticsAreaPageModel model = new RuleStatisticsAreaPageModel(rule);
			RuleStatisticsArea ruleStatisticsArea = new RuleStatisticsArea(model);
			return ruleStatisticsArea;
		}		
	}

}

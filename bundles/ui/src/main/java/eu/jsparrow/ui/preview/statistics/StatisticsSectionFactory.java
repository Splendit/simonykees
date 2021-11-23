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

/**
 * A factory for creating instances of {@link StatisticsSection} based on the
 * current license type.
 * 
 * @since 4.6.0
 */
public class StatisticsSectionFactory {

	private StatisticsSectionFactory() {
		/*
		 * Hide default constructor
		 */
	}

	/**
	 * Creates an instance of {@link TotalPayPerUseStatisticsSection} if the
	 * license type is {@link LicenseType#PAY_PER_USE} or an
	 * {@link EmptyStatisticsSection} otherwise.
	 * 
	 * @param refactoringPipeline
	 * @return
	 */
	public static StatisticsSection createStatisticsSection(RefactoringPipeline refactoringPipeline) {
		LicenseUtil licenseUtil = LicenseUtil.get();
		LicenseValidationResult validationResult = licenseUtil.getValidationResult();
		LicenseType licenseType = validationResult.getLicenseType();
		if (licenseType == LicenseType.PAY_PER_USE) {
			return new TotalPayPerUseStatisticsSection(refactoringPipeline,
					StatisticsSectionFactory.createStatisticsSectionModel(refactoringPipeline.getRules()));
		} else {
			return new EmptyStatisticsSection();
		}
	}

	/**
	 * Creates an instance of {@link TotalPayPerUseStatisticsSection} if the
	 * license type is {@link LicenseType#PAY_PER_USE} or an
	 * {@link MinimalStatisticsSection} otherwise.
	 * 
	 * @param refactoringPipeline
	 * @return
	 */
	public static StatisticsSection createStatisticsSectionForSummaryPage(RefactoringPipeline refactoringPipeline) {
		LicenseUtil licenseUtil = LicenseUtil.get();
		LicenseValidationResult validationResult = licenseUtil.getValidationResult();
		LicenseType licenseType = validationResult.getLicenseType();
		if (licenseType == LicenseType.PAY_PER_USE) {
			return new TotalPayPerUseStatisticsSection(refactoringPipeline,
					StatisticsSectionFactory.createStatisticsSectionModel(refactoringPipeline.getRules()));
		} else {
			return new MinimalStatisticsSection(refactoringPipeline,
					StatisticsSectionFactory.createStatisticsSectionModel(refactoringPipeline.getRules()));
		}
	}

	private static StatisticsSectionPageModel createStatisticsSectionModel(List<RefactoringRule> allRules) {
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
		if (licenseType == LicenseType.PAY_PER_USE) {
			Integer totalAvailable = validationResult.getCredit()
				.orElse(0);
			Long runDuration = StopWatchUtil.getTime();
			PayPerUseCreditCalculator calculator = new PayPerUseCreditCalculator();
			int totalRequired = calculator.findTotalRequiredCredit(allRules);
			return new StatisticsSectionPageModel(runDuration, issuesFixedCount, timeSaved, totalRequired,
					totalAvailable);
		} else {
			Long runDuration = StopWatchUtil.getTime();
			return new StatisticsSectionPageModel(runDuration, issuesFixedCount, timeSaved, 0, Integer.MAX_VALUE);
		}

	}

	/**
	 * Creates an instance of {@link PayPerUseRuleStatisticsSection} if the
	 * license type is {@link LicenseType#PAY_PER_USE} or an
	 * {@link RuleStatisticsSection} otherwise.
	 * 
	 * @param rule
	 * @return
	 */
	public static RuleStatisticsSection createRuleStatisticsSection(RefactoringRule rule) {
		LicenseUtil licenseUtil = LicenseUtil.get();
		LicenseValidationResult validatoinResult = licenseUtil.getValidationResult();
		LicenseType licenseType = validatoinResult.getLicenseType();
		if (licenseType == LicenseType.PAY_PER_USE) {
			PayPerUseRuleStatisticsSectionPageModel model = new PayPerUseRuleStatisticsSectionPageModel(rule);
			return new PayPerUseRuleStatisticsSection(model);

		} else {
			RuleStatisticsSectionPageModel model = new RuleStatisticsSectionPageModel(rule);
			return new RuleStatisticsSection(model);
		}
	}

}

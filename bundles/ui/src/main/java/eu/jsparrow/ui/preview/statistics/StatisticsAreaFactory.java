package eu.jsparrow.ui.preview.statistics;

import java.time.Duration;
import java.util.List;

import eu.jsparrow.core.statistic.StopWatchUtil;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.ui.preview.model.StatisticsAreaPageModel;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.PayPerUseCreditCalculator;

public class StatisticsAreaFactory {
	
	private StatisticsAreaFactory() {
		/*
		 * Hide default constructor
		 */
	}
	
	public static StatisticsAreaPageModel createStatisticsAreaModel(List<RefactoringRule> allRules) {
		Long runDuration = StopWatchUtil.getTime();
		int issuesFixedCount = allRules.stream()
				.map(RuleApplicationCount::getFor)
				.mapToInt(RuleApplicationCount::toInt)
				.sum();
		Duration timeSaved = allRules.stream()
				.map(EliminatedTechnicalDebt::get)
				.reduce(Duration.ZERO, Duration::plus);
		
		PayPerUseCreditCalculator calculator = new PayPerUseCreditCalculator();
		int totalRequired = calculator.findTotalRequiredCredit(allRules);
		
		Integer totalAvailable = LicenseUtil.get().getValidationResult().getCredit().get(); //FIXME
		return new StatisticsAreaPageModel(runDuration, issuesFixedCount, timeSaved, totalRequired, totalAvailable);
		
	}

}

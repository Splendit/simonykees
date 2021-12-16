package eu.jsparrow.ui.preview.statistics;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.statistic.DurationFormatUtil;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.ui.preview.model.StatisticsSectionPageModel;
import eu.jsparrow.ui.util.ResourceHelper;

/**
 * Creates the overall statistics section for the select rules wizard. Usually,
 * these statistics are shown in the summary page. Does not include statistics
 * for Pay-Per-Use license model.
 * 
 * @since 4.6.0
 */
public class MinimalStatisticsSection implements StatisticsSection {

	private RefactoringPipeline refactoringPipeline;
	private CLabel totalExecutionTime;
	private CLabel totalIssuesFixed;
	private CLabel totalHoursSaved;

	private StatisticsSectionPageModel model;

	public MinimalStatisticsSection(RefactoringPipeline refactoringPipeline, StatisticsSectionPageModel model) {
		this.model = model;
		this.refactoringPipeline = refactoringPipeline;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void initializeDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		IConverter convertRunDuration = IConverter.create(Long.class, String.class,
				x -> String.format(Messages.MinimalStatisticsSection_runDuration,
						DurationFormatUtil.formatRunDuration((Long) x)));
		IObservableValue<String> observeTextLabelRunDurationObserveWidget = WidgetProperties.text()
			.observe(totalExecutionTime);
		IObservableValue<Object> runDurationWizardPageModelObserveValue = BeanProperties.value("runDuration") //$NON-NLS-1$
			.observe(model);
		bindingContext.bindValue(observeTextLabelRunDurationObserveWidget,
				runDurationWizardPageModelObserveValue, null, UpdateValueStrategy.create(convertRunDuration));

		IConverter convertTotalIssuesFixed = IConverter.create(Integer.class, String.class,
				x -> (String.format(Messages.MinimalStatisticsSection_totalIssuesFixed, (Integer) x)));
		ISWTObservableValue observeTextLabelIssuesFixedObserveWidget = WidgetProperties.text()
			.observe(totalIssuesFixed);
		IObservableValue<Object> issuesFixedSummaryWizardPageModelObserveValue = BeanProperties
			.value("totalIssuesFixed") //$NON-NLS-1$
			.observe(model);
		bindingContext.bindValue(observeTextLabelIssuesFixedObserveWidget,
				issuesFixedSummaryWizardPageModelObserveValue, null,
				UpdateValueStrategy.create(convertTotalIssuesFixed));

		IConverter convertTotalTimeSaved = IConverter.create(Duration.class, String.class, x -> String
			.format(Messages.MinimalStatisticsSection_totalTimeSaved,
					DurationFormatUtil.formatTimeSaved((Duration) x)));
		ISWTObservableValue observeTextLabelHoursSavedObserveWidget = WidgetProperties.text()
			.observe(totalHoursSaved);
		IObservableValue<Object> hoursSavedSummaryWizardPageModelObserveValue = BeanProperties.value("totalTimeSaved") //$NON-NLS-1$
			.observe(model);
		bindingContext.bindValue(observeTextLabelHoursSavedObserveWidget, hoursSavedSummaryWizardPageModelObserveValue,
				null, UpdateValueStrategy.create(convertTotalTimeSaved));
	}

	@Override
	public List<Image> createView(Composite rootComposite) {
		Composite composite = new Composite(rootComposite, SWT.NONE);
		GridLayout layout = new GridLayout(3, true);
		layout.marginHeight = 0;
		layout.marginWidth = 10;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

		totalExecutionTime = new CLabel(composite, SWT.NONE);
		totalExecutionTime.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		totalExecutionTime.setImage(ResourceHelper.createImage("icons/fa-hourglass-half-002.png"));//$NON-NLS-1$

		totalIssuesFixed = new CLabel(composite, SWT.NONE);
		totalIssuesFixed.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		totalIssuesFixed.setImage(ResourceHelper.createImage("icons/fa-bolt-002.png"));//$NON-NLS-1$

		totalHoursSaved = new CLabel(composite, SWT.NONE);
		totalHoursSaved.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		totalHoursSaved.setImage(ResourceHelper.createImage("icons/fa-clock-002.png"));//$NON-NLS-1$

		Label label = new Label(rootComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return Arrays.asList(totalExecutionTime.getImage(), totalIssuesFixed.getImage(), totalHoursSaved.getImage());
	}

	@Override
	public void updateForSelected() {
		List<RefactoringRule> allRules = refactoringPipeline.getRules();
		int issuesFixedCount = allRules.stream()
			.map(RuleApplicationCount::getFor)
			.mapToInt(RuleApplicationCount::toInt)
			.sum();
		Duration timeSaved = allRules.stream()
			.map(EliminatedTechnicalDebt::get)
			.reduce(Duration.ZERO, Duration::plus);
		model.setTotalIssuesFixed(issuesFixedCount);
		model.setTotalTimeSaved(timeSaved);
	}
}

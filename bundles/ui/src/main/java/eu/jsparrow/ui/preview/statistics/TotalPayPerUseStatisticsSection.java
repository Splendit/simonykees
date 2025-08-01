package eu.jsparrow.ui.preview.statistics;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.core.statistic.DurationFormatUtil;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.preview.model.StatisticsSectionPageModel;
import eu.jsparrow.ui.util.PayPerUseCreditCalculator;
import eu.jsparrow.ui.util.ResourceHelper;

/**
 * Creates the overall statistics section for the select rules wizard. Usually,
 * these statistics are shown in the summary page. In addition to
 * {@link MinimalStatisticsSection}, this also includes UI components for
 * displaying statistics related to Pay-Per-Use license model.
 * 
 * @since 4.6.0
 *
 */
public class TotalPayPerUseStatisticsSection implements StatisticsSection {

	private RefactoringPipeline refactoringPipeline;
	private CLabel totalExecutionTime;
	private CLabel totalIssuesFixed;
	private CLabel totalHoursSaved;
	private CLabel totalRequiredCredit;
	private CLabel availableCredit;


	private StatisticsSectionPageModel model;

	public TotalPayPerUseStatisticsSection(RefactoringPipeline refactoringPipeline, StatisticsSectionPageModel model) {
		this.model = model;
		this.refactoringPipeline = refactoringPipeline;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void initializeDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		IConverter convertRunDuration = IConverter.create(Long.class, String.class,
				x -> String.format(Messages.TotalPayPerUseStatisticsSection_runDuration,
						DurationFormatUtil.formatRunDuration((Long) x)));
		IObservableValue<String> observeTextLabelRunDurationObserveWidget = WidgetProperties.text()
			.observe(totalExecutionTime);
		IObservableValue<Object> runDurationWizardPageModelObserveValue = BeanProperties.value("runDuration") //$NON-NLS-1$
			.observe(model);
		bindingContext.bindValue(observeTextLabelRunDurationObserveWidget,
				runDurationWizardPageModelObserveValue, null, UpdateValueStrategy.create(convertRunDuration));

		IConverter convertTotalIssuesFixed = IConverter.create(Integer.class, String.class,
				x -> (String.format(Messages.TotalPayPerUseStatisticsSection_totalIssuesFixed, (Integer) x)));
		ISWTObservableValue observeTextLabelIssuesFixedObserveWidget = WidgetProperties.text()
			.observe(totalIssuesFixed);
		IObservableValue<Object> issuesFixedSummaryWizardPageModelObserveValue = BeanProperties
			.value("totalIssuesFixed") //$NON-NLS-1$
			.observe(model);
		bindingContext.bindValue(observeTextLabelIssuesFixedObserveWidget,
				issuesFixedSummaryWizardPageModelObserveValue, null,
				UpdateValueStrategy.create(convertTotalIssuesFixed));

		IConverter convertTotalTimeSaved = IConverter.create(Duration.class, String.class, x -> String
			.format(Messages.TotalPayPerUseStatisticsSection_totalTimeSaved,
					DurationFormatUtil.formatTimeSaved((Duration) x)));
		ISWTObservableValue observeTextLabelHoursSavedObserveWidget = WidgetProperties.text()
			.observe(totalHoursSaved);
		IObservableValue<Object> hoursSavedSummaryWizardPageModelObserveValue = BeanProperties.value("totalTimeSaved") //$NON-NLS-1$
			.observe(model);
		bindingContext.bindValue(observeTextLabelHoursSavedObserveWidget, hoursSavedSummaryWizardPageModelObserveValue,
				null, UpdateValueStrategy.create(convertTotalTimeSaved));

		IConverter converterRequiredCredit = IConverter.create(Integer.class, String.class,
				x -> (String.format(Messages.TotalPayPerUseStatisticsSection_requiredCredit, (Integer) x)));
		ISWTObservableValue observeTextLabelRequiredCreditObserveWidget = WidgetProperties.text()
			.observe(totalRequiredCredit);
		IObservableValue<Object> requiredCreditPageModelObserveValue = BeanProperties.value("totalRequiredCredit") //$NON-NLS-1$
			.observe(model);
		bindingContext.bindValue(observeTextLabelRequiredCreditObserveWidget,
				requiredCreditPageModelObserveValue, null, UpdateValueStrategy.create(converterRequiredCredit));

		IConverter convertAvailableCredit = IConverter.create(Integer.class, String.class,
				x -> String.format(Messages.TotalPayPerUseStatisticsSection_availableCredit, x));
		IObservableValue availableCreditLabelObserveValue = WidgetProperties.text()
			.observe(availableCredit);
		IObservableValue availableCreditModelObserveValue = BeanProperties.value("availableCredit") //$NON-NLS-1$
			.observe(model);
		bindingContext.bindValue(availableCreditLabelObserveValue, availableCreditModelObserveValue, null,
				UpdateValueStrategy.create(convertAvailableCredit));
	}

	@Override
	public List<Image> createView(Composite rootComposite) {
		Composite composite = new Composite(rootComposite, SWT.NONE);
		GridLayout layout = new GridLayout(5, true);
		layout.marginHeight = 0;
		layout.marginWidth = 10;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

		totalExecutionTime = new CLabel(composite, SWT.NONE);
		totalExecutionTime.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		Image totalExecutionTimeImage = ResourceHelper.createImage("icons/fa-hourglass-half-002.png"); //$NON-NLS-1$
		totalExecutionTime.setImage(totalExecutionTimeImage);

		totalIssuesFixed = new CLabel(composite, SWT.NONE);
		totalIssuesFixed.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		Image totalIssuesFixedImage = ResourceHelper.createImage("icons/fa-bolt-002.png"); //$NON-NLS-1$
		totalIssuesFixed.setImage(totalIssuesFixedImage);

		totalHoursSaved = new CLabel(composite, SWT.NONE);
		totalHoursSaved.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		Image totalHoursSavedImage = ResourceHelper.createImage("icons/fa-clock-002.png"); //$NON-NLS-1$
		totalHoursSaved.setImage(totalHoursSavedImage);

		totalRequiredCredit = new CLabel(composite, SWT.NONE);
		totalRequiredCredit.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		Image totalRequiredCreditImage = ResourceHelper.createImage("icons/jsparrow-coin.png"); //$NON-NLS-1$
		totalRequiredCredit.setImage(totalRequiredCreditImage);

		availableCredit = new CLabel(composite, SWT.NONE);
		availableCredit.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		Image availableCreditImage = ResourceHelper.createImage("icons/jsparrow-wallet.png"); //$NON-NLS-1$
		availableCredit.setImage(availableCreditImage);

		Label label = new Label(rootComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return Arrays.asList(totalExecutionTimeImage, totalIssuesFixedImage, totalHoursSavedImage, totalRequiredCreditImage, availableCreditImage);
	}

	@Override
	public void updateForSelected() {
		PayPerUseCreditCalculator calculator = new PayPerUseCreditCalculator();
		List<RefactoringRule> allRules = refactoringPipeline.getRules();
		int requiredCredit = calculator.findTotalRequiredCredit(allRules);
		int issuesFixedCount = allRules.stream()
			.map(RuleApplicationCount::getFor)
			.mapToInt(RuleApplicationCount::toInt)
			.sum();
		Duration timeSaved = allRules.stream()
			.map(EliminatedTechnicalDebt::get)
			.reduce(Duration.ZERO, Duration::plus);
		model.setTotalIssuesFixed(issuesFixedCount);
		model.setTotalTimeSaved(timeSaved);
		model.setTotalRequiredCredit(requiredCredit);
	}

	public void updateForSelected(int deltaTotalIssues, Duration deltaTimeSaved, int deltaRequiredCredit) {
		int newTotalIssues = model.getTotalIssuesFixed() - deltaTotalIssues;
		model.setTotalIssuesFixed(newTotalIssues);

		int newRequiredCredit = model.getTotalRequiredCredit() - deltaRequiredCredit;
		model.setTotalRequiredCredit(newRequiredCredit);

		Duration newSavedTime = model.getTotalTimeSaved()
			.minus(deltaTimeSaved);
		model.setTotalTimeSaved(newSavedTime);
	}
}

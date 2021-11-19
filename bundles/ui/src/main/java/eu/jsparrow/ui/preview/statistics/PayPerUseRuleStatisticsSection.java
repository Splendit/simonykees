package eu.jsparrow.ui.preview.statistics;

import java.time.Duration;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.ui.preview.model.PayPerUseRuleStatisticsSectionPageModel;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.util.ResourceHelper;

public class PayPerUseRuleStatisticsSection extends RuleStatisticsSection {

	private CLabel requiredCredit;
	private PayPerUseRuleStatisticsSectionPageModel payPerUseModel;
	private StatisticsSection statisticsSection;

	public PayPerUseRuleStatisticsSection(PayPerUseRuleStatisticsSectionPageModel model, StatisticsSection statisticsSection) {
		super(model);
		this.payPerUseModel = model;
		this.statisticsSection = statisticsSection;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void initializeDataBindings() {
		super.initializeDataBindings();

		IConverter convertRequiredCredit = IConverter.create(Integer.class, String.class,
				x -> String.format("Used credit: %s", x));
		IObservableValue requiredCreditLabelObserveValue = WidgetProperties.text()
			.observe(requiredCredit);
		IObservableValue requiredCreditModelObserveValue = BeanProperties.value("requiredCredit") //$NON-NLS-1$
			.observe(model);
		bindingContext.bindValue(requiredCreditLabelObserveValue, requiredCreditModelObserveValue, null,
				UpdateValueStrategy.create(convertRequiredCredit));
	}

	@Override
	public void createRuleRuleStatisticsView(Composite rootComposite) {
		Composite composite = new Composite(rootComposite, SWT.NONE);
		GridLayout layout = createGridLayout(3);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		super.initIssuesFixedLabel(composite);

		requiredCredit = new CLabel(composite, SWT.NONE);
		requiredCredit.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true));
		Image usedCreditImage = ResourceHelper.createImage("icons/fa-bolt.png"); //$NON-NLS-1$
		requiredCredit.setImage(usedCreditImage);

		super.initTechDebtLabel(composite);

		Label label = new Label(rootComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	@Override
	public void updateIssuesAndTimeForSelected(RefactoringRule rule, RefactoringPreviewWizardModel wizardModel) {
		int timesApplied = RuleApplicationCount.getFor(rule)
			.getApplicationsForFiles(wizardModel.getFilesForRule(rule));
		int deltaTimesApplied = model.getIssuesFixed() - timesApplied;
		Duration timeSaved = rule.getRuleDescription()
			.getRemediationCost()
			.multipliedBy(timesApplied);
		Duration deltaTimeSaved = model.getTimeSaved()
			.minus(timeSaved);
		super.updateIssuesAndTimeForSelected(rule, wizardModel);
		int deltaCredit = deltaTimesApplied * rule.getRuleDescription()
			.getCredit();
		int newCredit = payPerUseModel.getRequiredCredit() - deltaCredit;
		payPerUseModel.setRequiredCredit(newCredit);
		statisticsSection.updateForSelected(deltaTimesApplied, deltaTimeSaved, deltaCredit);
	}
}

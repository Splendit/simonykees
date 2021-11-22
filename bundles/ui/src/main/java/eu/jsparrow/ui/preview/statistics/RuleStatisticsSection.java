package eu.jsparrow.ui.preview.statistics;

import java.time.Duration;

import org.eclipse.core.databinding.DataBindingContext;
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

import eu.jsparrow.core.statistic.DurationFormatUtil;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.preview.model.RuleStatisticsSectionPageModel;
import eu.jsparrow.ui.util.ResourceHelper;

public class RuleStatisticsSection {
	
	protected DataBindingContext bindingContext = new DataBindingContext();
	private CLabel techDebtLabel;
	private CLabel issuesFixedLabel;
	
	protected RuleStatisticsSectionPageModel model;
	
	public RuleStatisticsSection(RuleStatisticsSectionPageModel model) {
		this.model = model;
	}

	protected GridLayout createGridLayout(int columns) {
		GridLayout layout = new GridLayout(columns, true);
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		return layout;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void initializeDataBindings() {

		IConverter convertIssuesFixed = IConverter.create(Integer.class, String.class,
				x -> (String.format(Messages.SummaryWizardPageModel_IssuesFixed, (Integer) x)));
		IObservableValue issuesFixedLabelObserveValue = WidgetProperties.text()
			.observe(issuesFixedLabel);
		IObservableValue issuesFixedModelObserveValue = BeanProperties.value("issuesFixed") //$NON-NLS-1$
			.observe(model);
		bindingContext.bindValue(issuesFixedLabelObserveValue, issuesFixedModelObserveValue, null,
				UpdateValueStrategy.create(convertIssuesFixed));

		IConverter convertTimeSaved = IConverter.create(Duration.class, String.class, x -> String
			.format(Messages.DurationFormatUtil_TimeSaved, DurationFormatUtil.formatTimeSaved((Duration) x)));
		IObservableValue hoursSavedLabelObserveValue = WidgetProperties.text()
			.observe(techDebtLabel);
		IObservableValue hoursSavedModelObserveValue = BeanProperties.value("timeSaved") //$NON-NLS-1$
			.observe(model);
		bindingContext.bindValue(hoursSavedLabelObserveValue, hoursSavedModelObserveValue, null,
				UpdateValueStrategy.create(convertTimeSaved));

	}

	
	public void createRuleRuleStatisticsView(Composite rootComposite) {
		Composite composite = new Composite(rootComposite, SWT.NONE);
		GridLayout layout = createGridLayout(2);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		initIssuesFixedLabel(composite);
		initTechDebtLabel(composite);
		Label label = new Label(rootComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected void initIssuesFixedLabel(Composite composite) {
		issuesFixedLabel = new CLabel(composite, SWT.NONE);
		issuesFixedLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		Image inLoveImage = ResourceHelper.createImage("icons/fa-bolt.png"); //$NON-NLS-1$
		issuesFixedLabel.setImage(inLoveImage);
	}
	
	
	protected void initTechDebtLabel(Composite composite) {
		techDebtLabel = new CLabel(composite, SWT.NONE);
		techDebtLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		techDebtLabel.setImage(ResourceHelper.createImage("icons/fa-clock.png"));//$NON-NLS-1$
	}
	
	public RuleStatisticsSectionPageModel getModel() {
		return this.model;
	}
}

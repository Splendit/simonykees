package eu.jsparrow.ui.preview.statistics;

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

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.preview.model.PayPerUseRuleStatisticsSectionPageModel;
import eu.jsparrow.ui.util.ResourceHelper;

/**
 * Adds statistics related to Pay-Per-Use license model to the statistics of one
 * single rule.
 * 
 * @since 4.6.0
 */
public class PayPerUseRuleStatisticsSection extends RuleStatisticsSection {

	private CLabel requiredCredit;

	public PayPerUseRuleStatisticsSection(PayPerUseRuleStatisticsSectionPageModel model) {
		super(model);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void initializeDataBindings() {
		super.initializeDataBindings();

		IConverter convertRequiredCredit = IConverter.create(Integer.class, String.class,
				x -> String.format(Messages.PayPerUseRuleStatisticsSection_usedCredit, x));
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
}

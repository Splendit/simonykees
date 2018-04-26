package eu.jsparrow.ui.wizard.impl;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.rules.common.RefactoringRule;

/**
 * Label provider for right view in select rules wizard
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class TableLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		RefactoringRule rule = (RefactoringRule) element;
		switch (columnIndex) {
		case 0:
			return rule.getRuleDescription().getName();
		case 1:
			return rule.getRuleDescription().getDescription();
		default:
			return ExceptionMessages.TableLabelProvider_not_supported;
		}

	}

}

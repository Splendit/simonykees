package at.splendit.simonykees.core.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;

public interface IWizardPageModel {

	public void addListener(IValueChangeListener listener);

	public void moveToRight(IStructuredSelection selectedElements);

	public void moveAllToRight();

	public void moveToLeft(IStructuredSelection selectedElements);

	public void moveAllToLeft();
	
	public void filterByGroup(final String filter);

}

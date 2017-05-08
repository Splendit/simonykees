package at.splendit.simonykees.core.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Interface for selection Wizard page model
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public interface IWizardPageModel {

	public void addListener(IValueChangeListener listener);

	public void moveToRight(IStructuredSelection selectedElements);

	public void moveAllToRight();

	public void moveToLeft(IStructuredSelection selectedElements);

	public void moveAllToLeft();
	
}

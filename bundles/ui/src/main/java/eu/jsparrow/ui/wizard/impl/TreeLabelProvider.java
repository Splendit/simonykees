package eu.jsparrow.ui.wizard.impl;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import eu.jsparrow.rules.common.RefactoringRule;

/**
 * Label provider for left view in select rules wizard
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class TreeLabelProvider extends LabelProvider implements IColorProvider {

	private ResourceManager resourceManager;

	@Override
	public String getText(Object element) {
		String s;
		if (element instanceof RefactoringRule) {
			s = ((RefactoringRule) element).getRuleDescription()
				.getName();
		} else {
			s = (String) element;
		}
		return s;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof RefactoringRule) {
			if (((RefactoringRule) element).isEnabled()) {
				// without icon
			} else {
				// info icon that rule is disabled, explanation appears in
				// description text when rule is clicked
				return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
			}
		}
		return super.getImage(element);
	}

	@Override
	public Color getForeground(Object element) {
		if (element instanceof RefactoringRule && !((RefactoringRule) element).isEnabled()) {
			return Display.getDefault()
				.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
		}

		return null;
	}

	@Override
	public Color getBackground(Object element) {
		if (element instanceof RefactoringRule && !((RefactoringRule) element).isEnabled()) {
			return Display.getDefault()
				.getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
		}
		return null;
	}

	@Override
	public void dispose() {
		// garbage collect system resources, images have to be manually disposed
		if (resourceManager != null) {
			resourceManager.dispose();
			resourceManager = null;
		}
	}

	protected ResourceManager getResourceManager() {
		if (resourceManager == null) {
			resourceManager = new LocalResourceManager(JFaceResources.getResources());
		}
		return resourceManager;
	}

}

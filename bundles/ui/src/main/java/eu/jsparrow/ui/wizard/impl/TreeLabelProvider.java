package eu.jsparrow.ui.wizard.impl;

import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.LicenseUtilService;

/**
 * Label provider for left view in select rules wizard
 * 
 * @since 1.2
 *
 */
public class TreeLabelProvider extends LabelProvider implements IColorProvider {

	private ResourceManager resourceManager;

	private Image tickmarkGreenIconImage;
	private Image greenFreeRuleImage;
	private Image tickmarkLockedRuleImage;
	private Image lockedRuleImage;

	private boolean freeLicense;

	private static final String F_GREEN_ICON_PATH = "icons/icon-check.png"; //$NON-NLS-1$
	private static final String ICON_LOCK = "icons/icon-lock.png"; //$NON-NLS-1$

	public TreeLabelProvider() {
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		IPath iPathTickMarkGreen = new Path(F_GREEN_ICON_PATH);
		URL urlTickMarkGreen = FileLocator.find(bundle, iPathTickMarkGreen, new HashMap<>());
		ImageDescriptor imageDescTickMarkGreen = ImageDescriptor.createFromURL(urlTickMarkGreen);
		tickmarkGreenIconImage = imageDescTickMarkGreen.createImage();
		ImageData imageDataTickmarkGreen = tickmarkGreenIconImage.getImageData();
		greenFreeRuleImage = new Image(Display.getCurrent(), imageDataTickmarkGreen);

		IPath iPathIconLock = new Path(ICON_LOCK);
		URL urlIconLock = FileLocator.find(bundle, iPathIconLock, new HashMap<>());
		ImageDescriptor imageDescLockIcon = ImageDescriptor.createFromURL(urlIconLock);
		tickmarkLockedRuleImage = imageDescLockIcon.createImage();
		ImageData imageDataIconLock = tickmarkLockedRuleImage.getImageData();
		lockedRuleImage = new Image(Display.getCurrent(), imageDataIconLock);

		LicenseUtilService licenseUtil = LicenseUtil.get();
		freeLicense = licenseUtil.isFreeLicense();
	}

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

			RefactoringRule rule = (RefactoringRule) element;
			if (!rule.isEnabled()) {
				// info icon that rule is disabled, explanation appears in
				// description text when rule is clicked
				return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
			}

			if (freeLicense && !rule.isFree()) {
				return lockedRuleImage;
			}

			return greenFreeRuleImage;
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
		greenFreeRuleImage.dispose();
		tickmarkGreenIconImage.dispose();
		lockedRuleImage.dispose();
		tickmarkLockedRuleImage.dispose();
	}

	protected ResourceManager getResourceManager() {
		if (resourceManager == null) {
			resourceManager = new LocalResourceManager(JFaceResources.getResources());
		}
		return resourceManager;
	}

}

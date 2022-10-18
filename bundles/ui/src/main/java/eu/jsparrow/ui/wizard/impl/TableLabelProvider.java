package eu.jsparrow.ui.wizard.impl;

import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.util.LicenseUtil;

/**
 * Label provider for right view in select rules wizard
 * 
 * @since 1.2
 *
 */
public class TableLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
	private Image tickmarkGreenIconImage;
	private Image greenFreeRuleImage;
	private Image tickmarkLockedRuleImage;
	private Image lockedRuleImage;
	private static final String ICON_CHECK = "icons/icon-check.png"; //$NON-NLS-1$
	private static final String ICON_LOCK = "icons/icon-lock.png"; //$NON-NLS-1$

	public TableLabelProvider() {
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		IPath iPathTickMarkGreen = new Path(ICON_CHECK);
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
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		RefactoringRule rule = (RefactoringRule) element;
		boolean freeLicense = LicenseUtil.get()
			.isFreeLicense();
		if (freeLicense) {
			if (rule.isFree()) {
				return greenFreeRuleImage;
			}
			return lockedRuleImage;
		} else {
			return null;
		}
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		RefactoringRule rule = (RefactoringRule) element;
		switch (columnIndex) {
		case 0:
			return rule.getRuleDescription()
				.getName();
		case 1:
			return rule.getRuleDescription()
				.getDescription();
		default:
			return ExceptionMessages.TableLabelProvider_not_supported;
		}

	}

	@Override
	public void dispose() {
		greenFreeRuleImage.dispose();
		tickmarkGreenIconImage.dispose();
		lockedRuleImage.dispose();
		tickmarkLockedRuleImage.dispose();
	}
}

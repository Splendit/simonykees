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

	private Image greenFreeRuleImage;
	private Image tickmarkGreenIconImage;
	private static final String F_GREEN_ICON_PATH = "icons/f-icon-green-14px.png"; //$NON-NLS-1$

	public TableLabelProvider() {
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		IPath iPathTickMarkGreen = new Path(F_GREEN_ICON_PATH);
		URL urlTickMarkGreen = FileLocator.find(bundle, iPathTickMarkGreen, new HashMap<>());
		ImageDescriptor imageDescTickMarkGreen = ImageDescriptor.createFromURL(urlTickMarkGreen);
		tickmarkGreenIconImage = imageDescTickMarkGreen.createImage();
		ImageData imageDataTickmarkGreen = tickmarkGreenIconImage.getImageData();
		greenFreeRuleImage = new Image(Display.getCurrent(), imageDataTickmarkGreen);
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		RefactoringRule rule = (RefactoringRule) element;
		if (rule.isFree() && LicenseUtil.get().isFreeLicense()) {
			return greenFreeRuleImage;
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
	}

}

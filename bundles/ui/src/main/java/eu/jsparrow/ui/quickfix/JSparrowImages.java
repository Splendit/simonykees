package eu.jsparrow.ui.quickfix;

import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import eu.jsparrow.ui.Activator;

public final class JSparrowImages {

	public static final Image JSPARROW_ACTIVE_16 = createImage("icons/jSparrow_active_icon_16.png"); //$NON-NLS-1$

	private JSparrowImages() {
		/*
		 * Hide default constructor.
		 */
	}

	private static ImageDescriptor createImageDescriptor(String string) {
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		IPath iPathActive = new Path(string);
		URL urlActive = FileLocator.find(bundle, iPathActive, new HashMap<>());
		if(urlActive == null) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
		ImageDescriptor imageDesc = ImageDescriptor.createFromURL(urlActive);
		return imageDesc;
	}

	public static Image createImage(String string) {
		ImageDescriptor imageDesc = createImageDescriptor(string);
		return imageDesc.createImage();
	}

}

package eu.jsparrow.ui.markers;

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

/**
 * A container with predefined jSparrow icons. 
 * 
 * @since 3.31.0
 */
public final class JSparrowImages {

	public static final Image JSPARROW_ACTIVE_16 = createImage("icons/jSparrow_bulb_bottomRight_icon_16.png"); //$NON-NLS-1$

	private JSparrowImages() {
		/*
		 * Hide default constructor.
		 */
	}

	private static ImageDescriptor createImageDescriptor(String string) {
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		IPath iPathActive = new Path(string);
		URL urlActive = FileLocator.find(bundle, iPathActive, new HashMap<>());
		if (urlActive == null) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
		return ImageDescriptor.createFromURL(urlActive);
	}

	public static Image createImage(String string) {
		ImageDescriptor imageDesc = createImageDescriptor(string);
		return imageDesc.createImage();
	}

}

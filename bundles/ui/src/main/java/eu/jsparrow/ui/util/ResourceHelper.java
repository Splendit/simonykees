package eu.jsparrow.ui.util;

import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import eu.jsparrow.ui.Activator;

public class ResourceHelper {

	private ResourceHelper() {
		// Hide implicit constructor
	}

	public static Image createImage(String path) {
		IPath imagePath = new Path(path);
		URL url = FileLocator.find(Platform.getBundle(Activator.PLUGIN_ID), imagePath, new HashMap<>());
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
		return imageDescriptor.createImage();
	}
}

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
	
	/** 
	 *  Generates URL for the {@link ruleId}. The camelcase word is split,
	 *  transposed to lowercase and concatinated with a dash (-)
	 * @param baseUrl base URL 
	 * @param ruleId name of the rule in Id format.
	 * @return
	 */
	public static String generateLinkToDocumentation(String baseUrl, String ruleId) {
		return baseUrl + String.join("-", ruleId.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) //$NON-NLS-1$ //$NON-NLS-2$
			.toLowerCase() + ".html"; //$NON-NLS-1$
	}

}

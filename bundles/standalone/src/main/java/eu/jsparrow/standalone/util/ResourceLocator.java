package eu.jsparrow.standalone.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.standalone.Activator;

/**
 * A utility for finding the resource paths in this bundle.
 * 
 * @since 3.23.0
 *
 */
public class ResourceLocator {

	private static final Logger logger = LoggerFactory.getLogger(ResourceLocator.class);

	private ResourceLocator() {
		/*
		 * Hide default constructor.
		 */
	}

	/**
	 * Finds the resource file in this bundle.
	 * 
	 * @param path
	 *            the path of the resource to search for.
	 * @return a {@link File} representing the found resource or an empty
	 *         {@link Optional} if the resource cannot be found.
	 */
	public static Optional<File> findFile(String path) {
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		IPath iPathReport = new Path(path);
		URL url = FileLocator.find(bundle, iPathReport, new HashMap<>());
		URL templateDirecotryUrl;
		try {
			templateDirecotryUrl = FileLocator.toFileURL(url);
		} catch (IOException e1) {
			logger.error("Cannot convert the bundle resource to File URL", e1); //$NON-NLS-1$
			return Optional.empty();
		}
		URI templateFolderURI;
		try {
			templateFolderURI = templateDirecotryUrl.toURI();
			File templateFolder = new File(templateFolderURI);
			return Optional.of(templateFolder);

		} catch (URISyntaxException e) {
			logger.error("Cannot convert bundle URL to file URI", e); //$NON-NLS-1$
		}

		return Optional.empty();
	}
}

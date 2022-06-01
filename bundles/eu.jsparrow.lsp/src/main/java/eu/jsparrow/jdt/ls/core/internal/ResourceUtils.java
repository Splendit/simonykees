package eu.jsparrow.jdt.ls.core.internal;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;

public class ResourceUtils {
	
	public static final String FILE_UNC_PREFIX = "file:////";

	public static String fixURI(URI uri) {
		if (uri == null) {
			return null;
		}
		if (Platform.OS_WIN32.equals(Platform.getOS()) && URIUtil.isFileURI(uri)) {
			uri = URIUtil.toFile(uri).toURI();
		}
		String uriString = uri.toString();
		return uriString.replaceFirst("file:/([^/])", "file:///$1");
	}

	public static String expandPath(String path) {
		if (path != null) {
			if (path.startsWith("~" + File.separator)) {
				path = System.getProperty("user.home") + path.substring(1);
			}
			StrLookup<String> variableResolver = new StrLookup<>() {

				@Override
				public String lookup(String key) {
					if (key.length() > 0) {
						try {
							String prop = System.getProperty(key);
							if (prop != null) {
								return prop;
							}
							return System.getenv(key);
						} catch (final SecurityException scex) {
							return null;
						}
					}
					return null;
				}
			};
			StrSubstitutor strSubstitutor = new StrSubstitutor(variableResolver);
			return strSubstitutor.replace(path);
		}
		return path;
	}

	public static List<IMarker> findMarkers(IResource resource, Integer... severities) throws CoreException {
		if (resource == null) {
			return null;
		}
		Set<Integer> targetSeverities = severities == null ? Collections.emptySet()
				: new HashSet<>(Arrays.asList(severities));
		IMarker[] allmarkers = resource.findMarkers(null /* all markers */, true /* subtypes */,
				IResource.DEPTH_INFINITE);
		List<IMarker> markers = Stream.of(allmarkers).filter(
				m -> targetSeverities.isEmpty() || targetSeverities.contains(m.getAttribute(IMarker.SEVERITY, 0)))
				.collect(Collectors.toList());
		return markers;
	}
	
	public static String toClientUri(String uri) {
		if (uri != null && Platform.OS_WIN32.equals(Platform.getOS()) && uri.startsWith(FILE_UNC_PREFIX)) {
			uri = uri.replace(FILE_UNC_PREFIX, "file://");
		}
		return uri;
	}

	public static IPath canonicalFilePathFromURI(String uriStr) {
		URI uri = URI.create(uriStr);
		if ("file".equals(uri.getScheme())) {
			return FileUtil.canonicalPath(Path.fromOSString(Paths.get(uri).toString()));
		}
		return null;
	}

	public static boolean isContainedIn(IPath location, Collection<IPath> paths) {
		if (location == null || paths == null || paths.isEmpty()) {
			return false;
		}
		for (IPath path : paths) {
			if (path.isPrefixOf(location)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Convert an {@link IPath} to a glob pattern (i.e. ending with /**)
	 *
	 * @param path
	 *            the path to convert
	 * @return a glob pattern prefixed with the path
	 */
	public static String toGlobPattern(IPath path) {
		if (path == null) {
			return null;
		}

		String baseName = path.lastSegment();
		return toGlobPattern(path, !baseName.endsWith(".jar") && !baseName.endsWith(".zip"));
	}
	
	/**
	 * Convert an {@link IPath} to a glob pattern.
	 *
	 * @param path
	 *            the path to convert
	 * @param recursive
	 *            whether to end the glob with "/**"
	 * @return a glob pattern prefixed with the path
	 */
	public static String toGlobPattern(IPath path, boolean recursive) {
		if (path == null) {
			return null;
		}

		String globPattern = path.toPortableString();
		if (path.getDevice() != null) {
			//This seems pretty hack-ish: need to remove device as it seems to break
			// file detection, at least on vscode
			globPattern = globPattern.replace(path.getDevice(), "**");
		}

		if (recursive) {
			File file = path.toFile();
			if (!file.isFile()) {
				if (!globPattern.endsWith("/")) {
					globPattern += "/";
				}
				globPattern += "**";
			}
		}

		return globPattern;
	}

	public static File toFile(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public static IPath filePathFromURI(String uriStr) {
		URI uri = URI.create(uriStr);
		if ("file".equals(uri.getScheme())) {
			return Path.fromOSString(Paths.get(uri).toString());
		}
		return null;
	}
}

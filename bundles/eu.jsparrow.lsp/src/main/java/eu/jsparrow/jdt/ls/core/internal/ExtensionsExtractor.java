package eu.jsparrow.jdt.ls.core.internal;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class ExtensionsExtractor {
	public static <T> List<T> extractOrderedExtensions(final String namespace, final String extensionPointName) {

		final var extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(namespace, extensionPointName);
		final var configs = extensionPoint.getConfigurationElements();

		Map<Integer, T> extensionMap = new TreeMap<>();

		for (int i = 0; i < configs.length; i++) {
				Integer order = Integer.valueOf(configs[i].getAttribute("order"));
				extensionMap.put(order, makeExtension(configs[i]));
		}
		return extensionMap.values().stream().collect(Collectors.toUnmodifiableList());
	}

	@SuppressWarnings("unchecked")
	private static <T> T makeExtension(IConfigurationElement config) {
		try {
			return (T) config.createExecutableExtension("class");

		} catch (Exception ex) {
			throw new IllegalArgumentException("Could not create the extension", ex);
		}
	}

}

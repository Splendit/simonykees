/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package eu.jsparrow.jdt.ls.internal.gradle.checksums;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import eu.jsparrow.jdt.ls.core.internal.IConstants;
import eu.jsparrow.jdt.ls.core.internal.JavaLanguageServerPlugin;
import eu.jsparrow.jdt.ls.core.internal.JobHelpers;
import org.osgi.framework.Bundle;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 *
 * @author snjeza
 *
 */
public class WrapperValidator {

	public static final String GRADLE_CHECKSUMS = "/gradle/checksums/checksums.json";
	public static final String GRADLE_VERSIONS = "/gradle/checksums/versions.json";
	private static final int QUEUE_LENGTH = 20;
	private static final String WRAPPER_CHECKSUM_URL = "wrapperChecksumUrl";
	private static final String GRADLE_WRAPPER_JAR = "gradle/wrapper/gradle-wrapper.jar";

	private static Set<String> allowed = new HashSet<>();
	private static Set<String> disallowed = new HashSet<>();
	private static Set<String> wrapperChecksumUrls = new HashSet<>();
	private static AtomicBoolean downloaded = new AtomicBoolean(false);
	private HashProvider hashProvider;
	private int queueLength;

	public WrapperValidator(int queueLength) {
		this.hashProvider = new HashProvider();
		this.queueLength = queueLength;
	}

	public WrapperValidator() {
		this(QUEUE_LENGTH);
	}

	private void loadInternalChecksums() {
		Bundle bundle = Platform.getBundle(IConstants.PLUGIN_ID);
		URL url = FileLocator.find(bundle, new org.eclipse.core.runtime.Path(GRADLE_CHECKSUMS));
		if (url != null) {
			try (InputStream inputStream = url.openStream(); InputStreamReader inputStreamReader = new InputStreamReader(inputStream); Reader reader = new BufferedReader(inputStreamReader)) {
				JsonElement jsonElement = new JsonParser().parse(reader);
				if (jsonElement instanceof JsonArray) {
					JsonArray array = (JsonArray) jsonElement;
					for (JsonElement json : array) {
						String sha256 = json.getAsJsonObject().get("sha256").getAsString();
						String wrapperChecksumUrl = json.getAsJsonObject().get("wrapperChecksumUrl").getAsString();
						if (sha256 != null) {
							allowed.add(sha256);
						}
						if (wrapperChecksumUrl != null) {
							wrapperChecksumUrls.add(wrapperChecksumUrl);
						}
					}
				}
			} catch (IOException e) {
				JavaLanguageServerPlugin.logException(e.getMessage(), e);
			}
		}
	}

	private void updateGradleVersionsFile() {
		File file = getVersionCacheFile();
		if (file.isFile()) {
			return;
		}
		file.getParentFile().mkdirs();
		Bundle bundle = Platform.getBundle(IConstants.PLUGIN_ID);
		URL url = FileLocator.find(bundle, new org.eclipse.core.runtime.Path(GRADLE_VERSIONS));
		if (url != null) {
			try (InputStream is = url.openStream(); InputStreamReader isr = new InputStreamReader(is); BufferedReader br = new BufferedReader(isr); FileOutputStream os = new FileOutputStream(file.getAbsolutePath())) {
				br.lines().forEach(l -> {
					try {
						os.write(l.getBytes(StandardCharsets.UTF_8));
						os.write("\n".getBytes(StandardCharsets.UTF_8));
					} catch (IOException e) {
						JavaLanguageServerPlugin.logException(e.getMessage(), e);
					}
				});
			} catch (IOException e) {
				JavaLanguageServerPlugin.logException(e.getMessage(), e);
			}
		}
	}

	public static String getFileName(String url) {
		int index = url.lastIndexOf("/");
		if (index < 0 || url.length() < index + 1) {
			JavaLanguageServerPlugin.logInfo("Invalid wrapper URL " + url);
			return null;
		}
		return url.substring(index + 1);
	}

	private static String read(File file) {
		Optional<String> firstLine;
		try {
			firstLine = Files.lines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8).findFirst();
		} catch (IOException e) {
			JavaLanguageServerPlugin.logException(e);
			return null;
		}
		if (firstLine.isPresent()) {
			return firstLine.get();
		}
		return null;
	}

	private static File getVersionCacheFile() {
		String xdgCache = getXdgCache();
		return new File(xdgCache, "tooling/gradle/versions.json");
	}

	private static String getXdgCache() {
		String xdgCache = System.getenv("XDG_CACHE_HOME");
		if (xdgCache == null) {
			xdgCache = System.getProperty("user.home") + "/.cache/";
		}
		return xdgCache;
	}

	public static File getSha256CacheFile() {
		String checksumCache = System.getProperty("gradle.checksum.cacheDir");
		File file;
		if (checksumCache == null || checksumCache.isEmpty()) {
			String xdgCache = getXdgCache();
			file = new File(xdgCache, "tooling/gradle/checksums");
		} else {
			file = new File(checksumCache);
		}
		file.mkdirs();
		return file;
	}

	public static void clear() {
		allowed.clear();
		disallowed.clear();
		wrapperChecksumUrls.clear();
	}

	public static void allow(Collection<String> c) {
		allowed.addAll(c);
	}

	public static void allow(String checksum) {
		allowed.add(checksum);
	}

	public static void disallow(Collection<String> c) {
		disallowed.addAll(c);
	}

	public static boolean contains(String checksum) {
		return disallowed.contains(checksum);
	}

	public static int size() {
		return allowed.size() + disallowed.size();
	}

	public static Set<String> getAllowed() {
		return Collections.unmodifiableSet(allowed);
	}

	public static Set<String> getDisallowed() {
		return Collections.unmodifiableSet(allowed);
	}

	public static void putSha256(List<?> gradleWrapperList) {
		List<String> sha256Allowed = new ArrayList<>();
		List<String> sha256Disallowed = new ArrayList<>();
		for (Object object : gradleWrapperList) {
			if (object instanceof Map) {
				Map<?, ?> map = (Map<?, ?>) object;
				final ChecksumWrapper sha256 = new ChecksumWrapper();
				sha256.allowed = true;
				map.forEach((k, v) -> {
					if (k instanceof String) {
						switch ((String) k) {
							case "sha256":
								if (v instanceof String) {
									sha256.checksum = (String) v;
								}
								break;
							case "allowed":
								if (v instanceof Boolean) {
									sha256.allowed = (Boolean) v;
								}
								break;
							default:
								break;
						}
					}
				});
				if (sha256.checksum != null) {
					if (sha256.allowed) {
						sha256Allowed.add(sha256.checksum);
					} else {
						sha256Disallowed.add(sha256.checksum);
					}
				}
			}
		}
		WrapperValidator.clear();
		if (sha256Allowed != null) {
			WrapperValidator.allow(sha256Allowed);
		}
		if (sha256Disallowed != null) {
			WrapperValidator.disallow(sha256Disallowed);
		}
	}

	private static class ChecksumWrapper {
		private String checksum;
		private boolean allowed;
	}

}

package eu.jsparrow.independent.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.independent.ProxySettings;
import eu.jsparrow.independent.exceptions.StandaloneException;

public class ProxyUtils {

	private static final String PROXY_SETTINGS = "PROXY.SETTINGS"; //$NON-NLS-1$

	private ProxyUtils() {

	}

	public static void configureProxy(BundleContext context) throws IOException, StandaloneException {

		Queue<ProxySettings> proxySettingsList = parseProxySettingsFromBundleContext(
				context.getProperty(PROXY_SETTINGS));

		if (proxySettingsList.isEmpty()) {
			return;
		}

		setProxy(context, proxySettingsList);
	}

	private static Queue<ProxySettings> parseProxySettingsFromBundleContext(String settingsString)
			throws IOException, StandaloneException {

		if (settingsString == null || settingsString.isEmpty()) {
			return new LinkedList<>();
		}

		String[] splitSettingsString = settingsString.split("§"); //$NON-NLS-1$
		Queue<ProxySettings> proxySettingsList = new LinkedList<>();

		for (String proxySettingsString : splitSettingsString) {
			String proxySettingsPropertyString = proxySettingsString.replace("^", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			Properties prop = new Properties();
			prop.load(new StringReader(proxySettingsPropertyString));

			ProxySettings proxySettings = new ProxySettings();
			proxySettings.setType(prop.getProperty("type")); //$NON-NLS-1$
			proxySettings.setHost(prop.getProperty("host")); //$NON-NLS-1$
			proxySettings.setPort(Integer.parseInt(prop.getProperty("port"))); //$NON-NLS-1$
			proxySettings.setUserId(prop.getProperty("username", null)); //$NON-NLS-1$
			proxySettings.setPassword(prop.getProperty("password", null)); //$NON-NLS-1$

			String nonProxyHosts = prop.getProperty("nonProxyHosts"); //$NON-NLS-1$
			if (nonProxyHosts != null && !nonProxyHosts.isEmpty()) {
				String[] nonProxyHostsArray = nonProxyHosts.split("|"); //$NON-NLS-1$
				List<String> nonProxyHostsList = Arrays.asList(nonProxyHostsArray);
				proxySettings.setNonProxyHosts(nonProxyHostsList);
			}

			proxySettingsList.add(proxySettings);
		}

		return proxySettingsList;
	}

	/**
	 * Use the given {@link ProxySettings} to configure the equinox proxy
	 * 
	 * @param settings
	 *            object containing the proxy settings
	 * @throws StandaloneException
	 *             when the proxy couldn't be set
	 */
	private static void setProxy(BundleContext bundleContext, Queue<ProxySettings> settings)
			throws StandaloneException {
		ServiceReference<IProxyService> proxyServiceReference;
		IProxyService proxyService;
		proxyServiceReference = bundleContext.getServiceReference(IProxyService.class);
		proxyService = (IProxyService) bundleContext.getService(proxyServiceReference);

		if (proxyService == null) {
			throw new StandaloneException(Messages.ProxyConfiguration_CouldNotGetProxyServiceInstance);
		}

		IProxyData[] proxyData = proxyService.getProxyData();

		List<String> nonProxyHosts = new LinkedList<String>();
		for (IProxyData proxy : proxyData) {
			for (ProxySettings setting : settings) {
				boolean httpOrHttps = !IProxyData.SOCKS_PROXY_TYPE.equals(proxy.getType());
				if (httpOrHttps && proxy.getType()
					.equals(setting.getType())) {
					proxy.setHost(setting.getHost());
					proxy.setPort(setting.getPort());
					proxy.setUserid(setting.getUserId());
					proxy.setPassword(setting.getPassword());
					nonProxyHosts.addAll(setting.getNonProxyHosts());
				}
			}
		}

		try {
			if (!nonProxyHosts.isEmpty()) {
				proxyService.setNonProxiedHosts(nonProxyHosts.toArray(new String[0]));
			}
			proxyService.setProxyData(proxyData);
		} catch (CoreException e) {
			throw new StandaloneException(e.getMessage(), e);
		}

		proxyService.setSystemProxiesEnabled(false);
		proxyService.setProxiesEnabled(true);

		bundleContext.ungetService(proxyServiceReference);
		proxyService = null;
		proxyServiceReference = null;
	}
}

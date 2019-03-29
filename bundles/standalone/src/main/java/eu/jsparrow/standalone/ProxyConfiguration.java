package eu.jsparrow.standalone;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.jsparrow.standalone.exceptions.StandaloneException;

public class ProxyConfiguration {

	private BundleContext bundleContext;

	ServiceReference<IProxyService> proxyServiceReference;
	private IProxyService proxyService;

	public ProxyConfiguration(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	public void setProxy(ProxySettings settings) throws StandaloneException {
		initProxyService();

		if (proxyService == null) {
			throw new StandaloneException("Could not retrieve a proxy service instance");
		}

		IProxyData[] proxyData = proxyService.getProxyData();

		for (IProxyData proxy : proxyData) {
			if ((IProxyData.HTTP_PROXY_TYPE.equals(proxy.getType())
					|| IProxyData.HTTPS_PROXY_TYPE.equals(proxy.getType())) && proxy.getType()
						.equals(settings.getType())) {
				proxy.setHost(settings.getHost());
				proxy.setPort(settings.getPort());
				proxy.setUserid(settings.getUserId());
				proxy.setPassword(settings.getPassword());
			}
		}

		try {
			proxyService.setNonProxiedHosts(settings.getNonProxyHosts()
				.toArray(new String[0]));
		} catch (CoreException e) {
			throw new StandaloneException(e.getMessage(), e);
		}

		proxyService.setSystemProxiesEnabled(false);
		proxyService.setProxiesEnabled(true);

		ungetProxyService();
	}

	private void initProxyService() {
		proxyServiceReference = bundleContext.getServiceReference(IProxyService.class);
		proxyService = (IProxyService) bundleContext.getService(proxyServiceReference);
	}

	private void ungetProxyService() {
		if (proxyServiceReference == null || proxyService == null) {
			return;
		}

		bundleContext.ungetService(proxyServiceReference);
		proxyService = null;
		proxyServiceReference = null;
	}
}

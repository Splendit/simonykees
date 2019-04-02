package eu.jsparrow.maven.util;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.settings.Proxy;

public class ProxyUtil {
	
	private ProxyUtil() {
		
	}
	
	public static List<Proxy> getHttpProxies(MavenSession mavenSession) {
		return mavenSession.getSettings()
		.getProxies()
		.stream()
		.filter(Proxy::isActive)
		.filter(p -> "https".equalsIgnoreCase(p.getProtocol()) || "http".equalsIgnoreCase(p.getProtocol())) //$NON-NLS-1$ //$NON-NLS-2$
		.collect(Collectors.toList());
	}
	
	public static String getSettingsStringFrom(List<Proxy> proxies) {
		String settingsDelimiter = "^"; //$NON-NLS-1$
		String proxyDelimiter = "§"; //$NON-NLS-1$
		StringBuilder proxySettingsString = new StringBuilder();

		proxies.stream()
			.forEach(proxy -> {
				String type = proxy.getProtocol();
				String host = proxy.getHost();
				int port = proxy.getPort();
				String username = proxy.getUsername();
				String password = proxy.getPassword();
				String nonProxyHosts = proxy.getNonProxyHosts();

				proxySettingsString.append("type=") //$NON-NLS-1$
					.append(type)
					.append(settingsDelimiter);

				proxySettingsString.append("host=") //$NON-NLS-1$
					.append(host)
					.append(settingsDelimiter);

				proxySettingsString.append("port=") //$NON-NLS-1$
					.append(port)
					.append(settingsDelimiter);

				proxySettingsString.append("username=") //$NON-NLS-1$
					.append(username)
					.append(settingsDelimiter);

				proxySettingsString.append("password=") //$NON-NLS-1$
					.append(password)
					.append(settingsDelimiter);

				proxySettingsString.append("nonProxyHosts=") //$NON-NLS-1$
					.append(nonProxyHosts)
					.append(settingsDelimiter);

				proxySettingsString.append(proxyDelimiter);
			});

		return proxySettingsString.toString();
	}
	
	

}

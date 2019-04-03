package eu.jsparrow.maven.util;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.settings.Proxy;

public class ProxyUtil {
	
	private static final String PROXY_DELIMITER = "§"; //$NON-NLS-1$
	private static final String SETTINGS_DELIMITER = "^"; //$NON-NLS-1$

	private ProxyUtil() {

	}

	public static Stream<Proxy> getHttpProxies(MavenSession mavenSession) {
		return mavenSession.getSettings()
			.getProxies()
			.stream()
			.filter(Proxy::isActive)
			.filter(p -> "https".equalsIgnoreCase(p.getProtocol()) || "http".equalsIgnoreCase(p.getProtocol())) //$NON-NLS-1$ //$NON-NLS-2$
			.filter(p -> p.getHost() != null && p.getHost() != "" && p.getPort() != 0); //$NON-NLS-1$
	}

	public static String getSettingsStringFrom(Stream<Proxy> proxies) {
		 return proxies
				.map(p -> {
					StringBuilder proxySB = new StringBuilder();
					appendParameter(proxySB, "type=",p.getProtocol()); //$NON-NLS-1$
					appendParameter(proxySB, "host=",p.getHost()); //$NON-NLS-1$
					appendParameter(proxySB, "port=",String.valueOf(p.getPort())); //$NON-NLS-1$
					
					String username = p.getNonProxyHosts();
					if (username != null && !username.isEmpty()) {
						appendParameter(proxySB, "username=", username); //$NON-NLS-1$
					}
					
					String password = p.getNonProxyHosts();
					if (password != null && !password.isEmpty()) {
						appendParameter(proxySB, "password=", password); //$NON-NLS-1$
					}
					
					String nonProxyHosts = p.getNonProxyHosts();
					if (nonProxyHosts != null && !nonProxyHosts.isEmpty()) {
						appendParameter(proxySB, "nonProxyHosts=", nonProxyHosts); //$NON-NLS-1$
					}
					return proxySB.toString();
				}).collect(Collectors.joining(PROXY_DELIMITER));
	}
	
	private static void appendParameter(StringBuilder sb, String key, String value) {
		sb.append(key)
		.append(value)
		.append(SETTINGS_DELIMITER);
	}

}

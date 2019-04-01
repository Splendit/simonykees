package eu.jsparrow.standalone;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.osgi.util.NLS;

import eu.jsparrow.standalone.exceptions.StandaloneException;

public class ProxySettings {

	private String type;
	private String host;
	private int port;
	private String userId;
	private String password;
	private boolean requiresAuthentication;
	private List<String> nonProxyHosts;

	public String getHost() {
		return host;
	}

	public void setHost(String host) throws StandaloneException {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) throws StandaloneException {
		if (port < 0 || port > 65535) {
			String msg = NLS.bind("Port must have a value between 0 and 65535. {0} is invalid!", port);
			throw new StandaloneException(msg);
		}

		this.port = port;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isRequiresAuthentication() {
		return requiresAuthentication;
	}

	public void setRequiresAuthentication(boolean requiresAuthentication) {
		this.requiresAuthentication = requiresAuthentication;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) throws StandaloneException {
		if ("http".equalsIgnoreCase(type)) { //$NON-NLS-1$
			this.type = IProxyData.HTTP_PROXY_TYPE;
		} else if ("https".equalsIgnoreCase(type)) { //$NON-NLS-1$
			this.type = IProxyData.HTTPS_PROXY_TYPE;
		} else {
			throw new StandaloneException("The proxy only supports HTTPS or HTTP");
		}
	}

	public List<String> getNonProxyHosts() {
		return nonProxyHosts;
	}

	public void setNonProxyHosts(List<String> nonProxyHosts) {
		this.nonProxyHosts = nonProxyHosts;
	}
}

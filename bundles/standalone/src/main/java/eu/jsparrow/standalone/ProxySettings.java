package eu.jsparrow.standalone;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.osgi.util.NLS;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.standalone.exceptions.StandaloneException;

/**
 * Contains the settings to configure the equinox proxy
 * 
 * @since JMP 2.0.1
 *
 */
public class ProxySettings {

	private String type;
	private String host;
	private int port;
	private String userId;
	private String password;
	private boolean requiresAuthentication;
	private List<String> nonProxyHosts = new LinkedList<>();

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
			String msg = NLS.bind(Messages.ProxySettings_portMustBeBetween0And65535, port);
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
			throw new StandaloneException(Messages.ProxySettings_poxyOnlySupportsHTTPorHTTPS);
		}
	}

	public List<String> getNonProxyHosts() {
		return nonProxyHosts;
	}

	public void setNonProxyHosts(List<String> nonProxyHosts) {
		this.nonProxyHosts = nonProxyHosts;
	}
}

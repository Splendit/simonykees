package eu.jsparrow.standalone;

import java.io.File;
import java.util.Collections;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * this is a small helper class to execute a maven plugin
 * 
 * @author Matthias Webhofer
 * @since 2.5.0
 */
public class MavenInvoker {

	private Invoker invoker;

	private File mavenHome;
	private File pomFile;

	public MavenInvoker(File mavenHome, File pomFile) {
		this.mavenHome = mavenHome;
		this.pomFile = pomFile;

		this.invoker = getDefaultInvoker();
	}

	/**
	 * invokes the given maven plugin with the given goal
	 * 
	 * @param plugin
	 * @param goal
	 * @param version
	 * @throws MavenInvocationException
	 */
	public void invoke(String plugin, String goal, String version) throws MavenInvocationException {
		String goalString = this.createGoalsString(plugin, goal, version);

		InvocationRequest request = getDefaultInvocationRequest();
		request.setBatchMode(true);
		request.setPomFile(pomFile);
		request.setGoals(Collections.singletonList(goalString));

		invoker.setMavenHome(mavenHome);
		invoker.execute(request);
	}

	/**
	 * creates a plugin identifier string for maven. i.e. "eclipse:clean"
	 * 
	 * @param plugin
	 * @param goal
	 * @param version
	 * @return
	 */
	protected String createGoalsString(String plugin, String goal, String version) {
		String separator = ":"; //$NON-NLS-1$

		StringBuilder sb = new StringBuilder();

		if (plugin != null && !plugin.isEmpty()) {
			sb.append(plugin);
			if (goal != null && !goal.isEmpty()) {
				sb.append(separator)
					.append(goal);
				if (version != null && !version.isEmpty()) {
					sb.append(separator)
						.append(version);
				}
			}
		}

		return sb.toString();
	}

	protected Invoker getDefaultInvoker() {
		return new DefaultInvoker();
	}

	protected InvocationRequest getDefaultInvocationRequest() {
		return new DefaultInvocationRequest();
	}
}

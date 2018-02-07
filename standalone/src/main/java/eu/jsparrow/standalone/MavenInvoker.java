package eu.jsparrow.standalone;

import java.io.File;
import java.util.Collections;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

public class MavenInvoker {

	private Invoker invoker;

	private File mavenHome;
	private File pomFile;

	public MavenInvoker(File mavenHome, File pomFile) {
		this.mavenHome = mavenHome;
		this.pomFile = pomFile;

		this.invoker = getDefaultInvoker();
	}

	public void invoke(String plugin, String goal, String version) throws MavenInvocationException {
		String goalString = this.createGoalsString(plugin, goal, version);

		InvocationRequest request = getDefaultInvocationRequest();
		request.setBatchMode(true);
		request.setPomFile(pomFile);
		request.setGoals(Collections.singletonList(goalString));

		invoker.setMavenHome(mavenHome);
		invoker.execute(request);
	}

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

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

		this.invoker = new DefaultInvoker();
	}

	public void invoke(String plugin, String goal, String version) throws MavenInvocationException {
		String goalString = this.createGoalsString(plugin, goal, version);
		
		InvocationRequest request = new DefaultInvocationRequest();
		request.setBatchMode(true);
		request.setPomFile(pomFile);
		request.setGoals(Collections.singletonList(goalString));

		invoker = new DefaultInvoker();
		invoker.setMavenHome(mavenHome);
		invoker.execute(request);
	}

	private String createGoalsString(String plugin, String goal, String version) {
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
}

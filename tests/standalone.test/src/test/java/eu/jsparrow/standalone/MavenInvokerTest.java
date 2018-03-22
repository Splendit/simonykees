package eu.jsparrow.standalone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.List;

import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.5.0
 */
@SuppressWarnings("nls")
public class MavenInvokerTest {

	private static final String ECLIPSE = "eclipse";
	private static final String CLEAN = "clean";

	private File mavenHome;
	private File pomFile;

	private Invoker invoker;
	private InvocationRequest invocationRequest;

	private MavenInvoker mavenInvoker;

	@Before
	public void setUp() {
		mavenHome = mock(File.class);
		pomFile = mock(File.class);

		invoker = mock(Invoker.class);
		invocationRequest = mock(InvocationRequest.class);

		mavenInvoker = new TestableMavenInvoker(mavenHome, pomFile);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void invoke() throws Exception {
		String plugin = ECLIPSE;
		String goal = CLEAN;
		String goalString = plugin + ":" + goal;

		mavenInvoker.invoke(plugin, goal, null);

		verify(invocationRequest).setPomFile(eq(pomFile));

		ArgumentCaptor<List> goalsCaptor = ArgumentCaptor.forClass(List.class);
		verify(invocationRequest).setGoals(goalsCaptor.capture());
		assertTrue(goalsCaptor.getValue()
			.get(0)
			.equals(goalString));

		verify(invoker).setMavenHome(eq(mavenHome));
		verify(invoker).execute(eq(invocationRequest));
	}

	@Test
	public void createGoalsString_pluginGoalVersionProvided_shouldReturnGoalString() {
		String plugin = ECLIPSE;
		String goal = CLEAN;
		String version = "1.0";
		String expectedResult = plugin + ":" + goal + ":" + version;

		String result = mavenInvoker.createGoalsString(plugin, goal, version);

		assertEquals(result, expectedResult);
	}

	@Test
	public void createGoalsString_pluginGoalProvided_shouldReturnGoalString() {
		String plugin = ECLIPSE;
		String goal = CLEAN;
		String expectedResult = plugin + ":" + goal;

		String result = mavenInvoker.createGoalsString(plugin, goal, null);

		assertEquals(result, expectedResult);
	}

	@Test
	public void createGoalsString_pluginGoalAndEmptyVersionProvided_shouldReturnGoalString() {
		String plugin = ECLIPSE;
		String goal = CLEAN;
		String expectedResult = plugin + ":" + goal;

		String result = mavenInvoker.createGoalsString(plugin, goal, "");

		assertEquals(result, expectedResult);
	}

	@Test
	public void createGoalsString_nothingProvided_shouldReturnEmptyString() {
		String expectedResult = "";

		String result = mavenInvoker.createGoalsString(null, null, null);

		assertEquals(result, expectedResult);
	}

	@Test
	public void createGoalsString_emptyPluginProvided_shouldReturnEmptyString() {
		String plugin = "";
		String expectedResult = "";

		String result = mavenInvoker.createGoalsString(plugin, null, null);

		assertEquals(result, expectedResult);
	}

	@Test
	public void createGoalsString_pluginProvided_shouldReturnGoalString() {
		String plugin = ECLIPSE;
		String expectedResult = plugin;

		String result = mavenInvoker.createGoalsString(plugin, null, null);

		assertEquals(result, expectedResult);
	}

	@Test
	public void createGoalsString_pluginAndEmptyGoalProvided_shouldReturnGoalString() {
		String plugin = ECLIPSE;
		String goal = "";
		String expectedResult = plugin;

		String result = mavenInvoker.createGoalsString(plugin, goal, null);

		assertEquals(result, expectedResult);
	}

	class TestableMavenInvoker extends MavenInvoker {

		public TestableMavenInvoker(File mavenHome, File pomFile) {
			super(mavenHome, pomFile);
		}

		@Override
		protected Invoker getDefaultInvoker() {
			return invoker;
		}

		@Override
		protected InvocationRequest getDefaultInvocationRequest() {
			return invocationRequest;
		}
	}
}

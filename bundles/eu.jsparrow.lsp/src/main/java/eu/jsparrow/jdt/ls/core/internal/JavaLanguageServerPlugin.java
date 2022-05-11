package eu.jsparrow.jdt.ls.core.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.manipulation.JavaManipulation;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.core.manipulation.JavaManipulationPlugin;
import org.eclipse.jdt.internal.core.manipulation.MembersOrderPreferenceCacheCommon;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettingsConstants;

import eu.jsparrow.jdt.ls.core.internal.ServiceStatus;

import eu.jsparrow.jdt.ls.core.internal.ConnectionStreamFactory;
import eu.jsparrow.jdt.ls.core.internal.JDTEnvironmentUtils;
import eu.jsparrow.jdt.ls.core.internal.ParentProcessWatcher;
import eu.jsparrow.jdt.ls.core.internal.JavaClientConnection.JavaLanguageClient;
import eu.jsparrow.jdt.ls.core.internal.handlers.JSparrowLanguageServer;

import org.eclipse.lsp4j.DidChangeWatchedFilesRegistrationOptions;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;

import eu.jsparrow.jdt.ls.core.internal.managers.ISourceDownloader;
import eu.jsparrow.jdt.ls.core.internal.managers.MavenSourceDownloader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.util.tracker.ServiceTracker;

import com.google.common.base.Throwables;

import eu.jsparrow.jdt.ls.core.internal.managers.ProjectsManager;
import eu.jsparrow.jdt.ls.core.internal.managers.StandardProjectsManager;
import eu.jsparrow.jdt.ls.core.internal.preferences.PreferenceManager;
import eu.jsparrow.jdt.ls.core.internal.preferences.StandardPreferenceManager;

public class JavaLanguageServerPlugin extends Plugin {

	public static final String DEFAULT_MEMBER_SORT_ORDER = "T,SF,SI,SM,F,I,C,M"; //$NON-NLS-1$
	
	private static final String JDT_UI_PLUGIN = "org.eclipse.jdt.ui";
	private static final String LOGBACK_CONFIG_FILE_PROPERTY = "logback.configurationFile";
	private static final String LOGBACK_DEFAULT_FILENAME = "logback.xml";
	
	/**
	 * Source string send to clients for messages such as diagnostics.
	 **/
	public static final String SERVER_SOURCE_ID = "Java";

	private static JavaLanguageServerPlugin pluginInstance;
	private static BundleContext context;
	private ServiceTracker<IProxyService, IProxyService> proxyServiceTracker = null;
	private static InputStream in;
	private static PrintStream out;
	private static PrintStream err;
	private LanguageServer languageServer;
	
	private ISourceDownloader sourceDownloader;

	private PreferenceManager preferenceManager;
	private ProjectsManager projectsManager;
	
	private JSparrowLanguageServer protocol;
	
	public static JavaLanguageServerPlugin getInstance() {
		return pluginInstance;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		try {
			Platform.getBundle(ResourcesPlugin.PI_RESOURCES).start(Bundle.START_TRANSIENT);
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceDescription description = workspace.getDescription();
			description.setAutoBuilding(false);
			workspace.setDescription(description);
		} catch (BundleException e) {
			logException(e.getMessage(), e);
		}
		boolean isDebug = Boolean.getBoolean("jdt.ls.debug");
		try {
			redirectStandardStreams(isDebug);
		} catch (FileNotFoundException e) {
			logException(e.getMessage(), e);
		}
		JavaLanguageServerPlugin.context = bundleContext;
		JavaLanguageServerPlugin.pluginInstance = this;
		setPreferenceNodeId();

		// Override logback preferences *before* M2E plugin is activated below
		if (isDebug && System.getProperty(LOGBACK_CONFIG_FILE_PROPERTY) == null) {
			File stateDir = getStateLocation().toFile();
			File configFile = new File(stateDir, LOGBACK_DEFAULT_FILENAME);
			if (!configFile.isFile()) {
				try (InputStream is = bundleContext.getBundle().getEntry(LOGBACK_DEFAULT_FILENAME).openStream();
						FileOutputStream fos = new FileOutputStream(configFile)) {
					for (byte[] buffer = new byte[1024 * 4];;) {
						int n = is.read(buffer);
						if (n < 0) {
							break;
						}
						fos.write(buffer, 0, n);
					}
				}
			}
			// ContextInitializer.CONFIG_FILE_PROPERTY
			System.setProperty(LOGBACK_CONFIG_FILE_PROPERTY, configFile.getAbsolutePath());
		}

		preferenceManager = new StandardPreferenceManager();
		projectsManager = new StandardProjectsManager(preferenceManager);

		IEclipsePreferences fDefaultPreferenceStore = DefaultScope.INSTANCE
				.getNode(JavaManipulation.getPreferenceNodeId());
		fDefaultPreferenceStore.put(JavaManipulationPlugin.CODEASSIST_FAVORITE_STATIC_MEMBERS, "");

//		digestStore = new DigestStore(getStateLocation().toFile());
		try {
			ResourcesPlugin.getWorkspace().addSaveParticipant(IConstants.PLUGIN_ID, projectsManager);
		} catch (CoreException e) {
			logException(e.getMessage(), e);
		}
//		contentProviderManager = new ContentProviderManager(preferenceManager);
//		nonProjectDiagnosticsState = new DiagnosticsState();
		logInfo(getClass() + " is started");
//		configureProxy();
		// turn off substring code completion if isn't explicitly set
		if (System.getProperty(AssistOptions.PROPERTY_SubstringMatch) == null) {
			System.setProperty(AssistOptions.PROPERTY_SubstringMatch, "false");
		}
	}

	private void startConnection() throws IOException {
		Launcher<JavaLanguageClient> launcher;
		ExecutorService executorService = Executors.newCachedThreadPool();
		protocol = new JSparrowLanguageServer(projectsManager, preferenceManager);
		if (JDTEnvironmentUtils.inSocketStreamDebugMode()) {
			String host = JDTEnvironmentUtils.getClientHost();
			Integer port = JDTEnvironmentUtils.getClientPort();
			InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
			AsynchronousServerSocketChannel serverSocket = AsynchronousServerSocketChannel.open().bind(inetSocketAddress);
			try {
				AsynchronousSocketChannel socketChannel = serverSocket.accept().get();
				InputStream in = Channels.newInputStream(socketChannel);
				OutputStream out = Channels.newOutputStream(socketChannel);
				Function<MessageConsumer, MessageConsumer> messageConsumer = it -> it;
				launcher = Launcher.createIoLauncher(protocol, JavaLanguageClient.class, in, out, executorService, messageConsumer);
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException("Error when opening a socket channel at " + host + ":" + port + ".", e);
			}
		} else {
			ConnectionStreamFactory connectionFactory = new ConnectionStreamFactory();
			InputStream in = connectionFactory.getInputStream();
			OutputStream out = connectionFactory.getOutputStream();
			Function<MessageConsumer, MessageConsumer> wrapper;
			if ("false".equals(System.getProperty("watchParentProcess"))) {
				wrapper = it -> it;
			} else {
				wrapper = new ParentProcessWatcher(this.languageServer);
			}
			launcher = Launcher.createLauncher(protocol, JavaLanguageClient.class, in, out, executorService, wrapper);
		}
		protocol.connectClient(launcher.getRemoteProxy());
		launcher.startListening();
	}
	
	public void unregisterCapability(String id, String method) {
		if (protocol != null) {
			protocol.unregisterCapability(id, method);
		}
		
	}

	public void registerCapability(String id, String method) {
		registerCapability(id, method, null);
	}
	
	public void registerCapability(String id, String method, Object options) {
		if (protocol != null) {
			protocol.registerCapability(id, method, options);
		}
	}
	
	public static void startLanguageServer(LanguageServer newLanguageServer) throws IOException {
		if (pluginInstance != null) {
			pluginInstance.languageServer = newLanguageServer;
			pluginInstance.startConnection();
		}
		
	}
	
	public static BundleContext getBundleContext() {
		return JavaLanguageServerPlugin.context;
	}

	public static LanguageServer getLanguageServer() {
		return pluginInstance == null ? null : pluginInstance.languageServer;
	}

	public static ProjectsManager getProjectsManager() {
		return pluginInstance.projectsManager;
	}
	
	public static void log(IStatus status) {
		if (context != null) {
			Platform.getLog(JavaLanguageServerPlugin.context.getBundle()).log(status);
		}
	}

	public static void logException(String message, Throwable ex) {
		if (context != null) {
			log(new Status(IStatus.ERROR, context.getBundle().getSymbolicName(), message, ex));
		}
	}

	public static void logInfo(String message) {
		if (context != null) {
			log(new Status(IStatus.INFO, context.getBundle().getSymbolicName(), message));
		}
	}

	public static void log(CoreException e) {
		log(e.getStatus());
	}
	
	public static void logError(String message) {
		if (context != null) {
			log(new Status(IStatus.ERROR, context.getBundle().getSymbolicName(), message));
		}
	}

	public static PreferenceManager getPreferencesManager() {
		if (JavaLanguageServerPlugin.pluginInstance != null) {
			return JavaLanguageServerPlugin.pluginInstance.preferenceManager;
		}
		return null;
	}

	private static void redirectStandardStreams(boolean isDebug) throws FileNotFoundException {
		in = System.in;
		out = System.out;
		err = System.err;
		System.setIn(new ByteArrayInputStream(new byte[0]));
		if (isDebug) {
			String id = "jdt.ls-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			File workspaceFile = root.getRawLocation().makeAbsolute().toFile();
			File rootFile = new File(workspaceFile, ".metadata");
			rootFile.mkdirs();
			File outFile = new File(rootFile, ".out-" + id + ".log");
			FileOutputStream stdFileOut = new FileOutputStream(outFile);
			System.setOut(new PrintStream(stdFileOut));
			File errFile = new File(rootFile, ".error-" + id + ".log");
			FileOutputStream stdFileErr = new FileOutputStream(errFile);
			System.setErr(new PrintStream(stdFileErr));
		} else {
			System.setOut(new PrintStream(new ByteArrayOutputStream()));
			System.setErr(new PrintStream(new ByteArrayOutputStream()));
		}
	}

	private void setPreferenceNodeId() {
		// a hack to ensure unit tests work in Eclipse and CLI builds on Mac
		Bundle bundle = Platform.getBundle(JDT_UI_PLUGIN);
		if (bundle != null && bundle.getState() != Bundle.ACTIVE) {
			// start the org.eclipse.jdt.ui plugin if it exists
			try {
				bundle.start();
			} catch (BundleException e) {
				JavaLanguageServerPlugin.logException(e.getMessage(), e);
			}
		}
		// if preferenceNodeId is already set, we have to nullify it
		// https://git.eclipse.org/c/jdt/eclipse.jdt.ui.git/commit/?id=4c731bc9cc7e1cfd2e67746171aede8d7719e9c1
		JavaManipulation.setPreferenceNodeId(null);
		// Set the ID to use for preference lookups
		JavaManipulation.setPreferenceNodeId(IConstants.PLUGIN_ID);

		IEclipsePreferences fDefaultPreferenceStore = DefaultScope.INSTANCE
				.getNode(JavaManipulation.getPreferenceNodeId());
		fDefaultPreferenceStore.put(MembersOrderPreferenceCacheCommon.APPEARANCE_MEMBER_SORT_ORDER,
				DEFAULT_MEMBER_SORT_ORDER);
		fDefaultPreferenceStore.put(CodeGenerationSettingsConstants.CODEGEN_USE_OVERRIDE_ANNOTATION,
				Boolean.TRUE.toString());

		// initialize MembersOrderPreferenceCacheCommon used by BodyDeclarationRewrite
		MembersOrderPreferenceCacheCommon preferenceCache = JavaManipulationPlugin.getDefault()
				.getMembersOrderPreferenceCacheCommon();
		preferenceCache.install();
	}

	public static void sendStatus(ServiceStatus serverStatus, String status) {
		if (pluginInstance != null && pluginInstance.protocol != null) {
			pluginInstance.protocol.sendStatus(serverStatus, status);
		}
	}

	public static String getVersion() {
		return context == null ? "Unknown" : context.getBundle().getVersion().toString();
	}
	
	public static synchronized ISourceDownloader getDefaultSourceDownloader() {
		if (pluginInstance.sourceDownloader == null) {
			pluginInstance.sourceDownloader = new MavenSourceDownloader();
		}
		return pluginInstance.sourceDownloader;
	}

	public static void logException(Throwable ex) {
		if (context != null) {
			String message = ex.getMessage();
			if (message == null) {
				message = Throwables.getStackTraceAsString(ex);
			}
			logException(message, ex);
		}
	}

	public static InputStream getIn() {
		return in;
	}

	public static OutputStream getOut() {
		return out;
	}
	
	public static PrintStream getErr() {
		return err;
	}

	public JSparrowLanguageServer getProtocol() {
		return protocol;
	}

	public JavaClientConnection getClientConnection() {
		if (protocol != null) {
			return protocol.getClientConnection();
		}
		return null;
	}
}

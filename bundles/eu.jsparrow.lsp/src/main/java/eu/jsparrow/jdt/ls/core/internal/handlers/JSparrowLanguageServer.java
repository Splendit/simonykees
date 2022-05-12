package eu.jsparrow.jdt.ls.core.internal.handlers;

import static eu.jsparrow.jdt.ls.core.internal.JavaLanguageServerPlugin.logException;
import static eu.jsparrow.jdt.ls.core.internal.JavaLanguageServerPlugin.logInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import eu.jsparrow.jdt.ls.core.internal.BaseJDTLanguageServer;
import eu.jsparrow.jdt.ls.core.internal.JSONUtility;
import eu.jsparrow.jdt.ls.core.internal.JVMConfigurator;
import eu.jsparrow.jdt.ls.core.internal.JavaClientConnection.JavaLanguageClient;
import eu.jsparrow.jdt.ls.core.internal.JavaLanguageServerPlugin;
import eu.jsparrow.jdt.ls.core.internal.JobHelpers;
import eu.jsparrow.jdt.ls.core.internal.LanguageServerWorkingCopyOwner;
import eu.jsparrow.jdt.ls.core.internal.ServiceStatus;
import eu.jsparrow.jdt.ls.core.internal.managers.ProjectsManager;
import eu.jsparrow.jdt.ls.core.internal.preferences.PreferenceManager;
import eu.jsparrow.jdt.ls.core.internal.preferences.Preferences;

public class JSparrowLanguageServer extends BaseJDTLanguageServer implements LanguageServer, TextDocumentService, WorkspaceService  {

	public static final String JAVA_LSP_JOIN_ON_COMPLETION = "java.lsp.joinOnCompletion";
	public static final String JAVA_LSP_INITIALIZE_WORKSPACE = "java.lsp.initializeWorkspace";
	/**
	 * Exit code returned when JDTLanguageServer is forced to exit.
	 */
	private static final int FORCED_EXIT_CODE = 1;
	private ProjectsManager pm;
	private LanguageServerWorkingCopyOwner workingCopyOwner;
	private PreferenceManager preferenceManager;
	private DocumentLifeCycleHandler documentLifeCycleHandler;
	private WorkspaceDiagnosticsHandler workspaceDiagnosticsHandler;
	private ClasspathUpdateHandler classpathUpdateHandler;
	private JVMConfigurator jvmConfigurator;
	private WorkspaceExecuteCommandHandler commandHandler;
	private ProgressReporterManager progressReporterManager;

	/**
	 * The status of the language service
	 */
	private ServiceStatus status;
	
	private Job shutdownJob = new Job("Shutdown...") {

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				JavaRuntime.removeVMInstallChangedListener(jvmConfigurator);
				if (workspaceDiagnosticsHandler != null) {
					workspaceDiagnosticsHandler.removeResourceChangeListener();
					workspaceDiagnosticsHandler = null;
				}
				if (classpathUpdateHandler != null) {
					classpathUpdateHandler.removeElementChangeListener();
					classpathUpdateHandler = null;
				}
				ResourcesPlugin.getWorkspace().save(true, monitor);
			} catch (CoreException e) {
				logException(e.getMessage(), e);
			}
			return Status.OK_STATUS;
		}

	};
	
	public JSparrowLanguageServer(ProjectsManager projects, PreferenceManager preferenceManager) {
		this(projects, preferenceManager, WorkspaceExecuteCommandHandler.getInstance());
	}
	
	public JSparrowLanguageServer(ProjectsManager projects, PreferenceManager preferenceManager, WorkspaceExecuteCommandHandler commandHandler) {
		this.pm = projects;
		this.preferenceManager = preferenceManager;
		this.jvmConfigurator = new JVMConfigurator();
		JavaRuntime.addVMInstallChangedListener(jvmConfigurator);
		this.commandHandler = commandHandler;
	}
	
	@Override
	public void connectClient(JavaLanguageClient client) {
		super.connectClient(client);
		progressReporterManager = new ProgressReporterManager(client, preferenceManager);
		Job.getJobManager().setProgressProvider(progressReporterManager);
		this.workingCopyOwner = new LanguageServerWorkingCopyOwner(this.client);
		pm.setConnection(client);
		WorkingCopyOwner.setPrimaryBufferProvider(this.workingCopyOwner);
		this.documentLifeCycleHandler = new DocumentLifeCycleHandler(this.client, preferenceManager, pm, true);
		
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		logInfo(">> initialize");
		status = ServiceStatus.Starting;
		InitHandler handler = new InitHandler(pm, preferenceManager, client, commandHandler);
		return CompletableFuture.completedFuture(handler.initialize(params));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.lsp4j.services.LanguageServer#initialized(org.eclipse.lsp4j.InitializedParams)
	 */
	@Override
	public void initialized(InitializedParams params) {
		logInfo(">> initialized");
		try {
			JobHelpers.waitForInitializeJobs(60 * 60 * 1000); // 1 hour
		} catch (OperationCanceledException e) {
			logException(e.getMessage(), e);
		}
		logInfo(">> initialization job finished");

		Job initializeWorkspace = new Job("Initialize workspace") {

			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					JobHelpers.waitForBuildJobs(60 * 60 * 1000); // 1 hour
					logInfo(">> build jobs finished");
					workspaceDiagnosticsHandler = new WorkspaceDiagnosticsHandler(JSparrowLanguageServer.this.client, pm, preferenceManager.getClientPreferences(), documentLifeCycleHandler);
					workspaceDiagnosticsHandler.publishDiagnostics(monitor);
					workspaceDiagnosticsHandler.addResourceChangeListener();
					classpathUpdateHandler = new ClasspathUpdateHandler(JSparrowLanguageServer.this.client);
					classpathUpdateHandler.addElementChangeListener();
					pm.registerWatchers();
					logInfo(">> watchers registered");

					registerCapabilities();
					// we do not have the user setting initialized yet at this point but we should
					// still call to enable defaults in case client does not support configuration changes
					syncCapabilitiesToSettings();

					client.sendStatus(ServiceStatus.ServiceReady, "ServiceReady");
					status = ServiceStatus.ServiceReady;
				} catch (OperationCanceledException | CoreException e) {
					logException(e.getMessage(), e);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				return JAVA_LSP_INITIALIZE_WORKSPACE.equals(family);
			}

		};
		initializeWorkspace.setPriority(Job.BUILD);
		initializeWorkspace.setSystem(true);
		initializeWorkspace.schedule();
	}
	
	/**
	 * Register capabilities to client
	 */
	private void registerCapabilities() {
		if (preferenceManager.getClientPreferences().isWorkspaceSymbolDynamicRegistered()) {
			registerCapability(Preferences.WORKSPACE_SYMBOL_ID, Preferences.WORKSPACE_SYMBOL);
		}
		if (!preferenceManager.getClientPreferences().isClientDocumentSymbolProviderRegistered() && preferenceManager.getClientPreferences().isDocumentSymbolDynamicRegistered()) {
			registerCapability(Preferences.DOCUMENT_SYMBOL_ID, Preferences.DOCUMENT_SYMBOL);
		}
		if (preferenceManager.getClientPreferences().isDefinitionDynamicRegistered()) {
			registerCapability(Preferences.DEFINITION_ID, Preferences.DEFINITION);
		}
		if (preferenceManager.getClientPreferences().isTypeDefinitionDynamicRegistered()) {
			registerCapability(Preferences.TYPEDEFINITION_ID, Preferences.TYPEDEFINITION);
		}
		if (!preferenceManager.getClientPreferences().isClientHoverProviderRegistered() && preferenceManager.getClientPreferences().isHoverDynamicRegistered()) {
			registerCapability(Preferences.HOVER_ID, Preferences.HOVER);
		}
		if (preferenceManager.getClientPreferences().isReferencesDynamicRegistered()) {
			registerCapability(Preferences.REFERENCES_ID, Preferences.REFERENCES);
		}
		if (preferenceManager.getClientPreferences().isDocumentHighlightDynamicRegistered()) {
			registerCapability(Preferences.DOCUMENT_HIGHLIGHT_ID, Preferences.DOCUMENT_HIGHLIGHT);
		}
		if (preferenceManager.getClientPreferences().isWorkspaceFoldersSupported()) {
			registerCapability(Preferences.WORKSPACE_CHANGE_FOLDERS_ID, Preferences.WORKSPACE_CHANGE_FOLDERS);
		}
		if (preferenceManager.getClientPreferences().isImplementationDynamicRegistered()) {
			registerCapability(Preferences.IMPLEMENTATION_ID, Preferences.IMPLEMENTATION);
		}
	}

	/**
	 * Toggles the server capabilities according to user preferences.
	 */
	private void syncCapabilitiesToSettings() {
//		if (preferenceManager.getClientPreferences().isCompletionDynamicRegistered()) {
//			toggleCapability(preferenceManager.getPreferences().isCompletionEnabled(), Preferences.COMPLETION_ID, Preferences.COMPLETION, CompletionHandler.DEFAULT_COMPLETION_OPTIONS);
//		}
		if (preferenceManager.getClientPreferences().isFormattingDynamicRegistrationSupported()) {
			toggleCapability(preferenceManager.getPreferences().isJavaFormatEnabled(), Preferences.FORMATTING_ID, Preferences.TEXT_DOCUMENT_FORMATTING, null);
		}
		if (preferenceManager.getClientPreferences().isRangeFormattingDynamicRegistrationSupported()) {
			toggleCapability(preferenceManager.getPreferences().isJavaFormatEnabled(), Preferences.FORMATTING_RANGE_ID, Preferences.TEXT_DOCUMENT_RANGE_FORMATTING, null);
		}
//		if (preferenceManager.getClientPreferences().isOnTypeFormattingDynamicRegistrationSupported()) {
//			toggleCapability(preferenceManager.getPreferences().isJavaFormatOnTypeEnabled(), Preferences.FORMATTING_ON_TYPE_ID, Preferences.TEXT_DOCUMENT_ON_TYPE_FORMATTING,
//					new DocumentOnTypeFormattingOptions(";", Arrays.asList("\n", "}")));
//		}
//		if (preferenceManager.getClientPreferences().isCodeLensDynamicRegistrationSupported()) {
//			toggleCapability(preferenceManager.getPreferences().isCodeLensEnabled(), Preferences.CODE_LENS_ID, Preferences.TEXT_DOCUMENT_CODE_LENS, new CodeLensOptions(true));
//		}
//		if (preferenceManager.getClientPreferences().isSignatureHelpDynamicRegistrationSupported()) {
//			toggleCapability(preferenceManager.getPreferences().isSignatureHelpEnabled(), Preferences.SIGNATURE_HELP_ID, Preferences.TEXT_DOCUMENT_SIGNATURE_HELP, SignatureHelpHandler.createOptions());
//		}
//		if (preferenceManager.getClientPreferences().isRenameDynamicRegistrationSupported()) {
//			toggleCapability(preferenceManager.getPreferences().isRenameEnabled(), Preferences.RENAME_ID, Preferences.TEXT_DOCUMENT_RENAME, RenameHandler.createOptions());
//		}
		if (preferenceManager.getClientPreferences().isExecuteCommandDynamicRegistrationSupported()) {
			toggleCapability(preferenceManager.getPreferences().isExecuteCommandEnabled(), Preferences.EXECUTE_COMMAND_ID, Preferences.WORKSPACE_EXECUTE_COMMAND,
					new ExecuteCommandOptions(new ArrayList<>(commandHandler.getNonStaticCommands())));
		}
		if (preferenceManager.getClientPreferences().isCodeActionDynamicRegistered()) {
			toggleCapability(preferenceManager.getClientPreferences().isCodeActionDynamicRegistered(), Preferences.CODE_ACTION_ID, Preferences.CODE_ACTION, getCodeActionOptions());
		}
		if (preferenceManager.getClientPreferences().isFoldgingRangeDynamicRegistered()) {
			toggleCapability(preferenceManager.getPreferences().isFoldingRangeEnabled(), Preferences.FOLDINGRANGE_ID, Preferences.FOLDINGRANGE, null);
		}
		if (preferenceManager.getClientPreferences().isSelectionRangeDynamicRegistered()) {
			toggleCapability(preferenceManager.getPreferences().isSelectionRangeEnabled(), Preferences.SELECTION_RANGE_ID, Preferences.SELECTION_RANGE, null);
		}
	}
	
	private CodeActionOptions getCodeActionOptions() {
		String[] kinds = { CodeActionKind.QuickFix, CodeActionKind.Refactor, CodeActionKind.RefactorExtract, CodeActionKind.RefactorInline, CodeActionKind.RefactorRewrite, CodeActionKind.Source, CodeActionKind.SourceOrganizeImports };
		List<String> codeActionKinds = new ArrayList<>();
		for (String kind : kinds) {
			if (preferenceManager.getClientPreferences().isSupportedCodeActionKind(kind)) {
				codeActionKinds.add(kind);
			}
		}
		CodeActionOptions options = new CodeActionOptions(codeActionKinds);
		options.setResolveProvider(Boolean.valueOf(preferenceManager.getClientPreferences().isResolveCodeActionSupported()));
		return options;
	}
	
	@Override
	public CompletableFuture<Object> shutdown() {
		logInfo(">> shutdown");
		return computeAsync((monitor) -> {
			shutdownJob.schedule();
			shutdownReceived = true;
			if (preferenceManager.getClientPreferences().shouldLanguageServerExitOnShutdown()) {
				Executors.newSingleThreadScheduledExecutor().schedule(() -> exit(), 1, TimeUnit.SECONDS);
			}
			return new Object();
		});
	}

	@Override
	public void exit() {
		logInfo(">> exit");
		if (!shutdownReceived) {
			shutdownJob.schedule();
		}
		try {
			shutdownJob.join();
		} catch (InterruptedException e) {
			JavaLanguageServerPlugin.logException(e.getMessage(), e);
		}
		JavaLanguageServerPlugin.getLanguageServer().exit();
		Executors.newSingleThreadScheduledExecutor().schedule(() -> {
			logInfo("Forcing exit after 1 min.");
			System.exit(FORCED_EXIT_CODE);
		}, 1, TimeUnit.MINUTES);
		
	}
	
	@Override
	public TextDocumentService getTextDocumentService() {
		return this;
	}
	
	@Override
	public WorkspaceService getWorkspaceService() {
		return this;
	}
	
	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		logInfo(">> workspace/didChangeConfiguration");
		Object settings = JSONUtility.toModel(params.getSettings(), Map.class);
		if (settings instanceof Map) {
			Collection<IPath> rootPaths = preferenceManager.getPreferences().getRootPaths();
			@SuppressWarnings("unchecked")
			Preferences prefs = Preferences.createFrom((Map<String, Object>) settings);
			prefs.setRootPaths(rootPaths);
			preferenceManager.update(prefs);
		}
		if (status == ServiceStatus.ServiceReady) {
			// If we toggle on the capabilities too early before the tasks in initialized handler finished,
			// client will start to send request to server, but the server won't be able to handle them.
			syncCapabilitiesToSettings();
		}

		try {
			JVMConfigurator.configureJVMs(preferenceManager.getPreferences(), this.client);
		} catch (Exception e) {
			JavaLanguageServerPlugin.logException(e.getMessage(), e);
		}
//		try {
//			boolean autoBuildChanged = ProjectsManager.setAutoBuilding(preferenceManager.getPreferences().isAutobuildEnabled());
//			if (jvmChanged) {
//				buildWorkspace(Either.forLeft(true));
//			} else if (autoBuildChanged) {
//				buildWorkspace(Either.forLeft(false));
//			}
//		} catch (CoreException e) {
//			JavaLanguageServerPlugin.logException(e.getMessage(), e);
//		}
		logInfo(">> New configuration: " + settings);
		
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		logInfo(">> workspace/didChangeWatchedFiles ");
		WorkspaceEventsHandler handler = new WorkspaceEventsHandler(pm, client, this.documentLifeCycleHandler);
		handler.didChangeWatchedFiles(params);
		
	}
	
	@Override
	public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
		logInfo(">> workspace/executeCommand " + (params == null ? null : params.getCommand()));
		return computeAsync((monitor) -> {
			return commandHandler.executeCommand(params, monitor);
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.lsp4j.services.TextDocumentService#hover(org.eclipse.lsp4j.HoverParams)
	 */
	@Override
	public CompletableFuture<Hover> hover(HoverParams position) {
		logInfo(">> document/hover"); // TODO: Do we really need this? 
		HoverHandler handler = new HoverHandler(this.preferenceManager);
		return computeAsync((monitor) -> handler.hover(position, monitor));
	}
	
	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		logInfo(">> document/didOpen");
		documentLifeCycleHandler.didOpen(params);
		
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		logInfo(">> document/didChange");
		documentLifeCycleHandler.didChange(params);
		
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		logInfo(">> document/didClose");
		documentLifeCycleHandler.didClose(params);
		
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		logInfo(">> document/didSave");
		documentLifeCycleHandler.didSave(params);
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.lsp4j.services.TextDocumentService#codeAction(org.eclipse.lsp4j.CodeActionParams)
	 */
	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		logInfo(">> document/codeAction");
		CodeActionHandler handler = new CodeActionHandler(this.preferenceManager);
		return computeAsync((monitor) -> {
			waitForLifecycleJobs(monitor);
			return handler.getCodeActionCommands(params, monitor);
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.lsp4j.services.TextDocumentService#resolveCodeAction(org.eclipse.lsp4j.CodeAction)
	 */
	@Override
	public CompletableFuture<CodeAction> resolveCodeAction(CodeAction params) {
		logInfo(">> codeAction/resolve");
		// if no data property is specified, no further resolution the server can provide, so return the original result back.
		if (params.getData() == null) {
			return CompletableFuture.completedFuture(params);
		}

		CodeActionResolveHandler handler = new CodeActionResolveHandler();
		return computeAsync((monitor) -> {
			return handler.resolve(params, monitor);
		});
	}

	private void waitForLifecycleJobs(IProgressMonitor monitor) {
		JobHelpers.waitForJobs(DocumentLifeCycleHandler.DOCUMENT_LIFE_CYCLE_JOBS, monitor);
	}
	
	private void toggleCapability(boolean enabled, String id, String capability, Object options) {
		if (enabled) {
			registerCapability(id, capability, options);
		} else {
			unregisterCapability(id, capability);
		}
	}
}

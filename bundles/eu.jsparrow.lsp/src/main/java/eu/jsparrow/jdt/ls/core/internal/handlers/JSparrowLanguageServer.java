package eu.jsparrow.jdt.ls.core.internal.handlers;

import static eu.jsparrow.jdt.ls.core.internal.JavaLanguageServerPlugin.logException;
import static eu.jsparrow.jdt.ls.core.internal.JavaLanguageServerPlugin.logInfo;


import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import eu.jsparrow.jdt.ls.core.internal.BaseJDTLanguageServer;
import eu.jsparrow.jdt.ls.core.internal.JVMConfigurator;
import eu.jsparrow.jdt.ls.core.internal.JavaClientConnection.JavaLanguageClient;
import eu.jsparrow.jdt.ls.core.internal.JobHelpers;
import eu.jsparrow.jdt.ls.core.internal.LanguageServerWorkingCopyOwner;
import eu.jsparrow.jdt.ls.core.internal.ServiceStatus;
import eu.jsparrow.jdt.ls.core.internal.handlers.GenerateToStringHandler.GenerateToStringParams;
import eu.jsparrow.jdt.ls.core.internal.handlers.HashCodeEqualsHandler.CheckHashCodeEqualsResponse;
import eu.jsparrow.jdt.ls.core.internal.handlers.OverrideMethodsHandler.AddOverridableMethodParams;
import eu.jsparrow.jdt.ls.core.internal.handlers.OverrideMethodsHandler.OverridableMethodsResponse;
import eu.jsparrow.jdt.ls.core.internal.lsp.JavaProtocolExtensions;
import eu.jsparrow.jdt.ls.core.internal.managers.ProjectsManager;
import eu.jsparrow.jdt.ls.core.internal.preferences.PreferenceManager;

public class JSparrowLanguageServer extends BaseJDTLanguageServer implements LanguageServer, TextDocumentService, WorkspaceService, JavaProtocolExtensions  {

	public static final String JAVA_LSP_JOIN_ON_COMPLETION = "java.lsp.joinOnCompletion";
	public static final String JAVA_LSP_INITIALIZE_WORKSPACE = "java.lsp.initializeWorkspace";
	/**
	 * Exit code returned when JDTLanguageServer is forced to exit.
	 */
	private static final int FORCED_EXIT_CODE = 1;
	private ProjectsManager pm;
	private LanguageServerWorkingCopyOwner workingCopyOwner;
	private PreferenceManager preferenceManager;
//	private DocumentLifeCycleHandler documentLifeCycleHandler;
	private WorkspaceDiagnosticsHandler workspaceDiagnosticsHandler;
	private ClasspathUpdateHandler classpathUpdateHandler;
	private JVMConfigurator jvmConfigurator;
	private WorkspaceExecuteCommandHandler commandHandler;

//	private ProgressReporterManager progressReporterManager;
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
	
	public void connectClient(JavaLanguageClient remoteProxy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void exit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TextDocumentService getTextDocumentService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkspaceService getWorkspaceService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<String> classFileContents(TextDocumentIdentifier documentUri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<OverridableMethodsResponse> listOverridableMethods(CodeActionParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<WorkspaceEdit> addOverridableMethods(AddOverridableMethodParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<CheckHashCodeEqualsResponse> checkHashCodeEqualsStatus(CodeActionParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<WorkspaceEdit> organizeImports(CodeActionParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<WorkspaceEdit> generateToString(GenerateToStringParams params) {
		// TODO Auto-generated method stub
		return null;
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
}

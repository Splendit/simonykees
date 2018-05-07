package eu.jsparrow.standalone;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.logging.LoggingUtil;
import eu.jsparrow.rules.common.exception.RefactoringException;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 2.1.1
 */
public class Activator implements BundleActivator {

	private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	public static final String PLUGIN_ID = "eu.jsparrow.standalone"; //$NON-NLS-1$

	private static final String LIST_RULES_SELECTED_ID_KEY = "LIST.RULES.SELECTED.ID"; //$NON-NLS-1$
	private static final String STANDALONE_MODE_KEY = "STANDALONE.MODE"; //$NON-NLS-1$
	private static final String DEBUG_ENABLED = "debug.enabled"; //$NON-NLS-1$
	private static final String DEV_MODE_KEY = "dev.mode.enabled"; //$NON-NLS-1$
	private static final String LICENSE_KEY = "LICENSE"; //$NON-NLS-1$
	private static final String AGENT_URL = "URL"; //$NON-NLS-1$

	private static final String EQUINOX_DS_BUNDLE_NAME = "org.eclipse.equinox.ds"; //$NON-NLS-1$

	private RefactoringInvoker refactoringInvoker;
	ListRulesUtil listRulesUtil;

	@Inject
	StandaloneLicenseUtilService licenseService;

	public Activator() {
		this(new RefactoringInvoker(), new ListRulesUtil());
	}

	public Activator(RefactoringInvoker refactoringInvoker, ListRulesUtil listRulesUtil) {
		this.refactoringInvoker = refactoringInvoker;
		this.listRulesUtil = listRulesUtil;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		boolean debugEnabled = false;
		boolean devModeEnabled = Boolean.parseBoolean(context.getProperty(DEV_MODE_KEY));
		if (devModeEnabled) {
			debugEnabled = true;
			logger.warn("DEV MODE ENABLED"); //$NON-NLS-1$
		} else {
			debugEnabled = Boolean.parseBoolean(context.getProperty(DEBUG_ENABLED));
		}

		LoggingUtil.configureLogger(debugEnabled);

		startDeclarativeServices(context);

		logger.info(Messages.Activator_start);

		registerShutdownHook(context);

		String modeName = context.getProperty(STANDALONE_MODE_KEY);
		if (modeName != null && !modeName.isEmpty()) {

			StandaloneMode mode = StandaloneMode.valueOf(modeName);
			String listRulesId = context.getProperty(LIST_RULES_SELECTED_ID_KEY);

			switch (mode) {
			case REFACTOR:
				try {
					injectDependencies(context);
					String key = getLicenseKey(context);
					String agentUrl = getAgentUrl(context);
					if (licenseService.validate(key, agentUrl) || devModeEnabled) {
						refactoringInvoker.startRefactoring(context, new RefactoringPipeline());
					} else {
						String message = Messages.StandaloneActivator_noValidLicenseFound;
						logger.error(message);
						setExitErrorMessage(context, message);
						return;
					}
				} catch (YAMLConfigException | CoreException | MavenInvocationException | IOException
						| RefactoringException e) {
					logger.debug(e.getMessage(), e);
					logger.error(e.getMessage());
					setExitErrorMessage(context, e.getMessage());
					return;
				}
				break;
			case LIST_RULES:
				if (listRulesId != null && !listRulesId.isEmpty()) {
					listRulesUtil.listRules(listRulesId);
				} else {
					listRulesUtil.listRules();
				}
				break;
			case LIST_RULES_SHORT:
				listRulesUtil.listRulesShort();
				break;
			case LICENSE_INFO:
				injectDependencies(context);
				String key = getLicenseKey(context);
				String agentUrl = getAgentUrl(context);
				licenseService.licenseInfo(key, agentUrl);
				break;
			case TEST:
				break;
			}
		} else {
			String errorMsg = "No mode has been selected!"; //$NON-NLS-1$
			logger.error(errorMsg);
			setExitErrorMessage(context, errorMsg);
			return;
		}
	}

	void injectDependencies(BundleContext context) {
		IEclipseContext eclipseContext = EclipseContextFactory.getServiceContext(context);
		ContextInjectionFactory.inject(this, eclipseContext);
	}

	@Override
	public void stop(BundleContext context) {
		try {
			/* Unregister as a save participant */
			if (ResourcesPlugin.getWorkspace() != null) {
				ResourcesPlugin.getWorkspace()
					.forgetSavedTree(PLUGIN_ID);
				ResourcesPlugin.getWorkspace()
					.removeSaveParticipant(PLUGIN_ID);
			}
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			logger.error(e.getMessage());
		} finally {
			licenseService.stop();
			try {
				refactoringInvoker.cleanUp();
			} catch (IOException e) {
				logger.debug(e.getMessage(), e);
				logger.error(e.getMessage());
				setExitErrorMessage(context, e.getMessage());
			}
		}

		logger.info(Messages.Activator_stop);
	}

	private void registerShutdownHook(BundleContext context) {
		Runtime.getRuntime()
			.addShutdownHook(new Thread(() -> {
				licenseService.stop();
				try {
					refactoringInvoker.cleanUp();
				} catch (IOException e) {
					logger.debug(e.getMessage(), e);
					logger.error(e.getMessage());
					setExitErrorMessage(context, e.getMessage());
					return;
				}
			}));
	}

	private void startDeclarativeServices(BundleContext context) throws BundleException {
		for (Bundle b : context.getBundles()) {
			if (b.getSymbolicName()
				.startsWith(EQUINOX_DS_BUNDLE_NAME)) {

				String loggerInfo = NLS.bind(Messages.StandaloneActivator_startingBundle, b.getSymbolicName(),
						b.getState());
				logger.debug(loggerInfo);

				b.start();

				loggerInfo = NLS.bind(Messages.StandaloneActivator_bundleStarted, b.getSymbolicName(), b.getState());
				logger.debug(loggerInfo);
			}
		}
	}

	private EnvironmentInfo getEnvironmentInfo(BundleContext ctx) {
		if (ctx == null) {
			return null;
		}

		ServiceReference<?> infoRev = ctx.getServiceReference(EnvironmentInfo.class.getName());
		if (infoRev == null) {
			return null;
		}

		EnvironmentInfo envInfo = (EnvironmentInfo) ctx.getService(infoRev);
		if (envInfo == null) {
			return null;
		}
		ctx.ungetService(infoRev);

		return envInfo;
	}

	public void setExitErrorMessage(BundleContext ctx, String exitMessage) {
		String key = "eu.jsparrow.standalone.exit.message"; //$NON-NLS-1$
		EnvironmentInfo envInfo = getEnvironmentInfo(ctx);
		if (envInfo != null) {
			envInfo.setProperty(key, exitMessage);
		} else {
			System.setProperty(key, exitMessage);
		}
	}

	private String getLicenseKey(BundleContext context) {
		YAMLStandaloneConfig yamlStandaloneConfig = tryLoadStandaloneConfig(context);
		String licenseKey = ""; //$NON-NLS-1$
		if (yamlStandaloneConfig != null) {
			licenseKey = yamlStandaloneConfig.getKey();
		}
		String cmdlineLicenseKey = context.getProperty(LICENSE_KEY);
		if (cmdlineLicenseKey != null) {
			logger.info(Messages.RefactoringInvoker_OverridingConfigWithCommandLine);
			licenseKey = cmdlineLicenseKey;
		}
		return licenseKey;
	}

	private String getAgentUrl(BundleContext context) {
		YAMLStandaloneConfig yamlStandaloneConfig = tryLoadStandaloneConfig(context);
		String url = ""; //$NON-NLS-1$
		if (yamlStandaloneConfig != null) {
			url = yamlStandaloneConfig.getUrl();
		}

		String cmdlineIp = context.getProperty(AGENT_URL);
		if (cmdlineIp != null) {
			logger.info(Messages.RefactoringInvoker_OverridingConfigWithCommandLine);
			url = cmdlineIp;
		}
		return url;
	}

	private YAMLStandaloneConfig tryLoadStandaloneConfig(BundleContext context) {
		String filePath = String.format("%s/.config/jsparrow-standalone/config.yaml", System.getProperty("user.home")); //$NON-NLS-1$ //$NON-NLS-2$
		YAMLStandaloneConfig yamlStandaloneConfig = null;
		try {
			yamlStandaloneConfig = YAMLStandaloneConfig.load(new File(filePath));
		} catch (YAMLStandaloneConfigException e) {
			logger.warn(Messages.RefactoringInvoker_ConfigContainsInvalidSyntax);
		}
		return yamlStandaloneConfig;

	}
}

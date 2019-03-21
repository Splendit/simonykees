package eu.jsparrow.standalone;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.logging.LoggingUtil;
import eu.jsparrow.standalone.exceptions.StandaloneException;

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
	private static final String LICENSE_KEY = "LICENSE"; //$NON-NLS-1$
	private static final String AGENT_URL = "URL"; //$NON-NLS-1$

	// SIM-1406 org.eclipse.equinox.ds has been replaced with
	// org.apache.felix.scr
	private static final String EQUINOX_DS_BUNDLE_NAME = "org.apache.felix.scr"; //$NON-NLS-1$

	private RefactoringInvoker refactoringInvoker;
	ListRulesUtil listRulesUtil;

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

		boolean debugEnabled = Boolean.parseBoolean(context.getProperty(DEBUG_ENABLED));
		LoggingUtil.configureLogger(debugEnabled);

		startDeclarativeServices(context);
		// Put both together because it looks nicer
		String startMessage = String.format("%s", Messages.Activator_start); //$NON-NLS-1$
		logger.info(startMessage);
		registerShutdownHook(context);
		StandaloneMode mode = parseMode(context);
		String listRulesId = context.getProperty(LIST_RULES_SELECTED_ID_KEY);
		switch (mode) {
		case REFACTOR:
			refactor(context);
			break;
		case LIST_RULES:
			listRules(listRulesId);
			break;
		case LIST_RULES_SHORT:
			listRulesUtil.listRulesShort();
			break;
		case LICENSE_INFO:
			printLicenseInfo(context);
			break;
		case TEST:
			break;
		default:
			String errorMsg = "No mode has been selected!"; //$NON-NLS-1$
			logger.error(errorMsg);
			setExitErrorMessageAndCleanUp(context, errorMsg);
		}
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
			cleanUp(context);
		}

		logger.info(Messages.Activator_stop);
	}

	private void printLicenseInfo(BundleContext context) {
		licenseService = getStandaloneLicenseUtilService();
		String key = getLicenseKey(context);
		String agentUrl = getAgentUrl(context);
		licenseService.licenseInfo(key, agentUrl);
	}

	private void listRules(String listRulesId) {
		if (listRulesId != null && !listRulesId.isEmpty()) {
			listRulesUtil.listRules(listRulesId);
		} else {
			listRulesUtil.listRules();
		}
	}

	private void refactor(BundleContext context) {
		try {

			String key = getLicenseKey(context);
			String agentUrl = getAgentUrl(context);

			licenseService = getStandaloneLicenseUtilService();
			if (licenseService.validate(key, agentUrl)) {
				refactoringInvoker.startRefactoring(context);
			} else {
				String message = Messages.StandaloneActivator_noValidLicenseFound;
				logger.error(message);
				setExitErrorMessageAndCleanUp(context, message);
			}
		} catch (StandaloneException e) {
			logger.debug(e.getMessage(), e);
			logger.error(e.getMessage());
			setExitErrorMessageAndCleanUp(context, e.getMessage());
		}
	}

	private void registerShutdownHook(BundleContext context) {
		Runtime.getRuntime()
			.addShutdownHook(new Thread(() -> cleanUp(context)));
	}

	private void cleanUp(BundleContext context) {
		StandaloneMode mode = parseMode(context);
		if (licenseService != null && (mode == StandaloneMode.REFACTOR || mode == StandaloneMode.LICENSE_INFO)) {
			licenseService.stop();
		}
		try {
			refactoringInvoker.cleanUp();
		} catch (IOException | CoreException e) {
			logger.debug(e.getMessage(), e);
			logger.error(e.getMessage());
			setExitErrorMessage(context, e.getMessage());
		}
	}

	private StandaloneMode parseMode(BundleContext context) {
		String value = context.getProperty(STANDALONE_MODE_KEY);
		return StandaloneMode.fromString(value);
	}

	private void startDeclarativeServices(BundleContext context) throws BundleException {
		for (Bundle b : context.getBundles()) {
			if (b.getSymbolicName()
				.startsWith(EQUINOX_DS_BUNDLE_NAME)) {
				b.start();
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

	public void setExitErrorMessageAndCleanUp(BundleContext ctx, String exitMessage) {
		cleanUp(ctx);

		setExitErrorMessage(ctx, exitMessage);
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
		YAMLStandaloneConfig yamlStandaloneConfig = tryLoadStandaloneConfig();
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
		YAMLStandaloneConfig yamlStandaloneConfig = tryLoadStandaloneConfig();
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

	private YAMLStandaloneConfig tryLoadStandaloneConfig() {
		Path filePath = Paths.get(System.getProperty("user.home"), ".config", "jsparrow-standalone"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		YAMLStandaloneConfig yamlStandaloneConfig = null;

		Optional<String> configFile = new ConfigFinder().getYAMLFilePath(filePath);
		if (configFile.isPresent()) {
			try {
				yamlStandaloneConfig = YAMLStandaloneConfig.load(new File(configFile.get()));
			} catch (YAMLStandaloneConfigException e) {
				logger.warn(Messages.RefactoringInvoker_ConfigContainsInvalidSyntax);
			}
		} else {
			logger.info("No config.yaml file found in '{}'", filePath); //$NON-NLS-1$
		}

		return yamlStandaloneConfig;
	}

	StandaloneLicenseUtilService getStandaloneLicenseUtilService() {
		return StandaloneLicenseUtil.get();
	}
}

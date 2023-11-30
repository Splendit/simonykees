package eu.jsparrow.independent;

import static eu.jsparrow.independent.ContextPropertyHelper.DEBUG_ENABLED;
import static eu.jsparrow.independent.ContextPropertyHelper.LICENSE_KEY;
import static eu.jsparrow.independent.ContextPropertyHelper.STANDALONE_MODE_KEY;
import static eu.jsparrow.independent.ContextPropertyHelper.getProperty;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.independent.ConfigFinder.ConfigType;
import eu.jsparrow.independent.exceptions.StandaloneException;
import eu.jsparrow.independent.util.ProxyUtils;
import eu.jsparrow.logging.LoggingUtil;

public class JSparrowIndependentHandler {
	private static final String LIST_RULES_SELECTED_ID_KEY = "LIST.RULES.SELECTED.ID"; //$NON-NLS-1$

	private static final String AGENT_URL = "URL"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(JSparrowIndependentHandler.class);
	private static JSparrowIndependentHandler instance;

	private final RefactoringInvoker refactoringInvoker;
	private final ListRulesUtil listRulesUtil;
	private StandaloneLicenseUtilService licenseService;

	public static JSparrowIndependentHandler getInstance() {
		return instance;
	}

	public static void start() throws Exception {
		if (instance != null) {
			throw new IllegalStateException(JSparrowIndependentHandler.class.getName() + " has already been started."); //$NON-NLS-1$
		}

		BundleContext context = Activator.getContext();
		if (context == null) {
			throw new IllegalStateException(Activator.class.getName() + " has not been started."); //$NON-NLS-1$
		}
		String osgi_instance_area_default = context.getProperty("osgi.instance.area.default"); //$NON-NLS-1$
		instance = new JSparrowIndependentHandler();
		instance.doStart(context);
	}

	public static void stop(BundleContext context) {
		if (instance != null) {
			instance.doStop(context);
			instance = null;
		}
	}

	private JSparrowIndependentHandler() {
		this.refactoringInvoker = new RefactoringInvoker();
		this.listRulesUtil = new ListRulesUtil();
	}

	private void doStart(BundleContext context) throws Exception {
		boolean debugEnabled = Boolean.parseBoolean(getProperty(context, DEBUG_ENABLED));
		LoggingUtil.configureLogger(debugEnabled);

		registerShutdownHook(context);
		ProxyUtils.configureProxy(context);
		StandaloneMode mode = parseMode(context);
		String listRulesId = context.getProperty(LIST_RULES_SELECTED_ID_KEY);
		switch (mode) {
		case REFACTOR:
			refactor(context);
			break;
		case REPORT:
			runInReportMode(context);
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
			doSetExitErrorMessageAndCleanUp(context, errorMsg);
		}
	}

	private void registerShutdownHook(BundleContext context) {
		Runtime.getRuntime()
			.addShutdownHook(new Thread(() -> cleanUp(context)));
	}

	private void cleanUp(BundleContext context) {
		StandaloneMode mode = parseMode(context);
		if (licenseService != null && (mode == StandaloneMode.REFACTOR || mode == StandaloneMode.LICENSE_INFO)) {
			licenseService.stop(getAgentUrl(context));
		}

		refactoringInvoker.cleanUp();
	}

	private StandaloneMode parseMode(BundleContext context) {
		String value = getProperty(context, STANDALONE_MODE_KEY);
		return StandaloneMode.fromString(value);
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
		Path filePath = Paths.get(System.getProperty("user.home"), ".config", "jsparrow-independent"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		YAMLStandaloneConfig yamlStandaloneConfig = null;

		Optional<String> configFile = new ConfigFinder().getYAMLFilePath(filePath, ConfigType.CONFIG_FILE);
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

	/**
	 * @see Activator#runInReportMode(BundleContext)
	 * 
	 * @param context
	 */
	private void refactor(BundleContext context) {
		String key = getLicenseKey(context);
		String agentUrl = getAgentUrl(context);
		licenseService = getStandaloneLicenseUtilService();

		try {
			boolean validLicense = licenseService.validate(key, agentUrl);
			if (validLicense) {
				refactoringInvoker.startRefactoring(context);
			} else {
				String message = Messages.StandaloneActivator_noValidLicenseFound;
				logger.error(message);
				doSetExitErrorMessageAndCleanUp(context, message);
			}
		} catch (StandaloneException e) {
			logger.debug(e.getMessage(), e);
			logger.error(e.getMessage());
			doSetExitErrorMessageAndCleanUp(context, e.getMessage());
		}
	}

	private String getLicenseKey(BundleContext context) {
		YAMLStandaloneConfig yamlStandaloneConfig = tryLoadStandaloneConfig();
		String licenseKey = ""; //$NON-NLS-1$
		if (yamlStandaloneConfig != null) {
			licenseKey = yamlStandaloneConfig.getKey();
		}
		String cmdlineLicenseKey = getProperty(context, LICENSE_KEY);
		if (cmdlineLicenseKey != null) {
			logger.info(Messages.RefactoringInvoker_OverridingConfigWithCommandLine);
			licenseKey = cmdlineLicenseKey;
		}
		return licenseKey;
	}

	StandaloneLicenseUtilService getStandaloneLicenseUtilService() {
		return StandaloneLicenseUtil.get();
	}

	public void doSetExitErrorMessageAndCleanUp(BundleContext ctx, String exitMessage) {
		cleanUp(ctx);

		doSetExitErrorMessage(ctx, exitMessage);
	}

	public void doSetExitErrorMessage(BundleContext ctx, String exitMessage) {
		String key = "eu.jsparrow.independent.exit.message"; //$NON-NLS-1$
		EnvironmentInfo envInfo = getEnvironmentInfo(ctx);
		if (envInfo != null) {
			envInfo.setProperty(key, exitMessage);
		} else {
			System.setProperty(key, exitMessage);
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

	/**
	 * Contrary to {@link Activator#refactor(BundleContext)}, this method does
	 * not stop JMP if the license is invalid.
	 * 
	 * @param context
	 *            all the settings etc.
	 */
	private void runInReportMode(BundleContext context) {
		try {
			refactoringInvoker.runInReportMode(context);
		} catch (StandaloneException e) {
			logger.debug(e.getMessage(), e);
			logger.error(e.getMessage());
			doSetExitErrorMessageAndCleanUp(context, e.getMessage());
		}
	}

	private void listRules(String listRulesId) {
		if (listRulesId != null && !listRulesId.isEmpty()) {
			listRulesUtil.listRules(listRulesId);
		} else {
			listRulesUtil.listRules();
		}
	}

	private void printLicenseInfo(BundleContext context) {
		licenseService = getStandaloneLicenseUtilService();
		String key = getLicenseKey(context);
		String agentUrl = getAgentUrl(context);
		try {
			licenseService.licenseInfo(key, agentUrl);
		} catch (StandaloneException e) {
			logger.debug(e.getMessage(), e);
			logger.error(e.getMessage());
			doSetExitErrorMessage(context, e.getMessage());
		}
	}

	private void doStop(BundleContext context) {
		try {
			/* Unregister as a save participant */
			if (ResourcesPlugin.getWorkspace() != null) {
				ResourcesPlugin.getWorkspace()
					.forgetSavedTree(Activator.PLUGIN_ID);
				ResourcesPlugin.getWorkspace()
					.removeSaveParticipant(Activator.PLUGIN_ID);
			}
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			logger.error(e.getMessage());
		} finally {
			cleanUp(context);
		}
	}

	RefactoringInvoker getRefactoringInvoker() {
		return refactoringInvoker;
	}

	ListRulesUtil getListRulesUtil() {
		return listRulesUtil;
	}

	StandaloneLicenseUtilService getLicenseService() {
		return licenseService;
	}

}

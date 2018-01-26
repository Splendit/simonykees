package eu.jsparrow.standalone;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.logging.LoggingUtil;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 2.1.1
 */
public class Activator implements BundleActivator {

	private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	public static final String PLUGIN_ID = "eu.jsparrow.standalone"; //$NON-NLS-1$

	private static final String LIST_RULES = "LIST.RULES"; //$NON-NLS-1$
	private static final String LIST_RULES_SHORT = "LIST.RULES.SHORT"; //$NON-NLS-1$
	private static final String LIST_RULES_SELECTED_ID = "LIST.RULES.SELECTED.ID"; //$NON-NLS-1$
	private static final String DEBUG_ENABLED = "debug.enabled"; //$NON-NLS-1$

	@Override
	public void start(BundleContext context) throws Exception {
		logger.info(Messages.Activator_start);

		boolean listRules = Boolean.parseBoolean(context.getProperty(LIST_RULES));
		boolean listRulesShort = Boolean.parseBoolean(context.getProperty(LIST_RULES_SHORT));
		String listRulesId = context.getProperty(LIST_RULES_SELECTED_ID);

		boolean debugEnabled = Boolean.parseBoolean(context.getProperty(DEBUG_ENABLED));

		LoggingUtil.configureLogger(debugEnabled);

		if (listRules) {
			if (listRulesId != null && !listRulesId.isEmpty()) {
				ListRulesUtil.listRules(listRulesId);
			} else {
				ListRulesUtil.listRules();
			}
		} else if (listRulesShort) {
			ListRulesUtil.listRulesShort();
		} else {
			try {
				RefactorUtil.startRefactoring(context);
			} catch (YAMLConfigException yce) {
				logger.debug(yce.getMessage(), yce);
				logger.error(yce.getMessage());
				setExitErrorMessage(context, yce.getMessage());
			}
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
			RefactorUtil.cleanUp();
		}

		logger.info(Messages.Activator_stop);
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

	private void setExitErrorMessage(BundleContext ctx, String exitMessage) {
		String key = "eu.jsparrow.standalone.exit.message"; //$NON-NLS-1$
		EnvironmentInfo envInfo = getEnvironmentInfo(ctx);
		if (envInfo != null) {
			envInfo.setProperty(key, exitMessage);
		} else {
			System.setProperty(key, exitMessage);
		}
	}
}

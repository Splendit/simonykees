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

	@Override
	public void start(BundleContext context) throws Exception {
		logger.info(Messages.Activator_start);

		String modeName = context.getProperty(STANDALONE_MODE_KEY);
		if (modeName != null && !modeName.isEmpty()) {

			StandaloneMode mode = StandaloneMode.valueOf(modeName);
			String listRulesId = context.getProperty(LIST_RULES_SELECTED_ID_KEY);

			switch (mode) {
			case REFACTOR:
				try {
					RefactorUtil.startRefactoring(context);
				} catch (YAMLConfigException yce) {
					logger.debug(yce.getMessage(), yce);
					logger.error(yce.getMessage());
					setExitErrorMessage(context, yce.getMessage());
				}
				break;
			case LIST_RULES:
				ListRulesUtil.listRules();
				break;
			case LIST_RULES_SHORT:
				ListRulesUtil.listRulesShort();
				break;
			case LIST_RULES_WITH_SELECTED_ID:
				if (listRulesId != null && !listRulesId.isEmpty()) {
					ListRulesUtil.listRules(listRulesId);
				} else {
					String errorMsg = "Please specify rule IDs for this mode!"; //$NON-NLS-1$
					logger.error(errorMsg);
					setExitErrorMessage(context, errorMsg);
				}
				break;
			case TEST:
				break;
			}
		} else {
			String errorMsg = "No mode has been selected!"; //$NON-NLS-1$
			logger.error(errorMsg);
			setExitErrorMessage(context, errorMsg);
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
			logger.error(e.getMessage(), e);
		} finally {
			RefactorUtil.cleanUp();
		}

		logger.info(Messages.Activator_stop);
	}

	private static EnvironmentInfo getEnvironmentInfo(BundleContext ctx) {
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

	public static void setExitErrorMessage(BundleContext ctx, String exitMessage) {
		String key = "eu.jsparrow.standalone.exit.message"; //$NON-NLS-1$
		EnvironmentInfo envInfo = getEnvironmentInfo(ctx);
		if (envInfo != null) {
			envInfo.setProperty(key, exitMessage);
		} else {
			System.setProperty(key, exitMessage);
		}
	}
}

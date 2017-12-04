package eu.jsparrow.standalone;

import org.eclipse.core.resources.ResourcesPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final String LIST_RULES = "LIST.RULES"; //$NON-NLS-1$
	private static final String LIST_RULES_SHORT = "LIST.RULES.SHORT"; //$NON-NLS-1$
	private static final String LIST_RULES_SELECTED_ID = "LIST.RULES.SELECTED.ID"; //$NON-NLS-1$

	@Override
	public void start(BundleContext context) throws Exception {
		logger.info(Messages.Activator_start);

		boolean listRules = Boolean.parseBoolean(context.getProperty(LIST_RULES));
		boolean listRulesShort = Boolean.parseBoolean(context.getProperty(LIST_RULES_SHORT));
		String listRulesId = context.getProperty(LIST_RULES_SELECTED_ID);

		if (listRules) {
			if (listRulesId != null && !listRulesId.isEmpty()) {
				ListRulesUtil.listRules(listRulesId);
			} else {
				ListRulesUtil.listRules();
			}
		} else if (listRulesShort) {
			ListRulesUtil.listRulesShort();
		} else {
			RefactorUtil.startRefactoring(context);
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
}

package eu.jsparrow.independent;

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

	public static final String PLUGIN_ID = "eu.jsparrow.independent"; //$NON-NLS-1$

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		logger.info(Messages.Activator_start);
		Activator.context = context;
		JSparrowIndependentHandler.start();
	}

	@Override
	public void stop(BundleContext context) {
		JSparrowIndependentHandler.stop(context);
		Activator.context = null;
		logger.info(Messages.Activator_stop);
	}

	private JSparrowIndependentHandler getNonNullableJSparrowHandler() {
		JSparrowIndependentHandler handler = JSparrowIndependentHandler.getInstance();
		if (handler == null) {
			throw new IllegalStateException(JSparrowIndependentHandler.class.getName() + " has not been started."); //$NON-NLS-1$
		}
		return handler;
	}

	public void setExitErrorMessageAndCleanUp(BundleContext ctx, String exitMessage) {
		getNonNullableJSparrowHandler().doSetExitErrorMessageAndCleanUp(ctx, exitMessage);
	}

	public void setExitErrorMessage(BundleContext ctx, String exitMessage) {
		getNonNullableJSparrowHandler().doSetExitErrorMessage(ctx, exitMessage);
	}
}

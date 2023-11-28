package eu.jsparrow.independent;

import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 2.1.1
 */
public class JSparrowIndependentDelegate {
	private static JSparrowIndependentDelegate instance;

	private final RefactoringInvoker refactoringInvoker;
	private final ListRulesUtil listRulesUtil;
	private StandaloneLicenseUtilService licenseService;

	static JSparrowIndependentDelegate getInstance() {
		return instance;
	}

	static void start() {
		if (instance != null) {
			throw new IllegalStateException(JSparrowIndependentDelegate.class.getName() +" has already been started."); //$NON-NLS-1$
		}
		
		BundleContext context = Activator.getContext();
		if (context == null) {
			throw new IllegalStateException(Activator.class.getName() + " has not been started."); //$NON-NLS-1$
		}

		instance = new JSparrowIndependentDelegate();
		instance.doStart(context);
	}

	static void stop(BundleContext context) {
		if (instance != null) {
			instance.doStop(context);
			instance = null;
		}
	}

	private JSparrowIndependentDelegate() {
		this.refactoringInvoker = new RefactoringInvoker();
		this.listRulesUtil = new ListRulesUtil();
	}

	private void doStart(BundleContext context) {

	}

	private void doStop(BundleContext context) {

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

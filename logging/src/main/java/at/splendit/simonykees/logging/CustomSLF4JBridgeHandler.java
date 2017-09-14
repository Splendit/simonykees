package at.splendit.simonykees.logging;

import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * This is a custom implementation for {@link SLF4JBridgeHandler} to redirect
 * all java.util.logging activity to a special logger called "jul". This is done
 * to separate the logging of netlicensing and jSparrow in order to provide
 * cleaner logging files.
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class CustomSLF4JBridgeHandler extends SLF4JBridgeHandler {

	/**
	 * Adds a {@link CustomSLF4JBridgeHandler} to jul's root logger
	 */
	public static void install() {
		LogManager.getLogManager().getLogger("").addHandler(new CustomSLF4JBridgeHandler()); //$NON-NLS-1$
	}

	/**
	 * in the original method, the logger name is derived directly from
	 * java.util.logging and therefore it cannot be redirected in a separate file
	 * because the derived logger is usually the root logger. Hence, the overridden
	 * method returns a reference to the logger named "jul" in logback.xml to make
	 * this work.
	 */
	@Override
	protected Logger getSLF4JLogger(LogRecord record) {
		return LoggerFactory.getLogger("jul"); //$NON-NLS-1$
	}

}

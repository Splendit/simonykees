package eu.jsparrow.sample.postRule.allRules;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.jsparrow.sample.utilities.HelloWorld;

public class TestReplaceWrongClassForLoggerJavaLoggingRule {
	private TestReplaceWrongClassForLoggerJavaLoggingRule() {
		throw new IllegalStateException("Utility class");
	}

	static final Class<HelloWorld> HRLLO_WORLD_CLASS = HelloWorld.class;
	static final Logger LOGGER = Logger.getLogger(HelloWorld.class.getName());

	static class ClassWithLoggingMethodCaller {
		String msg;
		String sourceClass;
		String sourceMethod;
		Level level;
		ResourceBundle resourceBundle;
		Throwable thrown;

		void callLoggingMethods() {
			Logger.getLogger(ClassWithLoggingMethodCaller.class.getName())
				.config(msg);
			Logger.getLogger(ClassWithLoggingMethodCaller.class.getName())
				.entering(sourceClass, sourceMethod);
			Logger.getLogger(ClassWithLoggingMethodCaller.class.getName())
				.exiting(sourceClass, sourceMethod);
			Logger.getLogger(ClassWithLoggingMethodCaller.class.getName())
				.fine(msg);
			Logger.getLogger(ClassWithLoggingMethodCaller.class.getName())
				.finer(msg);
			Logger.getLogger(ClassWithLoggingMethodCaller.class.getName())
				.finest(msg);
			Logger.getLogger(ClassWithLoggingMethodCaller.class.getName())
				.info(msg);
			Logger.getLogger(ClassWithLoggingMethodCaller.class.getName())
				.log(level, msg);
			Logger.getLogger(ClassWithLoggingMethodCaller.class.getName())
				.logp(level, sourceClass, sourceMethod, msg);
			Logger.getLogger(ClassWithLoggingMethodCaller.class.getName())
				.logrb(level, sourceClass, sourceMethod, resourceBundle, msg);
			Logger.getLogger(ClassWithLoggingMethodCaller.class.getName())
				.severe(msg);
			Logger.getLogger(ClassWithLoggingMethodCaller.class.getName())
				.throwing(sourceClass, sourceMethod, thrown);
			Logger.getLogger(ClassWithLoggingMethodCaller.class.getName())
				.warning(msg);
		}
	}
}
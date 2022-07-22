package eu.jsparrow.sample.preRule;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.jsparrow.sample.utilities.HelloWorld;

public class TestReplaceWrongClassForLoggerJavaLoggingRule {
	static final Class<HelloWorld> HRLLO_WORLD_CLASS = HelloWorld.class;
	static final Logger LOGGER = Logger.getLogger(HelloWorld.class.getName());

	static class ClassWithLoggingMethodCaller {
		String msg;
		String sourceClass, sourceMethod;
		Level level;
		ResourceBundle resourceBundle;
		Throwable thrown;

		void callLoggingMethods() {
			Logger.getLogger(HelloWorld.class.getName())
				.config(msg);
			Logger.getLogger(HelloWorld.class.getName())
				.entering(sourceClass, sourceMethod);
			Logger.getLogger(HelloWorld.class.getName())
				.exiting(sourceClass, sourceMethod);
			Logger.getLogger(HelloWorld.class.getName())
				.fine(msg);
			Logger.getLogger(HelloWorld.class.getName())
				.finer(msg);
			Logger.getLogger(HelloWorld.class.getName())
				.finest(msg);
			Logger.getLogger(HelloWorld.class.getName())
				.info(msg);
			Logger.getLogger(HelloWorld.class.getName())
				.log(level, msg);
			Logger.getLogger(HelloWorld.class.getName())
				.logp(level, sourceClass, sourceMethod, msg);
			Logger.getLogger(HelloWorld.class.getName())
				.logrb(level, sourceClass, sourceMethod, resourceBundle, msg);
			Logger.getLogger(HelloWorld.class.getName())
				.severe(msg);
			Logger.getLogger(HelloWorld.class.getName())
				.throwing(sourceClass, sourceMethod, thrown);
			Logger.getLogger(HelloWorld.class.getName())
				.warning(msg);
		}
	}
}
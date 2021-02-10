package eu.jsparrow.sample.postRule.allRules;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.sample.utilities.Person;

public class RemoveRedundantTypeCastRule {

	private static final Logger logger = LoggerFactory.getLogger(RemoveRedundantTypeCastRule.class);

	public void undefinedWildCardTypeArgument_shouldNotTransform() {
		addUserListener((GenericFutureListener<Future<Person>>) res1 -> {
			res1.get()
				.getBirthday();
			res1.isDone();
		});
	}

	public void undefinedWildCardTypeArgument_shouldTransform() {
		addUserListener((Future<Person> res1) -> {
			res1.get()
				.getBirthday();
			res1.isDone();
		});

		addPersonListener(res1 -> {
			res1.get()
				.getBirthday();
			res1.isDone();
		});
	}

	public void usingVaragMethod_shouldNotTransform(MethodHandle methodHandle) throws Throwable {
		varArgObjects((Runnable) () -> {
		});
		methodHandle.invoke((Runnable) () -> {
		});
	}

	private void addUserListener(GenericFutureListener<? extends Future<? super Person>> listener) {
		try {
			listener.operationComplete(null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void addPersonListener(GenericFutureListener<Future<Person>> listener) {
		try {
			listener.operationComplete(null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void varArgObjects(Object... objects) {
		final int numObjects = objects.length;// just to use the param once
	}

}

interface GenericFutureListener<F extends Future<?>> {
	void operationComplete(F future) throws Exception;
}

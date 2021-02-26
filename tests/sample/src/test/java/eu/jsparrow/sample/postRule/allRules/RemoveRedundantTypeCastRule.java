package eu.jsparrow.sample.postRule.allRules;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;
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

	public void castingNeededForTypeVariables_shouldNotTransform(Object o) {
		/* SIM-1885 */
		findFooSomething((Foo) getFoo());
		findFooSomething((Foo) findFooSomething(o));
		findFooSomething((Foo) getFooSubtype());
		findFooSomething((Foo) getFooSubtype()).fooMethod();

		findFooSomething((Foo) getFoo()).fooMethod();
	}

	public void castingInMethodArguments_shouldTransform(Object o) {
		/* SIM-1885 */
		useFoo(getFooSubtype()).fooMethod();
	}

	public void usingAmbiguousOverloadedMethods() {
		/**
		 * Should not transform. Corner case similar to
		 * io.vertx.core.http.Http2ClientTest:
		 * 
		 * <code>
		 * resp.putHeader("juu_response", (List<String>) Arrays.asList("juu_value_1", "juu_value_2"));
		 * </code>
		 */
		overloadedMethod("", (List<String>) Arrays.asList("", ""));

		/*
		 * Should transform. Method resolution works in these cases.
		 */
		overloadedMethod(1, Arrays.asList("", ""));
		overloadedMethod("", Arrays.asList("", ""), 1);
	}

	public void overloadedMethod(String value, List<String> values, int i) {
	}

	public void overloadedMethod(String value, List<String> values) {
	}

	public void overloadedMethod(CharSequence value, List<CharSequence> values) {
	}

	public void overloadedMethod(Integer value, List<String> values) {
	}

	private Foo getFoo() {
		return null;
	}

	private <T extends Foo> T getFooSubtype() {
		return (T) new Foo();
	}

	private <T extends Object> T findFooSomething(T t) {
		return t;
	}

	<T extends Foo> T useFoo(Foo foo) {
		return (T) new Foo();
	}

	class Foo {
		public void fooMethod() {
		}
	}

}

interface GenericFutureListener<F extends Future<?>> {
	void operationComplete(F future) throws Exception;
}

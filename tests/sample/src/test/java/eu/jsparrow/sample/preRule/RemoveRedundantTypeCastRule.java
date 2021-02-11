package eu.jsparrow.sample.preRule;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.Future;

import eu.jsparrow.sample.utilities.Person;

public class RemoveRedundantTypeCastRule {
	
	public void undefinedWildCardTypeArgument_shouldNotTransform() {
		addUserListener((GenericFutureListener<Future<Person>>) res1 -> {
			res1.get().getBirthday();
			res1.isDone();
		});
	}
	
	public void undefinedWildCardTypeArgument_shouldTransform() {
		addUserListener((GenericFutureListener<Future<Person>>) (Future<Person> res1) -> {
			res1.get().getBirthday();
			res1.isDone();
		});
		
		addPersonListener((GenericFutureListener<Future<Person>>) res1 -> {
			res1.get().getBirthday();
			res1.isDone();
		});
	}
	
	public void usingVaragMethod_shouldNotTransform(MethodHandle methodHandle) throws Throwable {
		varArgObjects((Runnable)() -> {});
		methodHandle.invoke((Runnable)() -> {});
	}
	
	private void addUserListener(GenericFutureListener<? extends Future<? super Person>> listener) {
		try {
			listener.operationComplete(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void addPersonListener(GenericFutureListener<Future<Person>> listener) {
		try {
			listener.operationComplete(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void varArgObjects(Object... objects) {
		int numObjects = objects.length;//just to use the param once
	}

}

interface GenericFutureListener<F extends Future<?>> {
    void operationComplete(F future) throws Exception;
}

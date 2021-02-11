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
	
	
	public void castingNeededForTypeVariables_shouldNotTransform(Object o) {
		/* SIM-1885 */
		findFooSomething((Foo)getFoo());
		findFooSomething((Foo)findFooSomething(o));
		findFooSomething((Foo)getFooSubtype(o));
		findFooSomething((Foo)getFooSubtype(o)).fooMethod();
		
		findFooSomething((Foo)getFoo()).fooMethod();
	}
	
	public void castingInMethodArguments_shouldTransform(Object o) {
		/* SIM-1885 */
		useFoo((Foo)getFooSubtype(o)).fooMethod();
	}
	
	private Foo getFoo() {
		return null;
	}
	
	private <T extends Foo> T getFooSubtype(Object o) {
		return (T)new Foo();
	}
	
	
	private <T extends Object> T findFooSomething(T t) {
		return t;
	}
	
	<T extends Foo> T useFoo(Foo foo) {
		return (T)new Foo();
	}
	
	class Foo {
		public void fooMethod() {}
	}

}

interface GenericFutureListener<F extends Future<?>> {
    void operationComplete(F future) throws Exception;
}



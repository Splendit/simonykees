package eu.jsparrow.sample.postRule.allRules;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TestFunctionalInterface2Rule {
	private static final Logger logger = LoggerFactory.getLogger(TestFunctionalInterface2Rule.class);
	Object fields;
	MouseAdapter a = new MouseAdapter() {

		@Override
		public void mouseMoved(MouseEvent e) {
			e.getX();
			e.getY();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			mouseMoved(e);
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	};

	public void setFields(Object fields) {
		final Object proxyFields = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { List.class },
				(Object proxy, Method method, Object[] args) -> method.invoke(fields, args));
		this.fields = proxyFields;
	}

	public void setFields2(Object fields) {
		final Object proxyFields = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { List.class },
				new InvocationHandler() {

					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						doSomething();
						return method.invoke(fields, args);
					}

					private void doSomething() {

					}
				});
		this.fields = proxyFields;
	}

	public void testNotCorrectTypeVarDecl() {
		final Object o = new Runnable() {
			@Override
			public void run() {
			}
		};
	}

	public void testCorrectTypeVarDecl() {
		final Runnable r = () -> {
		};
	}

	public void testNotCorrectTypeAssignment() {
		final Object o;
		o = new Runnable() {
			@Override
			public void run() {
			}
		};
	}

	public void testCorrectTypeAssignment() {
		final Runnable r;
		r = () -> {
		};
	}

	public void testInlineNewClasExpression() {
		new Runnable() {

			@Override
			public void run() {

			}
		};
	}

	public void testCorrectForInitializer() {
		for (Runnable r = () -> {

		}; true;) {
			break;
		}
	}

	public void testNotCorrectForInitializer() {
		for (Object r = new Runnable() {

			@Override
			public void run() {

			}
		}; true;) {
			break;
		}
	}

	public void testCorrectTryWithResourceHeader() {
		try (Closeable c = () -> {

		}) {

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void testNotCorrectTryWithResourceHeader() {
		try (AutoCloseable c = new Closeable() {

			@Override
			public void close() throws IOException {

			}
		}) {

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void testCorrectMethodInvocation() {
		doSomethingRunnable(() -> {

		});
	}

	public void testNotCorrectMethodInvocation() {
		doSomething(new Runnable() {

			@Override
			public void run() {

			}
		});
	}

	public void testCorrectMethodInvocation2ndParam() {
		doSomethingRunnable("addition", () -> {

		});
	}

	public void testNotCorrectMethodInvocation2ndParam() {
		doSomething("addition", new Runnable() {

			@Override
			public void run() {

			}
		});
	}

	public void testCorrectClassInstanciation() {
		final MyRunnableClass myRunnableClass = new MyRunnableClass(() -> {
		});
	}

	public void testNotCorrectClassInstanciation() {
		final MyClass myClass = new MyClass(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

			}
		});
	}

	private void doSomething(Object o) {

	}

	private void doSomethingRunnable(Runnable o) {

	}

	private void doSomething(String s, Object o) {

	}

	private void doSomethingRunnable(String s, Runnable o) {

	}

	private class MyRunnableClass {
		public MyRunnableClass(Runnable run) {
		}
	}

	private class MyClass {
		public MyClass(Object run) {
		}
	}
}

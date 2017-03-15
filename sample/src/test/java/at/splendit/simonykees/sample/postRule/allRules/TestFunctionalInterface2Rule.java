package at.splendit.simonykees.sample.postRule.allRules;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public abstract class TestFunctionalInterface2Rule {
	Object fields;

	@SuppressWarnings("unchecked")
	public void setFields(Object fields) {
		Object proxyFields = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { List.class },
				(Object proxy, Method method, Object[] args) -> {
					return method.invoke(fields, args);
				});
		this.fields = proxyFields;
	}

	@SuppressWarnings("unchecked")
	public void setFields2(Object fields) {
		Object proxyFields = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { List.class },
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
}

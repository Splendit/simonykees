package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
@SuppressWarnings("serial")
public class ReImplementingInterfaceRule<E> extends SuperClass3<E> implements Iterable<E> {
	@Override
	public int compareTo(E o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}
}

@SuppressWarnings("serial")
class SuperClass1<E> extends ArrayList<E> {

}

@SuppressWarnings("serial")
class SuperClass2<E> extends SuperClass1<E> implements Comparable<E>, Observer {

	@Override
	public int compareTo(E o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}

}

@SuppressWarnings("serial")
class SuperClass3<E> extends SuperClass2<E> {

	@Override
	public int compareTo(E o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}

}

class SuperClass4 implements Observer {

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}

}

class SuperClass5 extends SuperClass4 {

}
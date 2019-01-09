package eu.jsparrow.sample.utilities;

import java.util.concurrent.Callable;

/**
 * 
 * Named after hudson.model.Queue in jenkins-core
 *
 */
public class Queue {
	public void withLock(Runnable runnable) {

	}

	public <V> V withLock(Callable<V> callable) throws Exception {
		return null;
	}
}
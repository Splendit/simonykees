package eu.jsparrow.sample.postRule.allRules;

public abstract class TestFunctionalInterface3Rule {

	static {
		staticGetRunnableHash();
	}

	static {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				hashCode();
			}
		};
		r.run();
	}

	static {
		staticGetRunnable();
	}

	static {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				getClass();
			}
		};
		r.run();
	}

	{
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				hashCode();
			}
		};
		r.run();
	}

	{
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				getClass();
			}
		};
		r.run();
	}

	@Override
	public int hashCode() {
		return 0;
	}

	private void sampleMethod() {

	}

	private static Runnable staticGetRunnableHash() {
		return new Runnable() {
			@Override
			public void run() {
				hashCode();
			}
		};
	}

	public Runnable getRunnableHash() {
		return new Runnable() {
			@Override
			public void run() {
				hashCode();
			}
		};
	}

	public Runnable runSampleMethod() {
		return this::sampleMethod;
	}

	private static Runnable staticGetRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				getClass();
			}
		};
	}

	public Runnable getRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				getClass();
			}
		};
	}

}

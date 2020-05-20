package eu.jsparrow.sample.preRule;

public abstract class TestFunctionalInterface3Rule {

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

	static {
		staticGetRunnableHash();
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
		return new Runnable() {
			@Override
			public void run() {
				sampleMethod();
			}
		};
	}


	static {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				hashCode();
			}
		};
		r.run();
	}

	{
		Runnable r = new Runnable() {
			@Override
			public void run() {
				hashCode();
			}
		};
		r.run();
	}

	private static Runnable staticGetRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				getClass();
			}
		};
	}

	static {
		staticGetRunnable();
	}

	public Runnable getRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				getClass();
			}
		};
	}

	static {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				getClass();
			}
		};
		r.run();
	}

	{
		Runnable r = new Runnable() {
			@Override
			public void run() {
				getClass();
			}
		};
		r.run();
	}

}

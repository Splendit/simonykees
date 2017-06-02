package at.splendit.simonykees.sample.postRule.allRules;

public abstract class TestFunctionalInterface3Rule {

	static {
		staticGetRunnableHash();
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

	static {
		staticGetRunnable();
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
		Runnable r = this::hashCode;
		r.run();
	}

	{
		Runnable r = this::getClass;
		r.run();
	}

	@Override
	public int hashCode() {
		return 0;
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
		return this::hashCode;
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
		return this::getClass;
	}

}

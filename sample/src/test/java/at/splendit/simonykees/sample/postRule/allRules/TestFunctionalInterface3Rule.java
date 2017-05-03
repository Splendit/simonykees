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
		Runnable r = () -> {
			hashCode();
		};
		r.run();
	}

	{
		Runnable r = () -> {
			getClass();
		};
		r.run();
	}

	private static Runnable staticGetRunnableHash() {
		return new Runnable() {
			@Override
			public void run() {
				hashCode();
			}
		};
	}

	private static Runnable staticGetRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				getClass();
			}
		};
	}

	@Override
	public int hashCode() {
		return 0;
	}

	public Runnable getRunnableHash() {
		return () -> {
			hashCode();
		};
	}

	public Runnable getRunnable() {
		return () -> {
			getClass();
		};
	}

}

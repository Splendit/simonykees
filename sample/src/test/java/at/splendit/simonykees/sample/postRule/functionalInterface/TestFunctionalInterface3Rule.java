package at.splendit.simonykees.sample.postRule.functionalInterface;

public abstract class TestFunctionalInterface3Rule {

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

	static {
		staticGetRunnableHash();
	}

	public Runnable getRunnableHash() {
		return () -> {
			hashCode();
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
		Runnable r = () -> {
			hashCode();
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
		return () -> {
			getClass();
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
		Runnable r = () -> {
			getClass();
		};
		r.run();
	}

}

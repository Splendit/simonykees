package at.splendit.simonykees.sample.postRule.allRules;

public abstract class TestFunctionalInterface3Rule {
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

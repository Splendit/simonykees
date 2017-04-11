package at.splendit.simonykees.sample.postRule.allRules;

public abstract class TestFunctionalInterface3Rule {
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
			getClass();
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

	public Runnable getRunnable() {
		return () -> {
			getClass();
		};
	}

}

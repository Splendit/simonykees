package at.splendit.simonykees.sample.preRule;

public abstract class TestFunctionalInterface3Rule {
	private static Runnable staticGetRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				hashCode();
			}
		};
	}
	public int hashCode(){
		return 0;
	}
	
	static {
		staticGetRunnable();
		Runnable r = () -> {};
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

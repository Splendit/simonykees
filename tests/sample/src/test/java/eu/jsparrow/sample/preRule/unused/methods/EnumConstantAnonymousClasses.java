package eu.jsparrow.sample.preRule.unused.methods;

public enum EnumConstantAnonymousClasses {

	CODE {
		public void work() {
			System.out.println("Write new features");
		}
	},
	REFACTOR {
		public void work() {
			System.out.println("Improve existing code");
		}

		public void chill() {
			
		}
	};

	public abstract void work();

	private void unusedCode() {
		System.out.println("Do nothing");
	}

}

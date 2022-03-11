package eu.jsparrow.sample.postRule.unused.methods;

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

}

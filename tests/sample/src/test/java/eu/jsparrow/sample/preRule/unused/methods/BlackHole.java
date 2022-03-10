package eu.jsparrow.sample.preRule.unused.methods;

import java.util.function.Consumer;

public class BlackHole {

	public void use() {
		UnusedPublicMethods unusedPublic = new UnusedPublicMethods();
		unusedPublic.usedInMethodInvocationExternally();
		Consumer<UnusedPublicMethods> consumer = UnusedPublicMethods::usedInTypeMethodReference;
		consumer.accept(unusedPublic);
		
		UnusedPackagePrivateMethods unusedPackagePrivate = new UnusedPackagePrivateMethods();
		unusedPackagePrivate.usedExternally();
	}

	public static void main(String[]args) {
		BlackHole blackHole = new BlackHole();
		blackHole.use();
	}
}
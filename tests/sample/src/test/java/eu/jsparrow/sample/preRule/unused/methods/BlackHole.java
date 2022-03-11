package eu.jsparrow.sample.preRule.unused.methods;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BlackHole {

	public void use() {
		UnusedPublicMethods unusedPublic = new UnusedPublicMethods();
		unusedPublic.usedInMethodInvocationExternally();
		Consumer<UnusedPublicMethods> consumer = UnusedPublicMethods::usedInTypeMethodReference;
		consumer.accept(unusedPublic);
		
		UnusedPackagePrivateMethods unusedPackagePrivate = new UnusedPackagePrivateMethods();
		unusedPackagePrivate.usedExternally();
		
		BiConsumer<ParameterizedType<String>, String> c = ParameterizedType::add;
		c.accept(new ParameterizedType<String>(), "");
		List<ParameterizedType<String>> list = new ArrayList<>();
		list.stream().map(ParameterizedType<String>::foo);
		
		ColoredShape coloredShape = new ColoredCircle();
		coloredShape.implicitlyOverriden();
	}

	public static void main(String[]args) {
		BlackHole blackHole = new BlackHole();
		blackHole.use();
	}
}
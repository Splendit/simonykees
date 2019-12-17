package eu.jsparrow.sample.postRule.allRules;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings({ "nls", "unused" })
public abstract class OverrideAnnotationRule<T> {

	T val;

	protected OverrideAnnotationRule(T val) {
		this.val = val;
	}

	public void toBeOveridden(String input) {

	}

	public String dontOverride(Object object, String string) {
		final String s = "I am not expecting anybody to override me";
		return new StringBuilder().append(s)
			.append(string)
			.append(object)
			.toString();
	}

	protected T genericMethod(T value) {
		T myVal = null;

		if (value != null) {
			myVal = value;
		}

		return myVal;
	}

	protected <Type extends List<String>> Type methodUsingTypeVariablesInSignature(Type someCollection) {
		return null;
	}

	protected String myChildCanMakeMePublic() {
		return "Please make me public";
	}

	protected String alreadyAnnotated() {
		return "Already annotated";
	}

	protected String qualifiedNameAnnotation() {
		return "Already annotated";
	}

	private String iAmPrivate() {
		return "I am a very private method";
	}

	@Override
	public abstract int hashCode();

}

@SuppressWarnings({ "nls", "unused" })
class Foo extends OverrideAnnotationRule<String> implements IFoo {

	public IFoo iFoo = new IFoo() {

		@Override
		public void methodFromYouFoo(String foo) {

		}

		@Override
		public void methodFromIfoo() {

		}
	};

	protected Foo(String val) {
		super(val);
	}

	protected Foo(List<String> val) {
		super("");
	}

	@Override
	public void toBeOveridden(String input) {

	}

	public void toNotBeOverriden(String iput) {

	}

	public String dontOverride(String object, String string) {
		return string;
	}

	@Override
	protected String genericMethod(String value) {
		String myString = "";
		if (value != null && !StringUtils.isEmpty(value)) {
			myString = value;
		}
		return myString;
	}

	@Override
	protected <Type extends List<String>> Type methodUsingTypeVariablesInSignature(Type someCollection) {
		return null;
	}

	@Override
	public void methodFromYouFoo(String foo) {

	}

	@Override
	public void methodFromIfoo() {

	}

	@Override
	public String myChildCanMakeMePublic() {
		return "You are now public";
	}

	@Override
	protected String alreadyAnnotated() {
		return "Already annotated";
	}

	@java.lang.Override
	protected String qualifiedNameAnnotation() {
		return "Already annotated";
	}

	private String iAmPrivate() {
		return "I am a very private method";
	}

	@Override
	public int hashCode() {
		return 0;
	}

	enum FooEnum {
		use,
		override;

		@Override
		public String toString() {
			return "Don't forget to put the @Override annotation!";
		}
	};
}

interface IFoo extends YouFoo {
	void methodFromIfoo();
}

interface YouFoo {
	void methodFromYouFoo(String foo);
}

package at.splendit.simonykees.sample.preRule;

import java.util.List;

@SuppressWarnings({"nls", "unused"})  
public abstract class OverrideAnnotationRule<T> {
	
	T val;
	
	protected OverrideAnnotationRule(T val) {
		this.val = val;
	}
	
	public void toBeOveridden(String input) {
		
	}
	
	public String dontOverride(Object object, String string) {
		String s = "I am not expecting anybody to override me";
		return s + string + object;
	}
	
	protected T genericMethod(T value) {
		T myVal = null;
		
		if(value != null) {
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
	
	private String iAmPrivate() {
		return "I am a very private method";
	}
	
	public abstract int hashCode();

}

@SuppressWarnings({"nls", "unused"})  
class Foo extends OverrideAnnotationRule<String> implements IFoo {
	
	protected Foo(String val) {
		super(val);
	}
	
	public IFoo iFoo = new IFoo() {
		
		public void methodFromYouFoo(String foo) {
			
		}
		
		public void methodFromIfoo() {
			
		}
	};
	
	enum FooEnum {
		use,
		override;
		
		public String toString() {
			return "Don't forget to put the @Override annotation!";
		}
	}
	
	protected Foo(List<String> val) {
		super("");
	}

	public void toBeOveridden(String input) {
		
	}
	
	public void toNotBeOverriden(String iput) {
		
	}
	
	public String dontOverride(String object, String string) {
		return string;
	}
	
	protected String genericMethod(String value) {
		String myString = "";
		if(value != null && !value.isEmpty()) {
			myString = value;
		}
		return myString;
	}
	
	protected <Type extends List<String>> Type methodUsingTypeVariablesInSignature(Type someCollection) {
		return null;
	}

	public void methodFromYouFoo(String foo) {
		
		
	}

	public void methodFromIfoo() {
		
		
	}
	
	public String myChildCanMakeMePublic() {
		return "You are now public";
	}
	
	@Override
	protected String alreadyAnnotated() {
		return "Already annotated";
	}
	
	private String iAmPrivate() {
		return "I am a very private method";
	}
	
	public int hashCode() {
		return 0;
	};
}

interface IFoo extends YouFoo {
	void methodFromIfoo();
}

interface YouFoo {
	void methodFromYouFoo(String foo);
}

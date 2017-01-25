package at.splendit.simonykees.sample.postRule.inefficientConstructor;

public class TestInefficientConstructorCornerCase {
	
	public void test(){
		new Foo(1.0);
		new Foo(new Double(1.0));
	}
	
	private class Foo{
		public Foo(double value)
		{
		    this(Double.valueOf(value));
		}
		
		public Foo(Double value)
		{
		    
		}
	}
}

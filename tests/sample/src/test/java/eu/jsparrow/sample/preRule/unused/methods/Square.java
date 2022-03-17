package eu.jsparrow.sample.preRule.unused.methods;

public class Square extends AbstractShape {

	@Override
	public String getShape() {
		return "Square";
	}

	@Override
	public int findSurface() {
		return calcSurface(5, 6);
	}

	public void unusedMethod() {

	}

	public int calcSurface(int a, int b) {
		return a * b;
	}

}

package eu.jsparrow.sample.postRule.unused.methods;

public class Square extends AbstractShape {

	@Override
	public String getShape() {
		return "Square";
	}

	@Override
	public int findSurface() {
		return calcSurface(5, 6);
	}

	public int calcSurface(int a, int b) {
		return a * b;
	}

}

package eu.jsparrow.sample.preRule;

public class RemoveDuplicatedThrowsRule {

	public void throwingTheSameExceptionTwice_shouldTransform() throws ParentException, ParentException {
		/*
		 * Should keep: ParentException
		 */
	}

	public void throwingChildAndParentException_shouldTransform() throws ChildException, ParentException {
		/*
		 * Should keep: ParentException
		 */
	}

	public void throwingParentAndChildException_shouldTransform() throws ParentException, ChildException {
		/*
		 * Should keep: ParentException
		 */
	}

	public void throwingExceptionAndGrandChildException_shouldTransform() throws ParentException, GrandChildException {
		/*
		 * Should keep: ParentException
		 */
	}
	
	public void throwingRuntimeException_shouldTransform() throws RuntimeException {
		/*
		 * Should keep: ParentException
		 */
	}

	public void throwingExceptionSiblingAndChild_shouldTransform()
			throws ChildException, SiblingException, GrandChildException {
		/*
		 * Should keep: ChildException, SiblingException
		 */
	}

	public void throwingOneExceptionMultipleTimes_shouldTransform() 
			throws ChildException, ChildException,
			SiblingException, SiblingException, 
			ChildException, SiblingException {
		/*
		 * Should keep: ChildException, SiblingException
		 */
	}

	public void throwingSiblingException_shouldNotTransform() throws ChildException, SiblingException {

	}

	public void throwingOneSingleException_shouldNotTransform() throws ParentException {

	}

	public void throwingNoException_shouldNotTransform() {

	}

	class ParentException extends Exception {
		private static final long serialVersionUID = 6023806331018889020L;
	}

	class ChildException extends ParentException {
		private static final long serialVersionUID = 1335640758693727349L;
	}

	class SiblingException extends ParentException {
		private static final long serialVersionUID = 7793747637871746398L;
	}

	class GrandChildException extends ChildException {
		private static final long serialVersionUID = 1491031502839338255L;
	}

}

package eu.jsparrow.sample.postRule.duplicatedThrows;

public class RemoveUnnecessaryThrownExceptionsRule {

	public void throwingTheSameExceptionTwice_shouldTransform() throws ParentException {
		/*
		 * Should keep: ParentException
		 */
	}

	public void throwingChildAndParentException_shouldTransform() throws ParentException {
		/*
		 * Should keep: ParentException
		 */
	}

	public void throwingParentAndChildException_shouldTransform() throws ParentException {
		/*
		 * Should keep: ParentException
		 */
	}

	public void throwingExceptionAndGrandChildException_shouldTransform() throws ParentException {
		/*
		 * Should keep: ParentException
		 */
	}

	public void throwingExceptionSiblingAndChild_shouldTransform()
			throws ChildException, SiblingException {
		/*
		 * Should keep: ChildException, SiblingException
		 */
	}

	public void throwingOneExceptionMultipleTimes_shouldTransform() 
			throws ChildException, SiblingException {
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

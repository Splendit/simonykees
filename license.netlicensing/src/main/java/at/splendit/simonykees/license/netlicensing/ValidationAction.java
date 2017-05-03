package at.splendit.simonykees.license.netlicensing;

/**
 * Enumeration of the validation actions.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public enum ValidationAction {
	CHECK_IN, CHECK_OUT, NONE;
	
	@SuppressWarnings("nls")
	public static ValidationAction fromString(String action) {
		ValidationAction validationAction = NONE;
		
		if(action != null) {
			switch (action) {
			case "check-in":
				validationAction = CHECK_IN;
				break;
			case "check-out":
				validationAction = CHECK_OUT;
				break;
			default:
				validationAction = NONE;
				break;
			}
		}
		
		return validationAction;
	}
	
	@SuppressWarnings("nls")
	public String toString() {
		String action = "none";
		switch (this) {
		case CHECK_IN:
			action = "check-in";
			break;
		case CHECK_OUT:
			action = "check-out";
			break;
		case NONE:
			action = "none";
		default:
			action = "none";
			break;
		}
		
		return action;
	}

}

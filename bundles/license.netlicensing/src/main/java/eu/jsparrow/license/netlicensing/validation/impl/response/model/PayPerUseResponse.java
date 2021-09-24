package eu.jsparrow.license.netlicensing.validation.impl.response.model;

public class PayPerUseResponse extends NetlicensingResponse {

	public static final String LICENSING_MODEL = "PayPerUse"; //$NON-NLS-1$
	public static final String REMAINING_QUANTITY_KEY = "remainingQuantity"; //$NON-NLS-1$

	private Integer remainingQuantity;

	public PayPerUseResponse(boolean valid) {
		super(valid);
	}

	public PayPerUseResponse(Integer remainingQuantity, boolean valid) {
		super(valid);
		this.remainingQuantity = remainingQuantity;
	}

	public Integer getRemainingQuantity() {
		return remainingQuantity;
	}

}

package at.splendit.simonykees.core.License;



import com.labs64.netlicensing.domain.vo.Composition;
import com.labs64.netlicensing.domain.vo.Context;

import com.labs64.netlicensing.domain.vo.SecurityMode;
import com.labs64.netlicensing.domain.vo.ValidationParameters;
import com.labs64.netlicensing.domain.vo.ValidationResult;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.service.LicenseeService;

public class LicenseServerManagment {

	static final String REST_API_PATH = "/core/v2/rest";
	static final String BASE_URL_PROD = "https://go.netlicensing.io";
	static final String APIKEY = "1594d25c-9c9e-404b-a3ae-716177fc22d4";
	static final String USER_APIKEY = "apikey";
	static final String PASS_APIKEY = "5e6b99b7-4a6f-4a33-b94e-67f3552e0925";
	static final String BASE_URL = BASE_URL_PROD + REST_API_PATH;
	static final String PRODUCT_NUMBER="test-01";

	public static void main(final String[] args) {
		
		

		final Context context = new Context();
		context.setBaseUrl(BASE_URL).setSecurityMode(SecurityMode.APIKEY_IDENTIFICATION).setApiKey(PASS_APIKEY);
       
		doValidate(context);
		
	}

	public static void doValidate(Context context) {
		
		try {
			
			ValidationResult validationResult = LicenseeService.validate(context, "28.NovSubs",PRODUCT_NUMBER,"LicenseeNameSubs", createValidationParamter());
			
			
			System.out.println(validationResult.getValidations().size());
			
						
			for (Composition value : validationResult.getValidations().values()) {
			    System.out.println("model = " + value);
			    
			    for(String key: value.getProperties().keySet()){
			    	 System.out.print("Key = " + key);
			    	 System.out.println("  value:  "+ value.getProperties().get(key).getValue());
			    }
			
			}
			
						
		} catch (final NetLicensingException e) {
			System.out.println("Got NetLicensing exception:" + e);
		} catch (final Exception e) {
			System.out.println("Got  exception:" + e);
		}
	}
	
	public static ValidationParameters createValidationParamter(){
		
		ValidationParameters validationParameter=new ValidationParameters();
		String productModuleNumber= "testModule-02";
		validationParameter.put(productModuleNumber, "sessionId", "uniques01");
		validationParameter.put(productModuleNumber, "action", "checkOut");
		
		return validationParameter;
		
	}
	
}
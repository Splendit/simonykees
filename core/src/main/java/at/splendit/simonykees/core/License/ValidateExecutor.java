package at.splendit.simonykees.core.License;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

public class ValidateExecutor {

	public static void main(String[] arg0){
		SchedulerEntity se=new SchedulerEntity();
		LicenseeEntity le=new LicenseeEntity("will be Implemented", "will be Implemented", new ValidationParameters());
		se.setDoValidate(true);
		se.setValidateInterval(5);
		 startSchedule(se,le);
		
		
	}
	
	
	protected synchronized static void startSchedule(SchedulerEntity se,LicenseeEntity le ) {
		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

		service.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				if (se.getDoValidate()) {
					CallLicenseValidity.doValidate(le);
					 System.out.println("start Validate");
				} else {
					 System.out.println("shutDownValidate");
					service.shutdown();

				}

			}
		}, 0, se.getValidateInterval(), TimeUnit.SECONDS);

	}
}

package eu.jsparrow.logging.service.impl;

import eu.jsparrow.logging.service.api.IJSparrowLoggerFactory;
import eu.jsparrow.logging.service.api.IJSparrowLoggerFactoryService;

/**
 * 
 */
public class JSparrowLoggerFactoryServiceImpl implements IJSparrowLoggerFactoryService {

	@Override
	public IJSparrowLoggerFactory createJSparrowFactory() {

		return new JSparrowLoggerFactoryImpl();
	}

}

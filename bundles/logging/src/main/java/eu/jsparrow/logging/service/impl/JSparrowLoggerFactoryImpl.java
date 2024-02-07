package eu.jsparrow.logging.service.impl;

import eu.jsparrow.logging.service.api.IJSparrowLogger;
import eu.jsparrow.logging.service.api.IJSparrowLoggerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSparrowLoggerFactoryImpl implements IJSparrowLoggerFactory {

	@Override
	public IJSparrowLogger createLogger(Class<?> clazz) {
		Logger logger = LoggerFactory.getLogger(clazz);
		return new JSparrowLoggerImpl(logger);
	}

}

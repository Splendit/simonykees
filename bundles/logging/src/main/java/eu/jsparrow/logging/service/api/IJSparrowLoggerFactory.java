package eu.jsparrow.logging.service.api;

public interface IJSparrowLoggerFactory {
	IJSparrowLogger createLogger(Class<?> clazz);
}

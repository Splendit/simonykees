package eu.jsparrow.core.rule.impl.logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.runtime.ITypeNotFoundRuntimeException;
import eu.jsparrow.core.rule.SemiAutomaticRefactoringRule;
import eu.jsparrow.core.visitor.semiautomatic.StandardLoggerASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * This rule replaces the System.out/err.print calls and the
 * Throwable.printStacktrace with logger methods, if any of the supported
 * loggers is in the classpath of the project. The supported loggers are:
 * 
 * <ul>
 * <li>{@value StandardLoggerConstants#SLF4J_LOGGER}</li>
 * <li>{@value StandardLoggerConstants#LOG4J_LOGGER}</li>
 * </ul>
 * 
 * If none of the supported loggers is in the classpath, then the rule cannot be
 * applied.
 * 
 * @see StandardLoggerASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class StandardLoggerRule extends SemiAutomaticRefactoringRule<StandardLoggerASTVisitor>
		implements StandardLoggerOptions {

	private static final Logger logger = LoggerFactory.getLogger(StandardLoggerRule.class);

	private static final String TRACE = "trace"; //$NON-NLS-1$
	private static final String DEBUG = "debug"; //$NON-NLS-1$
	private static final String INFO = "info"; //$NON-NLS-1$
	private static final String WARN = "warn"; //$NON-NLS-1$
	private static final String ERROR = "error"; //$NON-NLS-1$

	private Map<String, Integer> systemOutReplaceOptions = new LinkedHashMap<>();
	private Map<String, Integer> systemErrReplaceOptions = new LinkedHashMap<>();
	private Map<String, Integer> pritntStacktraceReplaceOptions = new LinkedHashMap<>();
	private Map<String, String> selectedOptions = new HashMap<>();

	private SupportedLogger supportedLoger = null;
	private String loggerQualifiedName = null;

	public StandardLoggerRule() {
		super();
		this.visitor = StandardLoggerASTVisitor.class;
		this.name = Messages.StandardLoggerRule_name;
		this.description = Messages.StandardLoggerRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		try {
			if (project.findType(StandardLoggerConstants.SLF4J_LOGGER) != null) {
				supportedLoger = SupportedLogger.SLF4J;
				loggerQualifiedName = StandardLoggerConstants.SLF4J_LOGGER;
			} else if (project.findType(StandardLoggerConstants.LOG4J_LOGGER) != null) {
				supportedLoger = SupportedLogger.LOG4J;
				loggerQualifiedName = StandardLoggerConstants.LOG4J_LOGGER;
			}

			if (supportedLoger != null) {
				initLogLevelOptions();
				return true;
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), new ITypeNotFoundRuntimeException());
		}

		return false;
	}

	public void setSelectedOptions(Map<String, String> selectedOptions) {
		this.selectedOptions.putAll(selectedOptions);
	}

	public Map<String, String> getSelectedOptions() {
		return Collections.unmodifiableMap(selectedOptions);
	}

	private void initLogLevelOptions() {
		systemOutReplaceOptions.put(TRACE, 1);
		systemOutReplaceOptions.put(DEBUG, 2);
		systemOutReplaceOptions.put(INFO, 3);
		systemOutReplaceOptions.put(WARN, 4);
		systemOutReplaceOptions.put(ERROR, 5);

		systemErrReplaceOptions.put(TRACE, 1);
		systemErrReplaceOptions.put(DEBUG, 2);
		systemErrReplaceOptions.put(INFO, 3);
		systemErrReplaceOptions.put(WARN, 4);
		systemErrReplaceOptions.put(ERROR, 5);

		pritntStacktraceReplaceOptions.put(TRACE, 1);
		pritntStacktraceReplaceOptions.put(DEBUG, 2);
		pritntStacktraceReplaceOptions.put(INFO, 3);
		pritntStacktraceReplaceOptions.put(WARN, 4);
		pritntStacktraceReplaceOptions.put(ERROR, 5);
	}

	@Override
	public Map<String, Integer> getSystemOutReplaceOptions() {
		return systemOutReplaceOptions;
	}

	@Override
	public Map<String, Integer> getSystemErrReplaceOptions() {
		return systemErrReplaceOptions;
	}

	@Override
	public Map<String, Integer> getPrintStackTraceReplaceOptions() {
		return pritntStacktraceReplaceOptions;
	}

	@Override
	public Map<String, String> getDefaultOptions() {
		Map<String, String> defaultOptions = new HashMap<>();
		defaultOptions.put(StandardLoggerConstants.SYSTEM_OUT_PRINT, INFO);
		defaultOptions.put(StandardLoggerConstants.SYSTEM_ERR_PRINT, ERROR);
		defaultOptions.put(StandardLoggerConstants.PRINT_STACKTRACE, ERROR);

		return defaultOptions;
	}

	@Override
	public SupportedLogger getAvailableLoggerType() {
		return this.supportedLoger;
	}

	public String getAvailableQualifiedLoggerName() {
		return loggerQualifiedName;
	}

	@Override
	public void activateDefaultOptions() {
		// default options should be activated only for test purposes
		setSelectedOptions(getDefaultOptions());
		this.loggerQualifiedName = StandardLoggerConstants.SLF4J_LOGGER;
	}

	public void activateOptions(Map<String, String> options) {
		// default options should be activated only for test purposes
		Map<String, String> defaultOptions = getDefaultOptions();
		options.forEach((key, value) -> {
			if (defaultOptions.containsKey(key))
				defaultOptions.put(key, value);
		});
		setSelectedOptions(defaultOptions);
		this.loggerQualifiedName = options.get(StandardLoggerConstants.LOGGER_QUALIFIED_NAME);
	}

	@Override
	protected StandardLoggerASTVisitor visitorFactory() {
		Map<String, String> replacingOptions = getSelectedOptions();
		String availableLogger = getAvailableQualifiedLoggerName();
		return new StandardLoggerASTVisitor(availableLogger, replacingOptions);
	}
}

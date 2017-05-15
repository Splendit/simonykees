package at.splendit.simonykees.core.rule.impl.standardLogger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.exception.runtime.ITypeNotFoundRuntimeException;
import at.splendit.simonykees.core.rule.SemiAutomaticRefactoringRule;
import at.splendit.simonykees.core.visitor.semiAutomatic.StandardLoggerASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * This rule replaces the System.out/err.print calls and the Throwable.printStacktrace with
 * logger methods, if any of the supported loggers is in the classpath of the project. 
 * The supported loggers are:
 * 
 * <ul>
 * 	<li> {@value #SLF4J_LOGGER} </li>
 * 	<li> {@value #LOGBACK_LOGGER} </li>
 * 	<li> {@value #LOG4J_LOGGER} </li>
 * </ul>
 * 
 * If none of the supported loggers is in the classpath, then the rule cannot be applied. 
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

	private static final String SLF4J_LOGGER = "org.slf4j.Logger"; //$NON-NLS-1$
	private static final String LOGBACK_LOGGER = "ch.qos.logback.classic.Logger"; //$NON-NLS-1$
	private static final String LOG4J_LOGGER = "org.apache.logging.log4j.Logger"; //$NON-NLS-1$

	private static final String TRACE = "trace"; //$NON-NLS-1$
	private static final String DEBUG = "debug"; //$NON-NLS-1$
	private static final String INFO = "info"; //$NON-NLS-1$
	private static final String WARN = "warn"; //$NON-NLS-1$
	private static final String ERROR = "error"; //$NON-NLS-1$

	private Map<String, Integer> systemOutReplaceOptions = new LinkedHashMap<>();
	private Map<String, Integer> systemErrReplaceOptions = new LinkedHashMap<>();
	private Map<String, Integer> pritntStacktraceReplaceOptions = new LinkedHashMap<>();

	private SupportedLogger supportedLoger;
	private IType loggerType;

	public StandardLoggerRule(Class<StandardLoggerASTVisitor> visitor) {
		super(visitor);
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
			if ((loggerType = project.findType(SLF4J_LOGGER)) != null) {
				supportedLoger = SupportedLogger.SLF4J;
			} else if ((loggerType = project.findType(LOGBACK_LOGGER)) != null) {
				supportedLoger = SupportedLogger.LOGBACK;
			} else if ((loggerType = project.findType(LOG4J_LOGGER)) != null) {
				supportedLoger = SupportedLogger.LOG4J;
			}

			if (loggerType != null) {
				initLogLevelOptions();
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), new ITypeNotFoundRuntimeException());
		}

		return loggerType != null && super.ruleSpecificImplementation(project);
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
		defaultOptions.put(SYSTEM_OUT_PRINT, INFO);
		defaultOptions.put(SYSTEM_ERR_PRINT, ERROR);
		defaultOptions.put(PRINT_STACKTRACE, ERROR);

		return defaultOptions;
	}

	@Override
	public SupportedLogger getAvailableLoggerType() {
		return this.supportedLoger;
	}

	@Override
	public IType getLoggerType() {
		return this.loggerType;
	}
}

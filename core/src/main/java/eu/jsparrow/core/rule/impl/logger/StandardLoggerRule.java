package eu.jsparrow.core.rule.impl.logger;

import java.time.Duration;
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
import eu.jsparrow.core.rule.RuleApplicationCount;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.rule.SemiAutomaticRefactoringRule;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.logger.StandardLoggerASTVisitor;
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
	private Map<String, Integer> systemOutPrintExceptionReplaceOptions = new LinkedHashMap<>();
	private Map<String, Integer> systemErrPrintExceptionReplaceOptions = new LinkedHashMap<>();
	private Map<String, Integer> printStacktraceReplaceOptions = new LinkedHashMap<>();
	private Map<String, Integer> newLoggingStatementOptions = new LinkedHashMap<>();
	private Map<String, String> selectedOptions = new HashMap<>();

	private static final Map<String, Integer> replaceOptions;

	private SupportedLogger supportedLoger = null;
	private String loggerQualifiedName = null;

	static {
		Map<String, Integer> options = new LinkedHashMap<>();
		options.put(TRACE, 1);
		options.put(DEBUG, 2);
		options.put(INFO, 3);
		options.put(WARN, 4);
		options.put(ERROR, 5);
		replaceOptions = Collections.unmodifiableMap(options);
	}

	public StandardLoggerRule() {
		this.visitorClass = StandardLoggerASTVisitor.class;
		this.id = "StandardLogger"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.StandardLoggerRule_name,
				Messages.StandardLoggerRule_description, Duration.ofMinutes(10),
				TagUtil.getTagsForRule(this.getClass()));
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

	private void setSelectedOptions(Map<String, String> selectedOptions) {
		this.selectedOptions.putAll(selectedOptions);
	}

	public Map<String, String> getSelectedOptions() {
		return Collections.unmodifiableMap(selectedOptions);
	}

	private void initLogLevelOptions() {
		systemOutReplaceOptions.putAll(replaceOptions);
		systemErrReplaceOptions.putAll(replaceOptions);
		systemErrPrintExceptionReplaceOptions.putAll(replaceOptions);
		systemOutPrintExceptionReplaceOptions.putAll(replaceOptions);
		newLoggingStatementOptions.putAll(replaceOptions);
		printStacktraceReplaceOptions.putAll(replaceOptions);
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
		return printStacktraceReplaceOptions;
	}

	@Override
	public Map<String, Integer> getSystemOutPrintExceptionReplaceOptions() {
		return systemOutPrintExceptionReplaceOptions;
	}

	@Override
	public Map<String, Integer> getSystemErrPrintExceptionReplaceOptions() {
		return systemErrPrintExceptionReplaceOptions;
	}

	@Override
	public Map<String, Integer> getMissingLogInsertOptions() {
		return newLoggingStatementOptions;
	}

	@Override
	public Map<String, String> getDefaultOptions() {
		Map<String, String> defaultOptions = new HashMap<>();
		defaultOptions.put(StandardLoggerConstants.SYSTEM_OUT_PRINT_KEY, INFO);
		defaultOptions.put(StandardLoggerConstants.SYSTEM_ERR_PRINT_KEY, ERROR);
		defaultOptions.put(StandardLoggerConstants.PRINT_STACKTRACE_KEY, ERROR);
		defaultOptions.put(StandardLoggerConstants.SYSTEM_OUT_PRINT_EXCEPTION_KEY, INFO);
		defaultOptions.put(StandardLoggerConstants.SYSTEM_ERR_PRINT_EXCEPTION_KEY, ERROR);
		defaultOptions.put(StandardLoggerConstants.MISSING_LOG_KEY, ERROR);

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
	}

	@Override
	public void activateOptions(Map<String, String> options) {
		// default options should be activated only for test purposes
		Map<String, String> defaultOptions = getDefaultOptions();
		options.forEach((key, value) -> {
			if (defaultOptions.containsKey(key)) {
				defaultOptions.put(key, value);
			}
		});
		setSelectedOptions(defaultOptions);
	}

	@Override
	protected StandardLoggerASTVisitor visitorFactory() {
		Map<String, String> replacingOptions = getSelectedOptions();
		String availableLogger = getAvailableQualifiedLoggerName();
		StandardLoggerASTVisitor visitor = new StandardLoggerASTVisitor(availableLogger, replacingOptions);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}
}

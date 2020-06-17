package eu.jsparrow.core.rule.impl.logger;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.runtime.ITypeNotFoundRuntimeException;
import eu.jsparrow.core.visitor.logger.StandardLoggerASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.SemiAutomaticRefactoringRule;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;

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

	private static final String TRUE = Boolean.TRUE.toString();

	public static final String STANDARD_LOGGER_RULE_ID = "StandardLogger"; //$NON-NLS-1$

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
		options.put(LogLevelEnum.TRACE.getLogLevel(), 1);
		options.put(LogLevelEnum.DEBUG.getLogLevel(), 2);
		options.put(LogLevelEnum.INFO.getLogLevel(), 3);
		options.put(LogLevelEnum.WARN.getLogLevel(), 4);
		options.put(LogLevelEnum.ERROR.getLogLevel(), 5);
		replaceOptions = Collections.unmodifiableMap(options);
	}

	public StandardLoggerRule() {
		this.visitorClass = StandardLoggerASTVisitor.class;
		this.id = STANDARD_LOGGER_RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.StandardLoggerRule_name,
				Messages.StandardLoggerRule_description, Duration.ofMinutes(10),
				Arrays.asList(Tag.JAVA_1_1, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.CODING_CONVENTIONS, Tag.LOGGING));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
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
		defaultOptions.put(StandardLoggerConstants.SYSTEM_OUT_PRINT_KEY, LogLevelEnum.INFO.getLogLevel());
		defaultOptions.put(StandardLoggerConstants.SYSTEM_ERR_PRINT_KEY, LogLevelEnum.ERROR.getLogLevel());
		defaultOptions.put(StandardLoggerConstants.PRINT_STACKTRACE_KEY, LogLevelEnum.ERROR.getLogLevel());
		defaultOptions.put(StandardLoggerConstants.SYSTEM_OUT_PRINT_EXCEPTION_KEY, LogLevelEnum.INFO.getLogLevel());
		defaultOptions.put(StandardLoggerConstants.SYSTEM_ERR_PRINT_EXCEPTION_KEY, LogLevelEnum.ERROR.getLogLevel());
		defaultOptions.put(StandardLoggerConstants.MISSING_LOG_KEY, LogLevelEnum.ERROR.getLogLevel());
		defaultOptions.put(StandardLoggerConstants.ATTACH_EXCEPTION_OBJECT, TRUE);

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

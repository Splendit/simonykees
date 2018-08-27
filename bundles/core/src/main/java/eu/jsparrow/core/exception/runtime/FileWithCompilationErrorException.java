package eu.jsparrow.core.exception.runtime;

import eu.jsparrow.core.rule.impl.FieldsRenamingRule;

/**
 * An exception for indicating that a file containing compilation
 * errors will be affected by the modifications of a rule. For example, 
 * a reference of a field which is being renamed by {@link FieldsRenamingRule}
 * is occurring in a file with compilation errors. 
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class FileWithCompilationErrorException extends RuntimeException {
	private static final long serialVersionUID = -8999931560571728411L;

	public FileWithCompilationErrorException(String message) {
		super(message);
	}
}

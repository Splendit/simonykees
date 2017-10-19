package eu.jsparrow.core.exception.model;

import java.util.List;
import java.util.stream.Collectors;

import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.rule.AbstractRefactoringRule;

/**
 * Information we need when reporting that a {@link AbstractRefactoringRule} cannot be
 * applied to a {@link RefactoringState}.
 * 
 * @author Ludwig Werzowa
 * @since 1.2
 */
public class NotWorkingRuleModel {

	private String ruleName;
	private String javaClassName;

	public NotWorkingRuleModel(String ruleName, String javaClassName) {
		this.ruleName = ruleName;
		this.javaClassName = javaClassName;
	}

	@Override
	public String toString() {
		return String.format("[%s on %s]", this.ruleName, this.javaClassName); //$NON-NLS-1$
	}

	/**
	 * Returns a list of {@link NotWorkingRuleModel}s as a flat String.
	 * 
	 * @param notWorkingRulesList
	 * @return
	 */
	public static String asString(List<NotWorkingRuleModel> notWorkingRulesList) {
		return notWorkingRulesList.stream().map(NotWorkingRuleModel::toString)
				.collect(Collectors.joining(System.lineSeparator()));
	}
}

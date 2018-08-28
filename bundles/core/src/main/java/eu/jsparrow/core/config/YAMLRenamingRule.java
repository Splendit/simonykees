package eu.jsparrow.core.config;

import java.util.Arrays;
import java.util.List;

import eu.jsparrow.core.rule.impl.FieldsRenamingRule;

/**
 * Model class for {@link FieldsRenamingRule} YAML data
 * 
 * @since 2.6.0
 *
 */
public class YAMLRenamingRule {

	private List<String> fieldTypes;
	private String underscoreReplacementOption;
	private String dollarReplacementOption;
	private boolean addTodoComments;

	public YAMLRenamingRule() {
		super();
		this.fieldTypes = Arrays.asList("private", "protected", "package-protected", "public"); //$NON-NLS-1$, //$NON-NLS-2$, //$NON-NLS-3$, //$NON-NLS-4$
		this.underscoreReplacementOption = "Upper"; //$NON-NLS-1$
		this.dollarReplacementOption = "Leave"; //$NON-NLS-1$
		this.addTodoComments = false;
	}

	public YAMLRenamingRule(List<String> fieldTypes, String underscoreReplacementOption, String dollarReplacementOption,
			boolean addTodoComments) {
		super();
		this.fieldTypes = fieldTypes;
		this.underscoreReplacementOption = underscoreReplacementOption;
		this.dollarReplacementOption = dollarReplacementOption;
		this.addTodoComments = addTodoComments;
	}

	public List<String> getFieldTypes() {
		return fieldTypes;
	}

	public void setFieldTypes(List<String> fieldTypes) {
		this.fieldTypes = fieldTypes;
	}

	public String getUnderscoreReplacementOption() {
		return underscoreReplacementOption;
	}

	public void setUnderscoreReplacementOption(String underscoreReplacementOption) {
		this.underscoreReplacementOption = underscoreReplacementOption;
	}

	public String getDollarReplacementOption() {
		return dollarReplacementOption;
	}

	public void setDollarReplacementOption(String dollarReplacementOption) {
		this.dollarReplacementOption = dollarReplacementOption;
	}

	public boolean isAddTodoComments() {
		return addTodoComments;
	}

	public void setAddTodoComments(boolean addTodoComments) {
		this.addTodoComments = addTodoComments;
	}

	@Override
	@SuppressWarnings("nls")
	public String toString() {
		return "YAMLRenamingRule [fieldTypes=" + fieldTypes + ", underscoreReplacementOption="
				+ underscoreReplacementOption + ", dollarReplacementOption=" + dollarReplacementOption
				+ ", addTodoComments=" + addTodoComments + "]";
	}
}

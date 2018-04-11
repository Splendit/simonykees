package eu.jsparrow.sample.preRule.renaming;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings({"nls", "unused"})
public class PublicFieldsRenamingRule {
	
	public String a_public_field_sample = "bad name";
	public String referenced_on_other_classes = "not referenced in this class";
	public String to_not_be_shadowed = "";
	public String avoid_clashes;
	private String avoidClashes;
	public String remove$dollar$sign = "expected new name: removeDollarSign";
	public String avoid_implicit_clashes = "shall be renamed";
	public String avoid_implicitClashes = "shall not be renamed";
	public int _int, Int, i_nt, $int, int_;
	
	public String usePublicFieldSomewhere(String input) {
		a_public_field_sample = "a second reference";
		return a_public_field_sample + input;
	}
	
	public void dontShadowVariables() {
		String toNotBeShadowed = "";
	}
	
	public String referenceFieldWithDollarSign() {
		return this.remove$dollar$sign;
	}
	
	public void referenceAsObjectProperty() {
		PublicFieldsRenamingRule rule = new PublicFieldsRenamingRule();
		rule.a_public_field_sample = "";
	}
	
	private void referenceImplicitClashes() {
		this.avoid_implicit_clashes = "should be changed";
		this.avoid_implicitClashes = "shall not be renamed";
	}
	
	public void referencingInAnonymousClass() {
		ActionListener listener = new ActionListener() {
			public String skip_fields_in_anonymous_class = "because search engine cannot find the exact references ";
			
			@Override
			public void actionPerformed(ActionEvent e) {
				skip_fields_in_anonymous_class = "just for reference";
				a_public_field_sample = "reference in anonymous class";
			}
		};
	}
	
	class InnerClass {
		public String avoidImplicit_clashes;
		public String clash_free_field_in_inner_type;
		
		public String useFieldHavingImplicitClash() {
			this.avoidImplicit_clashes = "";
			return this.avoidImplicit_clashes;
		}
		
		public String ussageOfClasshFreeField() {
			this.clash_free_field_in_inner_type = "shall be renamed";
			return this.clash_free_field_in_inner_type;
		}
	}
}

class AnotherNonMemberClass {
	public String avoid_implicit$clashes;
}

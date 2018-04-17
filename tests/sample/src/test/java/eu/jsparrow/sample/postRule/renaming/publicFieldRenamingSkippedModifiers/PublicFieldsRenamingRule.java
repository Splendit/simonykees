package eu.jsparrow.sample.postRule.renaming.publicFieldRenamingSkippedModifiers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings({"nls", "unused"})
public class PublicFieldsRenamingRule {
	
	public String aPublicFieldSample = "bad name";
	public String referencedOnOtherClasses = "not referenced in this class";
	public String to_not_be_shadowed = "";
	public String avoid_clashes;
	private String avoidClashes;
	public String removeDollarSign = "expected new name: removeDollarSign";
	public String avoidImplicitClashes = "shall be renamed";
	public String avoid_implicitClashes = "shall not be renamed";
	public int _int, Int, iNt, $int, int_;
	
	public String usePublicFieldSomewhere(String input) {
		aPublicFieldSample = "a second reference";
		return aPublicFieldSample + input;
	}
	
	public void dontShadowVariables() {
		String toNotBeShadowed = "";
	}
	
	public String referenceFieldWithDollarSign() {
		return this.removeDollarSign;
	}
	
	public void referenceAsObjectProperty() {
		PublicFieldsRenamingRule rule = new PublicFieldsRenamingRule();
		rule.aPublicFieldSample = "";
	}
	
	private void referenceImplicitClashes() {
		this.avoidImplicitClashes = "should be changed";
		this.avoid_implicitClashes = "shall not be renamed";
	}
	
	public void referencingInAnonymousClass() {
		ActionListener listener = new ActionListener() {
			public String skip_fields_in_anonymous_class = "because search engine cannot find the exact references ";
			
			@Override
			public void actionPerformed(ActionEvent e) {
				skip_fields_in_anonymous_class = "just for reference";
				aPublicFieldSample = "reference in anonymous class";
			}
		};
	}
	
	class InnerClass {
		public String avoidImplicit_clashes;
		public String clashFreeFieldInInnerType;
		
		public String useFieldHavingImplicitClash() {
			this.avoidImplicit_clashes = "";
			return this.avoidImplicit_clashes;
		}
		
		public String ussageOfClasshFreeField() {
			this.clashFreeFieldInInnerType = "shall be renamed";
			return this.clashFreeFieldInInnerType;
		}
	}
}

class AnotherNonMemberClass {
	public String avoidImplicitClashes;
}

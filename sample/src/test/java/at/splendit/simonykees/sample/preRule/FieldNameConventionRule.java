package at.splendit.simonykees.sample.preRule;

import java.util.Optional;

@SuppressWarnings({"nls", "unused"})
public class FieldNameConventionRule {
	private String String;
	private Foo Foo;
	private FieldNameConventionRule FieldName_CONVENTIONRule_cammel$case;
	
	private String fieldName01W1thNumbers;
	
	int $0;
	int $_;
	private String $$$;
	private int Int;

	private final String DONT_RENAME_ME = "";
	
	private static String i_nt, i$nt, s$Witch, $int, _int;
	private String _myval;
	private String $myval;
	private String myval_;
	private String myval$ = _myval;
	private String my$val = new String(_myval);
	private String Mon = myval$;
	private static String CanBeRenamed = "expecting renaming";
	private static String CAMMEL_CASE_ME = "this can be converted to cammel case";
	
	private String shadowed_var;
	private String österreich, Österreich;
	
	public Integer MyInt;
	
	protected int getInt() {
		return Int;
	}
	
	public String someFieldsAreShadowed() {
		String shadowed_var = "myLocalVal";
		String Österreich = "Österreich";
		return shadowed_var + Österreich + Optional.ofNullable(this.shadowed_var).orElse("");
	}
	
	public String referenceWithQualifiedName() {
		FieldNameConventionRule conentionRule = new FieldNameConventionRule();
		return conentionRule.my$val;
	}
	
	public String methodImplementigAnAnonymousClass() {
		String _myval = Optional.ofNullable(this._myval).orElse("");
		FooInterface fooInterface = new FooInterface() {
			int _myval = 0;
			
			@Override
			public WeekDays getTomorrow() {
				return WeekDays.SATURDAAAY;
			}
			
			@Override
			public int getMyVal() {
				return _myval;				
			}
		};
		
		_myval += Integer.toString(fooInterface.getMyVal());
		
		return _myval; 
	}
	
	class Foo {
		
//		private Integer Int;
		private String i_nt;
		private String _myval;
		
		public Foo () {
			FieldNameConventionRule conventionRule = new FieldNameConventionRule();
			conventionRule._myval = "";
			if(my$val != null) {
				Int = 0;
			}
		}
		
		public String getMyVal() {
			
			return _myval;
		}
		
		public Integer getIntValue() {
			return Int;
		}
	}
	
	enum WeekDays {
		Mon, Tue, Wed, Thu, Fri, SATURDAAAY, SUNDAAAYY;
		
		private Integer _myval;
		
		public Integer getMyVal() {
			FieldNameConventionRule conventionRule = new FieldNameConventionRule();
			conventionRule.my$val = null;
			return this._myval;
		}
	}
	
	interface FooInterface {
		int getMyVal();
		WeekDays getTomorrow();
	}
	
	@interface FooAnnotation {
		String myvalue = _int;
		String Mon = CanBeRenamed;
		String cammelCaseMe = CAMMEL_CASE_ME;
		
	}
}

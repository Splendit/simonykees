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

	private static final String DONT_RENAME_ME = "";
	private final String FINAL_INSTANCE_VARIABLES_CAN_BE_RENAMED = "toBeRenamed";
	
	private static String i_nt, i$nt, s$Witch, $int, _int;
	private String _myval;
	private String $myval;
	private String myval_;
	private String myval$ = _myval;
	private String my$val = new String(_myval);
	private String Mon = myval$;
	private static String CanBeRenamed = "expecting renaming";
	private static String CAMEL_CASE_ME = "this can be converted to camel case";
	
	private String shadowed_var;
	private String österreich, Österreich;
	
	private String[]MyArray = {"my", "array"};
	
	public Integer MyInt;
	
	private int True;
	private double False;
	private Object Null;
	
	{
		String insideInitializer = FINAL_INSTANCE_VARIABLES_CAN_BE_RENAMED;
	}
	
	protected int getInt() {
		String aFinalIncanceVariable = FINAL_INSTANCE_VARIABLES_CAN_BE_RENAMED;
		return Int;
	}
	
	public String someFieldsAreShadowed() {
		String shadowed_var = "myLocalVal";
		String Österreich = "Österreich";
		return shadowed_var + Österreich + Optional.ofNullable(this.shadowed_var).orElse("");
	}
	
	public String referenceWithQualifiedName() {
		FieldNameConventionRule conentionRule = new FieldNameConventionRule();
		int length = conentionRule.MyArray.length;
		return conentionRule.my$val;
	}
	
	public void invokingObjectMethod() {
		int length = MyArray.length;
	}
	
	public void accessingEnumConstants() {
		WeekDays day = WeekDays.SATURDAAAY;
		switch (day) {
		case Mon:
			break;
		case Fri:
			break;

		default:
			break;
		}
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
	
	public void referencingRenamesInInnerClass() {
		Foo foo = new Foo();
		
		String t = foo.Can_be_renamed;
	}
	
	public class Foo {
		
//		private Integer Int;
		private String i_nt;
		private String _myval;
		private String Can_be_renamed = "";
		
		public Foo () {
			FieldNameConventionRule conventionRule = new FieldNameConventionRule();
			Can_be_renamed = "_" + FINAL_INSTANCE_VARIABLES_CAN_BE_RENAMED;
			conventionRule._myval = "";
			if(my$val != null) {
				Int = 0;
			}
		}
		
		public String getMyVal() {
			Can_be_renamed = "in_getMyVal";
			return _myval;
		}
		
		public Integer getIntValue() {
			return Int;
		}
		
		public String getCanBeRenamed() {
			InnerFoo innerInnerFoo = new InnerFoo();
			String s = innerInnerFoo.innerInnerFooString;
			return Can_be_renamed;
		}
		
		public class InnerFoo {
			private String innerInnerFooString;
			public InnerFoo() {
				String aFinalIncanceVariable = FINAL_INSTANCE_VARIABLES_CAN_BE_RENAMED;
				String s = Can_be_renamed;
				s = my$val;
			}
			
			class EndlessInnerFoo {
				
			}
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
		String camelCaseMe = CAMEL_CASE_ME;
	}
}

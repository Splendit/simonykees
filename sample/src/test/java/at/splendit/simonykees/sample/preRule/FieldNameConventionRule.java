package at.splendit.simonykees.sample.preRule;

import java.util.Optional;

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
	private String myval$;
	private String my$val;
	
	private String österreich, Österreich;
	
	protected int getInt() {
		return Int;
	}
	
	public String someFieldsAreShadowed() {
		String myval = "myLocalVal";
		
		String Österreich = "Österreich";
		
		return myval + Österreich + Optional.ofNullable(this._myval).orElse("");
	}
	
	class Foo {
		
		private Integer _Int;
		
		public Foo () {
			Int = 0;
		}
		
		public Integer getIntValue() {
			return Int;
		}
	}
}

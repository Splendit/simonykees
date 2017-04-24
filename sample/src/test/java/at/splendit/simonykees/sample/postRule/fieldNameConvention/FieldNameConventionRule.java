package at.splendit.simonykees.sample.postRule.fieldNameConvention;

import java.util.Optional;

public class FieldNameConventionRule {
	private String string;
	private Foo foo;
	private FieldNameConventionRule fieldNameConventionRuleCammelCase;
	
	private String fieldName01W1thNumbers;
	
	int $0;
	int $_;
	private String $$$;
	private int Int;
	private final String dontRenameMe = "";
	
	private static String iNt, i$nt, sWitch, $int, _int;
	private String myval;
	private String $myval;
	private String myval_;
	private String myval$;
	private String myVal;
	
	private String österreich, Österreich;
	
	protected int getInt() {
		return Int;
	}
	
	public String someFieldsAreShadowed() {
		String myval = "myLocalVal";
		
		String Österreich = "Österreich";
		
		return myval + Österreich + Optional.ofNullable(this.myval).orElse("");
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

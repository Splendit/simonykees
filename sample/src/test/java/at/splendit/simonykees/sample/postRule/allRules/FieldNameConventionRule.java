package at.splendit.simonykees.sample.postRule.allRules;

import java.util.Optional;

@SuppressWarnings({ "nls", "unused" })
public class FieldNameConventionRule {
	private static String iNt, i$nt, sWitch, $int, _int;
	private static String canBeRenamed = "expecting renaming";
	private static String CAMMEL_CASE_ME = "this can be converted to cammel case";
	private String string;
	private Foo foo;
	private FieldNameConventionRule fieldNameConventionRuleCammelCase;
	private String fieldName01W1thNumbers;
	int $0;
	int $_;
	private String $$$;
	private int Int;
	private final String DONT_RENAME_ME = "";
	private String myval;
	private String $myval;
	private String myval_;
	private String myval$ = myval;
	private String myVal = myval;
	private String mon = myval$;
	private String shadowedVar;
	private String österreich, Österreich;
	public Integer MyInt;

	public String someFieldsAreShadowed() {
		String shadowed_var = "myLocalVal";
		String Österreich = "Österreich";
		return shadowed_var + Österreich + Optional.ofNullable(this.shadowedVar).orElse("");
	}

	public String referenceWithQualifiedName() {
		FieldNameConventionRule conentionRule = new FieldNameConventionRule();
		return conentionRule.myVal;
	}

	public String methodImplementigAnAnonymousClass() {
		String _myval = Optional.ofNullable(this.myval).orElse("");
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

	protected int getInt() {
		return Int;
	}

	enum WeekDays {
		Mon, Tue, Wed, Thu, Fri, SATURDAAAY, SUNDAAAYY;

		private Integer _myval;

		public Integer getMyVal() {
			FieldNameConventionRule conventionRule = new FieldNameConventionRule();
			conventionRule.myVal = null;
			return this._myval;
		}
	}

	@interface FooAnnotation {
		String myvalue = _int;
		String Mon = canBeRenamed;
		String cammelCaseMe = CAMMEL_CASE_ME;

	}

	class Foo {

		// private Integer Int;
		private String iNt;
		private String myval;

		public Foo() {
			FieldNameConventionRule conventionRule = new FieldNameConventionRule();
			conventionRule.myval = "";
			if (myVal != null) {
				Int = 0;
			}
		}

		public String getMyVal() {

			return myval;
		}

		public Integer getIntValue() {
			return Int;
		}
	}

	interface FooInterface {
		int getMyVal();

		WeekDays getTomorrow();
	}
}

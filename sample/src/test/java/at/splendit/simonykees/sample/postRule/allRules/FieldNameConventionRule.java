package at.splendit.simonykees.sample.postRule.allRules;

import java.util.LinkedList;
import java.util.Optional;

@SuppressWarnings({ "nls", "unused" })
public class FieldNameConventionRule {
	private static final String DONT_RENAME_ME = "";
	private static String iNt;
	private static String i$nt;
	private static String sWitch;
	private static String $int;
	private static String _int;
	private static String canBeRenamed = "expecting renaming";
	private static String CAMEL_CASE_ME = "this can be converted to camel case";
	private String string;
	private Foo Foo;
	private FieldNameConventionRule fieldNameConventionRuleCammelCase;
	private String fieldName01W1thNumbers;
	int $0;
	int $_;
	private String $$$;
	private int Int;
	private final String finalInstanceVariablesCanBeRenamed = "toBeRenamed";
	private String myval;
	private String $myval;
	private String myval_;
	private String myval$ = myval;
	private String myVal = myval;
	private String mon = myval$;
	private String shadowedVar;
	private String österreich;
	private String Österreich;
	private String[] myArray = { "my", "array" };
	public Integer MyInt;
	private int True;
	private double False;
	private Object Null;

	{
		String insideInitializer = finalInstanceVariablesCanBeRenamed;
	}

	protected int getInt() {
		String aFinalIncanceVariable = finalInstanceVariablesCanBeRenamed;
		return Int;
	}

	public String someFieldsAreShadowed() {
		String shadowed_var = "myLocalVal";
		String Österreich = "Österreich";
		return shadowed_var + Österreich + Optional.ofNullable(this.shadowedVar).orElse("");
	}

	public String referenceWithQualifiedName() {
		FieldNameConventionRule conentionRule = new FieldNameConventionRule();
		int length = conentionRule.myArray.length;
		return conentionRule.myVal;
	}

	public void invokingObjectMethod() {
		int length = myArray.length;
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

	public void referencingRenamesInInnerClass() {
		Foo foo = new Foo();

		String t = foo.canBeRenamed;
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
		String camelCaseMe = CAMEL_CASE_ME;
	}

	static final class InnerClassWithSameFieldName {
		private int index;

		InnerClassWithSameFieldName(int i) {
			this.index = i;
		}

		public void setIndex(int i) {
			this.index = i;
		}

		@SuppressWarnings("serial")
		private static final class NestedInnerClassWithSameFieldName extends LinkedList<String> {
			private int index_;
			InnerClassWithSameFieldName innerClaz = new InnerClassWithSameFieldName(0);

			public void setInnerAndOuterIndex(int i) {
				this.index_ = i;
				innerClaz.index = index_;
			}
		}
	}

	public class Foo {

		// private Integer Int;
		private String i_nt;
		private String _myval;
		private String canBeRenamed = "";

		public Foo() {
			FieldNameConventionRule conventionRule = new FieldNameConventionRule();
			canBeRenamed = "_" + finalInstanceVariablesCanBeRenamed;
			conventionRule.myval = "";
			if (myVal != null) {
				Int = 0;
			}
		}

		public String getMyVal() {
			canBeRenamed = "in_getMyVal";
			return _myval;
		}

		public Integer getIntValue() {
			return Int;
		}

		public String getCanBeRenamed() {
			InnerFoo innerInnerFoo = new InnerFoo();
			String s = innerInnerFoo.innerInnerFooString;
			return canBeRenamed;
		}

		public class InnerFoo {
			private String innerInnerFooString;

			public InnerFoo() {
				String aFinalIncanceVariable = finalInstanceVariablesCanBeRenamed;
				String s = canBeRenamed;
				s = myVal;
			}

			class EndlessInnerFoo {

			}
		}
	}

	interface FooInterface {
		int getMyVal();

		WeekDays getTomorrow();
	}
}

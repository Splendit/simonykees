package at.splendit.simonykees.sample.postRule.allRules;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
@SuppressWarnings({ "nls", "unused" })
public class StringBufferToBuilderRule {
	private StringBuffer stringBuffer1 = new StringBuffer();
	private StringBuffer stringBuffer2;
	private StringBuffer stringBuffer3 = new StringBuffer();
	private StringBuffer stringBuffer4;
	private StringBuffer stringBuffer5;
	private StringBuffer stringBuffer6 = new StringBuffer();

	public StringBuffer stringBuffer7 = new StringBuffer();

	public StringBufferToBuilderRule() {
		stringBuffer2 = new StringBuffer();
	}

	private void test1() {
		StringBuilder localStringBuffer1 = new StringBuilder();
		StringBuilder localStringBuffer2 = null;
		StringBuilder localStringBuffer3;
		StringBuilder localStringBuffer4 = new StringBuilder();

		if (localStringBuffer2 == null) {
			localStringBuffer2 = new StringBuilder();
		}

		stringBuffer1.append("asdf").append("jkl");
		stringBuffer2.append("asdf").append("jkl");
		localStringBuffer1.append("asdf").insert(3, "jkl");
		localStringBuffer2.append("asdf").insert(2, "jkl");
	}

	private StringBuffer test2() {
		StringBuilder localStringBuffer1 = new StringBuilder();
		StringBuilder localStringBuffer2 = null;
		StringBuilder localStringBuffer3 = null;
		StringBuffer localStringBuffer4 = new StringBuffer();

		if (localStringBuffer2 == null) {
			localStringBuffer2 = new StringBuilder();
		}
		if (localStringBuffer3 == null) {
			localStringBuffer3 = new StringBuilder();
		}

		stringBuffer1.append("asdf").append("jkl");
		stringBuffer2.append("asdf").append("jkl");
		localStringBuffer1.append("asdf").insert(3, "jkl");
		localStringBuffer2.append("asdf").insert(2, "jkl");

		return localStringBuffer4;
	}

	private StringBuffer test3(StringBuffer stringBufferArg) {
		StringBuilder localStringBuffer1 = new StringBuilder();
		StringBuffer localStringBuffer2 = stringBufferArg;
		StringBuilder localStringBuffer3 = null;
		StringBuffer localStringBuffer4 = new StringBuffer();

		if (localStringBuffer2 == null) {
			localStringBuffer2 = new StringBuffer();
		}
		if (localStringBuffer3 == null) {
			localStringBuffer3 = new StringBuilder();
		}

		return localStringBuffer4;
	}

	private StringBuffer test4(StringBuffer stringBufferArg) {
		StringBuilder localStringBuffer1 = new StringBuilder();
		StringBuilder localStringBuffer2 = null;
		StringBuilder localStringBuffer3 = null;
		StringBuilder localStringBuffer4 = new StringBuilder();

		if (localStringBuffer2 == null) {
			localStringBuffer2 = new StringBuilder();
		}
		if (localStringBuffer3 == null) {
			localStringBuffer3 = new StringBuilder();
		}

		return stringBufferArg;
	}

	private StringBuffer test5(StringBuffer stringBufferArg) {
		StringBuilder localStringBuffer1 = new StringBuilder();
		StringBuffer localStringBuffer2 = null;
		StringBuilder localStringBuffer3 = null;
		StringBuilder localStringBuffer4 = new StringBuilder();

		if (localStringBuffer2 == null) {
			localStringBuffer2 = stringBufferArg;
		}
		if (localStringBuffer3 == null) {
			localStringBuffer3 = new StringBuilder();
		}

		return stringBufferArg;
	}

	private StringBuffer test6(StringBuffer stringBufferArg) {
		StringBuilder localStringBuffer1 = new StringBuilder();
		StringBuilder localStringBuffer2 = null;
		StringBuffer localStringBuffer3 = null;
		StringBuilder localStringBuffer4 = new StringBuilder();

		if (localStringBuffer2 == null) {
			localStringBuffer2 = new StringBuilder();
		}
		if (localStringBuffer3 == null) {
			localStringBuffer3 = stringBufferArg;
		}

		return stringBufferArg;
	}

	private void test7() {
		StringBuilder localStringBuffer1 = new StringBuilder();
		StringBuffer localStringBuffer2 = new StringBuffer();

		test6(localStringBuffer2);
		localStringBuffer1.append("asdf");
	}

	public StringBuffer getStringBuffer1() {
		return stringBuffer1;
	}

	private StringBuffer getStringBuffer2() {
		return stringBuffer2;
	}

	public StringBuffer getLocalStringBuffer1() {
		StringBuffer localStringBuffer3 = new StringBuffer();
		localStringBuffer3.append("asdf").append("jkl");
		return localStringBuffer3;
	}

	private StringBuffer getLocalStringBuffer2() {
		StringBuffer localStringBuffer4 = new StringBuffer();
		localStringBuffer4.append("asdf").append("jkl");
		return localStringBuffer4;
	}
}

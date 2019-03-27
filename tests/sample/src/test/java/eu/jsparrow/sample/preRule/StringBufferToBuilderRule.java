package eu.jsparrow.sample.preRule;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
@SuppressWarnings({ "nls", "unused" })
public class StringBufferToBuilderRule {
	private StringBuffer stringBuffer1 = new StringBuffer();
	private StringBuffer stringBuffer2;
	private StringBuffer stringBuffer3 = new StringBuffer(), stringBuffer4;
	private StringBuffer stringBuffer5, stringBuffer6 = new StringBuffer();

	public StringBuffer stringBuffer7 = new StringBuffer();

	public StringBufferToBuilderRule() {
		stringBuffer2 = new StringBuffer();
	}

	private void test1() {
		//a comment here
		StringBuffer /* internal comment in declaration */ localStringBuffer1 // I don't want to break anything
		= new /* internal comment in initializer */ StringBuffer(); // trailing comment
		StringBuffer localStringBuffer2 = null;
		StringBuffer localStringBuffer3, localStringBuffer4 = new StringBuffer();

		if (localStringBuffer2 == null) {
			localStringBuffer2 = new StringBuffer();
		}

		stringBuffer1.append("asdf").append("jkl");
		stringBuffer2.append("asdf").append("jkl");
		localStringBuffer1.append("asdf").insert(3, "jkl");
		localStringBuffer2.append("asdf").insert(2, "jkl");
	}

	private StringBuffer test2() {
		StringBuffer localStringBuffer1 = new StringBuffer();
		StringBuffer localStringBuffer2 = null;
		StringBuffer localStringBuffer3 = null, localStringBuffer4 = new StringBuffer();

		if (localStringBuffer2 == null) {
			localStringBuffer2 = new StringBuffer();
		}
		if (localStringBuffer3 == null) {
			localStringBuffer3 = new StringBuffer();
		}

		stringBuffer1.append("asdf").append("jkl");
		stringBuffer2.append("asdf").append("jkl");
		localStringBuffer1.append("asdf").insert(3, "jkl");
		localStringBuffer2.append("asdf").insert(2, "jkl");

		return localStringBuffer4;
	}

	private StringBuffer test3(StringBuffer stringBufferArg) {
		StringBuffer localStringBuffer1 = new StringBuffer();
		StringBuffer localStringBuffer2 = stringBufferArg;
		StringBuffer localStringBuffer3 = null, localStringBuffer4 = new StringBuffer();

		if (localStringBuffer2 == null) {
			localStringBuffer2 = new StringBuffer();
		}
		if (localStringBuffer3 == null) {
			localStringBuffer3 = new StringBuffer();
		}

		return localStringBuffer4;
	}

	private StringBuffer test4(StringBuffer stringBufferArg) {
		StringBuffer localStringBuffer1 = new StringBuffer();
		StringBuffer localStringBuffer2 = null;
		StringBuffer localStringBuffer3 = null, localStringBuffer4 = new StringBuffer();

		if (localStringBuffer2 == null) {
			localStringBuffer2 = new StringBuffer();
		}
		if (localStringBuffer3 == null) {
			localStringBuffer3 = new StringBuffer();
		}

		return stringBufferArg;
	}

	private StringBuffer test5(StringBuffer stringBufferArg) {
		StringBuffer localStringBuffer1 = new StringBuffer();
		StringBuffer localStringBuffer2 = null;
		StringBuffer localStringBuffer3 = null, localStringBuffer4 = new StringBuffer();

		if (localStringBuffer2 == null) {
			localStringBuffer2 = stringBufferArg;
		}
		if (localStringBuffer3 == null) {
			localStringBuffer3 = new StringBuffer();
		}

		return stringBufferArg;
	}

	private StringBuffer test6(StringBuffer stringBufferArg) {
		StringBuffer localStringBuffer1 = new StringBuffer();
		StringBuffer localStringBuffer2 = null;
		StringBuffer localStringBuffer3 = null, localStringBuffer4 = new StringBuffer();

		if (localStringBuffer2 == null) {
			localStringBuffer2 = new StringBuffer();
		}
		if (localStringBuffer3 == null) {
			localStringBuffer3 = stringBufferArg;
		}

		return stringBufferArg;
	}

	private void test7() {
		StringBuffer localStringBuffer1 = new StringBuffer();
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
	
	/*
	 * Return StringBuffer tests
	 */
	
	private String returnMethodInvocation_shouldTransform() {
		StringBuffer localStringBuffer4 = new StringBuffer();
		localStringBuffer4.append("asdf").append("jkl");
		return localStringBuffer4.append("zxc").toString();
	}
	
	private StringBuffer returnSimpleName_shouldNotTransform() {
		StringBuffer localStringBuffer4 = new StringBuffer();
		localStringBuffer4.append("asdf").append("jkl");
		return localStringBuffer4;
	}
	
	private StringBuffer returnMethodInvocation_shouldNotTransform() {
		StringBuffer localStringBuffer4 = new StringBuffer();
		localStringBuffer4.append("asdf").append("jkl");
		return localStringBuffer4.append("zxc");
	}
	
	private StringBuffer returnMethodInvocationChain_shouldNotTransform() {
		StringBuffer localStringBuffer4 = new StringBuffer();
		localStringBuffer4.append("asdf").append("jkl");
		return localStringBuffer4.append("zxc").append("").insert(0, true);
	}
	
	/*
	 * StringBuffer assignment tests
	 */
	
	private void assignSimpleName_shouldTransform() {
		StringBuffer localStringBuffer1 = new StringBuffer();
		localStringBuffer1.append("asdf").append("jkl");
		String localStringBuffer2; 
		localStringBuffer2 = localStringBuffer1.toString();
	}
	
	private void assignSimpleName_shouldNotTransform() {
		StringBuffer localStringBuffer1 = new StringBuffer();
		localStringBuffer1.append("asdf").append("jkl");
		StringBuffer localStringBuffer2; 
		localStringBuffer2 = localStringBuffer1;
	}
	
	private void assignMethodInvocation_shouldNotTransform() {
		StringBuffer localStringBuffer1 = new StringBuffer();
		localStringBuffer1.append("asdf").append("jkl");
		StringBuffer localStringBuffer2; 
		localStringBuffer2 = localStringBuffer1.append("zxc");
	}
	
	private void assignMethodInvocationChain_shouldNotTransform() {
		StringBuffer localStringBuffer1 = new StringBuffer();
		localStringBuffer1.append("asdf").append("jkl");
		StringBuffer localStringBuffer2;  
		localStringBuffer2 = localStringBuffer1.append("zxc").append("").insert(0, true);
	}
	
	/*
	 * StringBuffer initialization tests
	 */
	
	private void initializerSimpleName_shouldTransform() {
		StringBuffer localStringBuffer1 = new StringBuffer();
		localStringBuffer1.append("asdf").append("jkl");
		String result = localStringBuffer1.toString();
	}
	
	private void initializerSimpleName_shouldNotTransform() {
		StringBuffer localStringBuffer1 = new StringBuffer();
		localStringBuffer1.append("asdf").append("jkl");
		StringBuffer localStringBuffer2 = localStringBuffer1;
	}
	
	private void initializerMethodInvocation_shouldNotTransform() {
		StringBuffer localStringBuffer1 = new StringBuffer();
		localStringBuffer1.append("asdf").append("jkl");
		StringBuffer localStringBuffer2 = localStringBuffer1.append("zxc");
	}
	
	private void initializerMethodInvocationChain_shouldNotTransform() {
		StringBuffer localStringBuffer1 = new StringBuffer();
		localStringBuffer1.append("asdf").append("jkl");
		StringBuffer localStringBuffer2 = localStringBuffer1.append("zxc").append("").insert(0, true);
	}

}

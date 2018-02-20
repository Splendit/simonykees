package eu.jsparrow.sample.preRule;

//be careful when you write comments! 

/*
 * Comments are such a pain. 
 */

/**
 * Corner cases for {@linkplain RearrangeClassMembersRule}.
 * 
 * @author Ardit Ymeri
 *
 */
@SuppressWarnings({"unused", "nls"})
public class RearrangeClassMembersRule {
	
    @Deprecated
    abstract class AbstractInnerType {
        //		private Days days;
        private String name;
    }
    
    @SuppressWarnings({})
    String annotatedField = "";
    //this comment lies above anotherFoo filed
    private String anotherFoo;
    public int noEmptyLineAbove;
    
    /**
     * Don't ever use this annotation. It is made only for tests.
     *
     */
    @interface ClassPreamble {
        String author();
        String date();
        int currentRevision() default 1;
        String lastModified() default "N/A";
        String lastModifiedBy() default "N/A";
        // Note use of array
        String[] reviewers();
    }
    
    String defaultModifierFoo;
    public String publicFoo;
    @Deprecated
    public String publicAnnotatedFoo;
    private static final String A_STATIC_FINAL_FIELD;
    static {
    	A_STATIC_FINAL_FIELD = "staticFinalString";
    }
    protected static final String A_PROTECTED_FINAL_FIELD = "protectedFinalString";
    public static final String A_PUBLIC_FINAL_FIELD = A_STATIC_FINAL_FIELD;
    
    private final String myString = "to-be-used-as-initializer";
    public String initializedField = myString;
    class SomethingCouldBeInnerType {

        private String foo = "it-shadows-the-outer-class";


        // comment above the default ctor
        /**
         * Docs above the default ctor.
         */
        public SomethingCouldBeInnerType() {
            foo = "it-shadows-the-outer-class";
        }

        public SomethingCouldBeInnerType(String input) {
            foo = input;
        }

        private void resetFoo() {
            foo = "";
        }
    }
    
    // comment above foo
    private String foo = "foo-value";
    
    private enum Days {
        Mon, Tue, Wed, Thu, Fri, Sat, Sun,
    }
    
    protected String protectedFoo;
    
    {{
        Days a = Days.Mon;
        // some useless block
        int t = 0;
        for(int i = 0; i < t; i++) {
            foo = "foo-value";
        }
    }}
    private static final String ND_STATIC_FINAL_FIELD = "staticFinalString";
    public static final String ND_PUBLIC_FINAL_FIELD = "protectedFinalString";
    @SuppressWarnings({})
    protected String protectedAnnotatedFoo;
    
    public static void staticMethod() {
    	// should show up below constructors
    	String description = "is public, has comments  ";
    	description += "and is static";
    }
    
    public void instanceMethod() {
    	// should show up below static methods
    	String description = "is public, has comments  ";
    	description += "and is NOT static";
    }
    
    private void sampleMethod() {
        if(foo != null && foo.isEmpty()) {
            foo = "foo-value";
        }
    }
    
    /**
     * A static method for testing code rearrange
     */
    private static void sampleStaticMethod() {
    	// doesn't do much 
    	String description = "has comments and javadoc ";
    	description += "and is static";
    }
    
    public RearrangeClassMembersRule() {
        this.foo = "foo-value";
        this.anotherFoo = "another-foo";
    }
}

@SuppressWarnings({"unused", "nls"})
class SecondClassInCompilationUnit {
	
      private void resetFoo() {
          foo = "";
      }
      
      String foo = "has-no-modifier";
  public SecondClassInCompilationUnit() {
        foo = "ctor-is-the-last";
    }
}

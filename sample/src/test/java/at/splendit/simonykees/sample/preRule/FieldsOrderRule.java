package at.splendit.simonykees.sample.preRule;

@SuppressWarnings({"unused", "nls"})
public class FieldsOrderRule {

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

    @SuppressWarnings({})
    protected String protectedAnnotatedFoo;

    private void sampleMethod() {
        if(foo != null && foo.isEmpty()) {
            foo = "foo-value";
        }
    }

    public FieldsOrderRule() {
        this.foo = "foo-value";
        this.anotherFoo = "another-foo";
    }

}

@SuppressWarnings({"unused", "nls"})
class SecondClassInCompilationUnit {
      String foo = "has-no-modifier";
      private void resetFoo() {
          foo = "";
      }
  public SecondClassInCompilationUnit() {
        foo = "ctor-is-the-last";
    }


}

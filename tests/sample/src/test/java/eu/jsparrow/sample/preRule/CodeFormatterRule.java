package eu.jsparrow.sample.preRule;

public class CodeFormatterRule {

	public int a( int i ) { i *= 2; return i; }

	public int b( int i ) { if (0 == i) {return 1;} else {return 0;  } }
	
	/**
	 * @param i Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
	 	@return evaluated in
	 */
	public  int c( int i ) { return i + 3;}
	
	public void d( int  i ) { if (0 == i) { use( 1 ); } else { use(0) ;  } }
	
	public void use(int i) {}
}

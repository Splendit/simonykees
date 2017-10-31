package eu.jsparrow.core.visitor.renaming;

import java.util.Arrays;

/**
 * List of java key words sorted by name. Taken from
 * {@linkplain https://docs.oracle.com/javase/tutorial/java/nutsandbolts/_keywords.html}
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class JavaReservedKeyWords {

	private JavaReservedKeyWords() {
		/*
		 * Hiding the default public constructor
		 */
	}

	@SuppressWarnings("nls")
	static final String[] javaKeyWords = { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
			"class", "const",

			"continue", "default", "do", "double", "else", "enum", "extends", "false", "final", "finally", "float",

			"for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",

			"new", "null", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
			"super",

			"switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "void", "volatile",
			"while" };

	public static boolean isKeyWord(String keyword) {
		return (Arrays.binarySearch(javaKeyWords, keyword) >= 0);
	}
}

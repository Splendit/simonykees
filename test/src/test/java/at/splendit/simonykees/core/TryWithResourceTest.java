package at.splendit.simonykees.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.util.ASTPrinter;


@RunWith(Parameterized.class)
public class TryWithResourceTest extends AbstractTest {

	private ASTParser astParser;
	private final String baseDir = "src/main/java/resource/";
	private String inputFile;
	private String outputFile;
	

	@Before
	public void setUP() throws Exception {
		astParser = ASTParser.newParser(AST.JLS8);
		astParser.setCompilerOptions(JavaCore.getDefaultOptions());
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		BufferedReader br = new BufferedReader(new FileReader(new File(baseDir+inputFile)));
        String sCurrentLine = "";
        StringBuilder sb = new StringBuilder();
        while ((sCurrentLine = br.readLine()) != null) {
			sb.append(sCurrentLine);
			sb.append(LINE_SEPARATOR);
		}
        astParser.setSource(sb.toString().toCharArray());
        br.close();
	}

	@After
	public void tearDown() {
		astParser = null;
	}
	
	
	public TryWithResourceTest(String inputFile, String outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}
	
	//TODO add more tests for arithmetic optimization
	@Parameters(name = "{index}: inputfile[{0}] outputfile[{1}]")
	public static Collection<Object[]> assignments() {
		return Arrays.asList(new Object[][] {
				{ "tryWithResource/TryWithResource.java", "tryWithResource/TryWithResource.java" },
		});
	}


	@Test
	public void tryWithResourceTest() throws Exception {
		CompilationUnit astRoot = (CompilationUnit) astParser.createAST(null);
		ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		ASTPrinter.print(astRoot, 0);

	}

}

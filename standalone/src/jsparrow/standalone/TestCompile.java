package jsparrow.standalone;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.util.Util;

@SuppressWarnings("restriction")
public class TestCompile {
	static class ByteClassLoader extends ClassLoader {
		private Map<String, byte[]> classMap;

		public ByteClassLoader(Map<String, byte[]> classMap) {
			super();
			this.classMap = classMap;
		}

		protected Class<?> findClass(String name) throws ClassNotFoundException {
			byte[] bytes = classMap.get(name);
			if (bytes == null) {
				return super.findClass(name);
			} else {
				return defineClass(name, bytes, 0, bytes.length);
			}
		}
	}

	public static ICompilationUnit[] compile(String code, String filename) {
		ArrayList<Classpath> cp = new ArrayList<FileSystem.Classpath>();
		Util.collectRunningVMBootclasspath(cp);
		INameEnvironment env = new NameEnv(cp.toArray(new FileSystem.Classpath[cp.size()]), null, true);
		ICompilerRequestor requestor = new ICompilerRequestor() {
			@Override
			public void acceptResult(CompilationResult result) {
				ClassFile[] cf = result.getClassFiles();
				HashMap<String, byte[]> classMap = new HashMap<String, byte[]>();
				classMap.put("Test", cf[0].getBytes());
				ByteClassLoader cl = new ByteClassLoader(classMap);
				try {
					Class<?> c = cl.loadClass("Test");
					Method m = c.getMethod("test");
					m.invoke(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		Compiler compiler = new Compiler(env, DefaultErrorHandlingPolicies.exitAfterAllProblems(),
				new CompilerOptions(), requestor, new DefaultProblemFactory());

		ICompilationUnit[] units = new ICompilationUnit[] { new CompilationUnit(code.toCharArray(), filename, null) };
		compiler.compile(units);

		return units;
	}

	public static void main(String[] args) {
		compile("public class Test { public static void test() { System.out.println(\"Hello, world.\"); }}",
				"Test.java");

//		createFile();
	}

//	public static void createFile() {
//		final String PACKAGE_PATH = "z.ex/src/z/ex/go";
//		final String CONTENT = "package z.ex.go;\n"
//				+ "public class RunAway {\npublic static void main(String[] args) {\n"
//				+ "System.out.println(\"Run Away\");\n}\n}\n";
//		IPath location = new Path(PACKAGE_PATH);
//		IFile file = IFile.create;
//		file.createLink(location, IResource.NONE, null);
//		
//		try {
//			if (!file.exists()) {
//				file.create(new ByteArrayInputStream(CONTENT.getBytes()), true, new NullProgressMonitor());
//			} else {
//				file.setContents(new ByteArrayInputStream(CONTENT.getBytes()), IFile.FORCE | IFile.KEEP_HISTORY,
//						new NullProgressMonitor());
//			}
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
//		return;
//	}
}

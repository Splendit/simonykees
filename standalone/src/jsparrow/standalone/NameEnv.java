package jsparrow.standalone;

import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;

@SuppressWarnings("restriction")
public class NameEnv extends FileSystem implements INameEnvironment {

	protected NameEnv(Classpath[] paths, String[] initialFileNames, boolean annotationsFromClasspath) {
		super(paths, initialFileNames, annotationsFromClasspath);
		// TODO Auto-generated constructor stub
	}

}

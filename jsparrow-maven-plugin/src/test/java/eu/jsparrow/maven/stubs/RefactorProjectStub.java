package eu.jsparrow.maven.stubs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.codehaus.plexus.util.ReaderFactory;

@SuppressWarnings("nls")
public class RefactorProjectStub extends MavenProjectStub {

	/**
	 * Default constructor
	 */
	public RefactorProjectStub() {
		try {
			createSourceCode();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MavenXpp3Reader pomReader = new MavenXpp3Reader();
		Model model;
		try {
			model = pomReader.read(ReaderFactory.newXmlReader(getPom()));
			setModel(model);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		setGroupId(model.getGroupId());
		setArtifactId(model.getArtifactId());
		setVersion(model.getVersion());
		setName(model.getName());
		setUrl(model.getUrl());
		setPackaging(model.getPackaging());
		List<String> classpath = new ArrayList<>();
		classpath.add(getPathToRtJar());
		setRuntimeClasspathElements(classpath);

		Build build = new Build();
		build.setFinalName(model.getArtifactId());
		build.setDirectory(getBasedir() + "/target");
		build.setSourceDirectory(getBasedir() + "/src/main/java");
		build.setOutputDirectory(getBasedir() + "/target/classes");
		build.setTestSourceDirectory(getBasedir() + "/src/test/java");
		build.setTestOutputDirectory(getBasedir() + "/target/test-classes");
		setBuild(build);

		List<String> compileSourceRoots = new ArrayList<String>();
		compileSourceRoots.add(getBasedir() + "/src/main/java");
		setCompileSourceRoots(compileSourceRoots);

		List<String> testCompileSourceRoots = new ArrayList<String>();
		testCompileSourceRoots.add(getBasedir() + "/src/test/java");
		setTestCompileSourceRoots(testCompileSourceRoots);
	}

	/** {@inheritDoc} */
	public File getBasedir() {
		return new File(super.getBasedir() + "/src/test/resources/jsparrow-maven-test/");
	}

	/** {@inheritDoc} */
	public File getSourceDir() {
		return new File(getBasedir() + "/src/main/java");
	}

	public File getPom() {
		return new File(getBasedir(), "pom.xml");
	}

	private static String getPathToRtJar() {
		final String classPath = System.getProperty("sun.boot.class.path");
		final int idx = StringUtils.indexOf(classPath, "rt.jar");
		if (idx == -1) {
			throw new RuntimeException("Could not find Java runtime library rt.jar");
		}
		final int end = idx + "rt.jar".length();
		final int lastIdx = classPath.lastIndexOf(":", idx);
		final int start = lastIdx != -1 ? lastIdx + 1 : 0;
		return StringUtils.substring(classPath, start, end);
	}

	public void createSourceCode() throws IOException {
		File baseDir = getBasedir();
		if (!baseDir.exists()) {
			baseDir.mkdirs();
		}
		// create the source
		createProjectPom();

		File sourceDir = getSourceDir();
		if (!sourceDir.exists()) {
			sourceDir.mkdirs();
		}
		File sourceFile = new File(getSourceDir() + "/Hello.java");
		if (!sourceFile.exists()) {
			sourceFile.createNewFile();
		}
		FileWriter writer = new FileWriter(sourceFile);

		writer.write("package main.java;\n" + "public class Hello{ \n" + " public void doit() { \n"
				+ "   System.out.println(\"Hello world\") ;\n" + " }\n" + "}");
		writer.close();
	}

	public void createProjectPom() throws IOException {
		File sourceFile = new File(getBasedir() + "/pom.xml");
		if (!sourceFile.exists()) {
			sourceFile.createNewFile();
		}
		FileWriter writer = new FileWriter(sourceFile);

		writer
			.write("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
					+ "	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
					+ "	<modelVersion>4.0.0</modelVersion>\n" + "	<groupId>sample</groupId>\n"
					+ "	<artifactId>sample</artifactId>\n" + "	<version>0.0.1-SNAPSHOT</version>\n" + "	<build>\n"
					+ "		<sourceDirectory>src</sourceDirectory>\n" + "		<plugins>\n" + "			<plugin>\n"
					+ "				<groupId>eu.jsparrow</groupId>\n"
					+ "				<artifactId>jsparrow-maven-plugin</artifactId>\n"
					+ "				<version>2.5.0-SNAPSHOT</version>\n" + "				<configuration>\n"
					+ "					<!-- The defined stubs -->\n"
					+ "					<project implementation=\"eu.jsparrow.maven.stubs.RefactorProjectStub\" />\n"
					+ "				</configuration>\n" + "			</plugin>\n" + "			<plugin>\n"
					+ "				<artifactId>maven-compiler-plugin</artifactId>\n"
					+ "				<version>3.5.1</version>\n" + "				<configuration>\n"
					+ "					<source>1.8</source>\n" + "					<target>1.8</target>\n"
					+ "				</configuration>\n" + "			</plugin>\n" + "		</plugins>\n"
					+ "	</build>\n" + "	<dependencies>\n" + "		<dependency>\n"
					+ "			<groupId>junit</groupId>\n" + "			<artifactId>junit</artifactId>\n"
					+ "			<version>4.12</version>\n" + "			<scope>test</scope>\n" + "		</dependency>\n"
					+ "		<dependency>\n" + "			<groupId>javax.servlet</groupId>\n"
					+ "			<artifactId>servlet-api</artifactId>\n" + "			<version>2.5</version>\n"
					+ "			<scope>provided</scope>\n" + "		</dependency>\n" + "		<dependency>\n"
					+ "			<groupId>commons-collections</groupId>\n"
					+ "			<artifactId>commons-collections</artifactId>\n" + "			<version>3.2.2</version>\n"
					+ "			<type>jar</type>\n" + "			<scope>compile</scope>\n" + "		</dependency>\n"
					+ "		<dependency>\n" + "			<groupId>commons-logging</groupId>\n"
					+ "			<artifactId>commons-logging</artifactId>\n" + "			<version>1.1.3</version>\n"
					+ "		</dependency>\n" + "		<dependency>\n" + "			<groupId>org.slf4j</groupId>\n"
					+ "			<artifactId>slf4j-api</artifactId>\n" + "			<version>1.6.1</version>\n"
					+ "			<type>jar</type>\n" + "			<scope>compile</scope>\n" + "		</dependency>\n"
					+ "		<dependency>\n" + "			<groupId>org.apache.commons</groupId>\n"
					+ "			<artifactId>commons-lang3</artifactId>\n" + "			<version>3.1</version>\n"
					+ "		</dependency>\n" + "\n" + "	</dependencies>\n" + "</project>");
		writer.close();
	}

	public void createYamlFile() throws IOException {
		File sourceFile = new File(getBasedir() + "/jsparrow.yml");
		FileWriter writer = new FileWriter(sourceFile);

		writer.write("# specify one of the profiles declared below as the selected profile.\n"
				+ "# if the selectedProfile is not specified the rules in the “rules:” section will be applied\n"
				+ "selectedProfile: profile1\n" + "\n" + "# define profiles here\n" + "profiles:\n"
				+ " - name: profile1\n" + "   rules:\n" + "     - TryWithResource\n" + "     - MultiCatch\n"
				+ "     - CodeFormatter\n" + "\n"
				+ "# rules in this section will be executed, if no profile has been specified as selectedProfile or via maven.\n"
				+ "# to deactivate rules, they could be commented with the #-sign\n" + "rules:\n"
				+ " - TryWithResource\n" + " - MultiCatch");
		writer.close();
	}
	
	public void createYamlFileWithError() throws IOException {
		File sourceFile = new File(getBasedir() + "/jsparrow.yml");
		FileWriter writer = new FileWriter(sourceFile);

		writer.write("This is not a valid YAML file.");
		writer.close();
	}
}
